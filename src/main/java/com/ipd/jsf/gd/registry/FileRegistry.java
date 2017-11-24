/**
 * Copyright 2004-2048 .
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.registry;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.registry.file.FileRegistryHelper;
import com.ipd.jsf.gd.config.RegistryConfig;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.FileUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.ListDifference;
import com.ipd.jsf.gd.util.MapDifference;
import com.ipd.jsf.gd.util.ScheduledService;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.util.ValueDifference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Title: 文件注册中心<br>
 * <p/>
 * Description: 可单独启动，也可以作为其它注册中心的备用（设置主动订阅subscribe为false）<br>
 * <p/>
 */
public class FileRegistry implements ClientRegistry {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(FileRegistry.class);

    /**
     * 当前Registry配置
     */
    protected final RegistryConfig config;

    /**
     * 定时加载
     */
    private ScheduledService scheduledExecutorService;

    /**
     * The Memory cache of provider list.
     */
    private Map<String, List<Provider>> memoryCache = new ConcurrentHashMap<String, List<Provider>>();

    /**
     * 内存发生了变化，如果为true，则将触发写入文件动作
     */
    private boolean needBackup = false;

    /**
     * 是否订阅通知（即扫描文件变化），默认为false
     * 如果FileRegistry是被动加载（例如作为注册中心备份的）的，建议false，防止重复通知
     */
    private boolean subscribe = false;

    /**
     * 订阅者通知列表（key为订阅者关键字，value为listener）
     */
    private Map<String, ProviderListener> notifyListeners = new ConcurrentHashMap<String, ProviderListener>();

    /**
     * 最后一次扫描文件时间
     */
    private long lastLoadTime;
    /**
     * 扫描周期，毫秒
     */
    private int scanPeriod = 60000;

    /**
     * 构造函数，一般为spring加载而来，默认订阅变化
     *
     * @param registryConfig
     *         the registry config
     */
    protected FileRegistry(RegistryConfig registryConfig) {
        this(registryConfig, true);
    }

    /**
     * 输出和备份文件目录
     */
    private String address;
    /**
     * 是否可以更换备份文件路径
     */
    private boolean addressCanChange;

    /**
     * 构造函数，一般为主动调用，是否订阅参数决定
     *
     * @param registryConfig
     *         注册中心配置
     * @param startSubscribe
     *         是否订阅
     */
    public FileRegistry(RegistryConfig registryConfig, boolean startSubscribe) {
        String file = registryConfig.getFile(); // 文件指定的注册中心文件读取地址
        if (StringUtils.isNotBlank(file)) { // 文件里配了文件里为准
            if (!check(file)) {
                throw new IllegalConfigureException(21103, "registry.file", file);
            }
            address = file;
            addressCanChange = false;
        } else { // 文件没配，先取注册中心下发配置
            file = JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_BACKUP_DIR, null);
            if (file != null && check(file)) {
                address = file;
            } else { // 无配置或者配置不对，取默认地址
                address = FileUtils.getUserHomeDir(Constants.DEFAULT_PROTOCOL);
                if (!check(address)) {
                    JSFContext.put("provider.backfile", "false"); // 打上标记
                }
            }
            addressCanChange = true;
        }

        // 从最新的file中加载到内存，记录最后一次加载时间
        this.lastLoadTime = startSubscribe ? FileRegistryHelper.loadBackupFileToCache(address, memoryCache)
                : 0;
        this.config = registryConfig;
        // 设置状态
        this.subscribe = startSubscribe;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // 如果要求备份，那么说明内存中为最新的，无需加载
                    if (needBackup) {
                        FileRegistryHelper.backup(address, memoryCache);
                        needBackup = false;
                    }

                    // 订阅变化（默认是不订阅的）
                    // 检查最后修改时间，如果有有变，则自动重新加载
                    else if (subscribe && FileRegistryHelper.checkModified(address, lastLoadTime)) {
                        // 加载到内存
                        Map<String, List<Provider>> tempCache = new ConcurrentHashMap<String, List<Provider>>();
                        long currentTime = FileRegistryHelper.loadBackupFileToCache(address, tempCache);
                        // 比较旧列表和新列表，通知订阅者变化部分
                        notifyConsumer(tempCache, memoryCache, notifyListeners);

                        // 通知完保存到内存
                        memoryCache = tempCache;
                        // 如果有文件更新,将上一次更新时间保持为当前时间
                        lastLoadTime = currentTime;
                    }
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };
        //启动扫描线程
        scheduledExecutorService = new ScheduledService("JSF-FileRegistry-Back-Load",
                ScheduledService.MODE_FIXEDDELAY,
                task, //定时load任务
                scanPeriod, // 延迟一个周期
                scanPeriod, // 一个周期循环
                TimeUnit.MILLISECONDS
        ).start();
    }

    /**
     * 设置备份地址 运行时？？
     *
     * @param address
     */
    public void setAddress(String address) {
        if (addressCanChange && address != null && !address.equals(this.address) && check(address)) {
            LOGGER.info("Provider backup dir changed from {} to {}", this.address, address);
            this.address = address;
        }
    }

    /**
     * 检查地址时候是否可用
     *
     * @param address 配置的地址
     * @return 是否可用
     */
    private boolean check(String address) {
        try {
            File file = new File(address);
            if (!file.exists() && !file.mkdirs()) {
                return false;
            }
            return file.isDirectory() && file.canRead() && file.canWrite();
        } catch (Exception e) {
            LOGGER.warn("Check file " + address + " error, cause by : " + e.getMessage());
            return false;
        }
    }

    /**
     * Notify consumer.
     *
     * @param newCache
     *         the new cache
     * @param oldCache
     *         the old cache
     * @param notifyListeners
     *         the notify listeners
     */
    private void notifyConsumer(Map<String, List<Provider>> newCache, Map<String, List<Provider>> oldCache,
                                Map<String, ProviderListener> notifyListeners) {
        // 比较两个map的差异
        MapDifference<String, List<Provider>> difference = new MapDifference<String, List<Provider>>(newCache, oldCache);
        // 新的有，旧的没有，通知
        Map<String, List<Provider>> onlynew = difference.entriesOnlyOnLeft();
        for (Map.Entry<String, List<Provider>> entry : onlynew.entrySet()) {
            ProviderListener listener = notifyListeners.get(entry.getKey());
            if (listener != null) {
                listener.addProvider(entry.getValue());
            }
        }
        // 旧的有，新的没有，全部干掉
        Map<String, List<Provider>> onlyold = difference.entriesOnlyOnRight();
        for (Map.Entry<String, List<Provider>> entry : onlyold.entrySet()) {
            ProviderListener listener = notifyListeners.get(entry.getKey());
            if (listener != null) {
                listener.removeProvider(entry.getValue());
            }
        }

        // 新旧都有，而且有变化
        Map<String, ValueDifference<List<Provider>>> changed = difference.entriesDiffering();
        for (Map.Entry<String, ValueDifference<List<Provider>>> entry : changed.entrySet()) {
            LOGGER.debug("{} has differente", entry.getKey());
            ValueDifference<List<Provider>> differentValue = entry.getValue();
            List<Provider> inold = differentValue.leftValue();
            List<Provider> innew = differentValue.rightValue();
            LOGGER.debug("old(left)  is {}", inold);
            LOGGER.debug("new(right) is {}", innew);
            ListDifference<Provider> listDifference = new ListDifference<Provider>(inold, innew);
            List<Provider> adds = listDifference.getOnlyOnRight();
            List<Provider> removeds = listDifference.getOnlyOnLeft();
            // List<Provider> sames = listDifference.getOnBoth();
            LOGGER.debug("add     :{}", adds);
            LOGGER.debug("removed :{}", removeds);
            // 只通知变化部分内容
            ProviderListener listener = notifyListeners.get(entry.getKey());
            if (listener != null) {
                listener.addProvider(adds);
                listener.removeProvider(removeds);
            }
        }
    }

    /**
     * 改变订阅行为，设置为false则不扫描备份文件变化进行错误
     *
     * @param newsubscribe
     *         the subscribe 是否通知变化
     */
    protected synchronized void switchSubscribe(boolean newsubscribe) {
        if (!this.subscribe && newsubscribe) { // 从不订阅改为订阅
            LOGGER.info("File Registry change to subscribe !");
            if(this.lastLoadTime == 0){ // 加载一次
                this.lastLoadTime = FileRegistryHelper.loadBackupFileToCache(address, memoryCache);
            }
        }
        // 从订阅改为不订阅
        else if (this.subscribe && !newsubscribe) {
            LOGGER.info("File Registry change to unsubscribe !");
        }
        // 切换状态
        this.subscribe = newsubscribe;
    }

    /**
     * 增加服务节点，要求备份
     *
     * @param config the config
     * @param addProviders the add providers
     */
    protected void addProvider(ConsumerConfig config, List<Provider> addProviders) {
        String key = FileRegistryHelper.buildKey(config);
        List<Provider> olds = memoryCache.get(key);
        if (olds != null) {
            HashSet<Provider> set = new HashSet<Provider>(olds);
            set.addAll(addProviders);
            memoryCache.put(key, new ArrayList<Provider>(set));
        } else {
            memoryCache.put(key, addProviders);
        }
        // 备份到文件 改为定时写
        needBackup = true;
        if(subscribe){
            ProviderListener listener = notifyListeners.get(key);
            if (listener != null) {
                listener.addProvider(addProviders);
            }
        }

    }

    /**
     * 删除服务节点，要求备份
     *
     * @param config the config
     * @param delProviders the del providers
     */
    protected void delProvider(ConsumerConfig config, List<Provider> delProviders){
        String key = FileRegistryHelper.buildKey(config);
        List<Provider> olds = memoryCache.get(key);
        if (olds != null) {
            HashSet<Provider> set = new HashSet<Provider>(olds);
            set.removeAll(delProviders);
            memoryCache.put(key, new ArrayList<Provider>(set));
        } else {
            memoryCache.put(key, delProviders);
        }
        // 备份到文件 改为定时写
        needBackup = true;
        if(subscribe){
            ProviderListener listener = notifyListeners.get(key);
            if (listener != null) {
                listener.removeProvider(delProviders);
            }
        }
    }

    /**
     * 更新服务节点，要求备份
     *
     * @param config the config
     * @param newProviders the new providers
     */
    protected void updateProvider(ConsumerConfig config, List<Provider> newProviders){
        String key = FileRegistryHelper.buildKey(config);
        memoryCache.put(key, newProviders == null? new ArrayList<Provider>() : newProviders);
        // 备份到文件 改为定时写
        needBackup = true;
        if(subscribe){
            ProviderListener listener = notifyListeners.get(key);
            if (listener != null) {
                listener.updateProvider(newProviders);
            }
        }
    }

    /**
     * Register void.
     *
     * @param providerConfig the provider config
     * @param configListener the config listener
     */
    @Override
    public void register(ProviderConfig providerConfig, ConfigListener configListener) {
        // 文件注册中心不支持，忽略
    }


    /**
     * Unregister void.
     *
     * @param config the config
     */
    @Override
    public void unregister(ProviderConfig config) {
        // 文件注册中心不支持，忽略
    }


    /**
     * Subscribe list.
     *
     * @param config the config
     * @param providerListener the provider listener
     * @param configListener the config listener
     * @return the list
     */
    @Override
    public List<Provider> subscribe(ConsumerConfig config, ProviderListener providerListener,
                                    ConfigListener configListener) {
        String key = FileRegistryHelper.buildKey(config);
                    // 注册订阅关系，监听文件修改变化
        notifyListeners.put(key, providerListener);
        // 返回已经加载到内存的列表（可能不是最新的，
        return memoryCache.get(key);
    }

    /**
     * Unsubscribe void.
     *
     * @param config the config
     */
    @Override
    public void unsubscribe(ConsumerConfig config) {
        String key = FileRegistryHelper.buildKey(config);
        // 取消注册订阅关系，监听文件修改变化
        notifyListeners.remove(key);
    }

    /**
     * Destroy void.
     */
    @Override
    public void destroy() {
        // 销毁前备份一下
        FileRegistryHelper.backup(address, memoryCache);
        try {
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
                scheduledExecutorService = null;
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
    }
}