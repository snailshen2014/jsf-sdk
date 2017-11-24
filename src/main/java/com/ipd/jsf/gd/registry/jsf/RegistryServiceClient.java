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
package com.ipd.jsf.gd.registry.jsf;

import com.ipd.jsf.gd.client.Client;
import com.ipd.jsf.gd.client.TransportResettableClient;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.RegistryConfig;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.Heartbeat;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.ConcurrentHashSet;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.JSFLogicSwitch;
import com.ipd.jsf.gd.util.ScheduledService;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.vo.HbResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: 注册中心适配器<br>
 * <p/>
 * Description: 维护注册中心的重连,心跳等，使用者无需关系注册中心的可用情况<br>
 * <p/>
 */
public class RegistryServiceClient implements RegistryService {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistryServiceClient.class);

    /**
     * slf4j Logger for this class
     */
    private final static JSFLogger JSFLOGGER = JSFLoggerFactory.getLogger(RegistryServiceClient.class);
    /**
     * The Config.
     */
    private final RegistryConfig config;
    /**
     * 内置的RegistryService，指向一组远程RPC服务
     */
    private RegistryService registryService;
    /**
     * 当前引用的注册中心实例
     */
    private ConsumerConfig<RegistryService> currentConsumerConfig;
    /**
     * 注册中心地址列表
     */
    private List<String> registryAddressList;

    /**
     * 是否可用
     */
    private volatile boolean available = false;

    /**
     * 是否需要检查index
     */
    private boolean needCheckIndex;

    /**
     * 构造函数
     *
     * @param config
     *         RegistryConfig配置
     */
    public RegistryServiceClient(RegistryConfig config) {
        this.config = config;
        String registryAddress = getRegistryAddress(); // 注册中心实际ip:port;ip:port
        LOGGER.info("Final registry address is :{}", registryAddress);

        // 保存到缓存
        JSFContext.putIfAbsent(JSFContext.KEY_REGISTRY_CONFIG, registryAddress);
        registryAddressList = Arrays.asList(registryAddress.split(","));
    }

    private String getRegistryAddress() {
        String address = config.getAddress();
        if (StringUtils.isNotBlank(address)) { // 自己配置了地址，地址优先
            JSFRegistryHelper.writeAddressToFile(address);
            return address;
        } else {
            // 去文件里找
            String addressFromFile = JSFRegistryHelper.getAddressFromFile();
            if (StringUtils.isNotBlank(addressFromFile)) {
                return addressFromFile;
            }
            // 地址没有，备份文件也没有
            throw new IllegalConfigureException(21101, "registry.address", config.getAddress(),
                    "Can not get registry address from configed address and backup file");
        }
    }

    /**
     * 建立与注册中心连接
     *
     * @return 是否成功
     */
    public boolean connect() {
        // 建立reference
        buildRegistryRef(null);
        // 开启守护线程（包含心跳，重试，定时扫描等）
        // 不主动开启，等连接事件调用开始
        //startDaemonThread();
        return available;
    }

    @Override
    public JsfUrl doRegister(JsfUrl jsfUrl) throws RpcException {
        try {
            checkConnection();
            JsfUrl returnUrl = registryService.doRegister(jsfUrl);
            String insKey = returnUrl.getInsKey();
            if (insKey != null) {
                JSFContext.putIfAbsent(JSFContext.KEY_INSTANCEKEY, insKey);
            }
            registered_urls.add(jsfUrl); // 成功记录下
            jsfUrl.getAttrs().put("re-reg", "true"); // 下次将代表重新注册
            return returnUrl;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_register_urls.add(jsfUrl); // 失败记录下
            throw t;
        }
    }

    @Override
    public List<JsfUrl> doRegisterList(List<JsfUrl> jsfUrlList) throws RpcException {
        try {
            checkConnection();
            List<JsfUrl> returnUrls = registryService.doRegisterList(jsfUrlList);
            if (CommonUtils.isNotEmpty(returnUrls)) {
                String insKey = returnUrls.get(0).getInsKey();
                if (insKey != null) {
                    JSFContext.putIfAbsent(JSFContext.KEY_INSTANCEKEY, insKey);
                }
            }
            registered_urls.addAll(jsfUrlList); // 成功记录下
            for (JsfUrl jsfUrl : jsfUrlList) {
                jsfUrl.getAttrs().put("re-reg", "true"); // 下次将代表重新注册
            }
            return returnUrls;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_register_urls.addAll(jsfUrlList); // 失败记录下
            throw t;
        }
    }

    @Override
    public boolean doCheckRegister(JsfUrl jsfUrl) throws RpcException {
        checkConnection();
        return registryService.doCheckRegister(jsfUrl);
    }

    @Override
    public boolean doUnRegister(JsfUrl jsfUrl) throws RpcException {
        try {
            checkConnection();
            boolean returnUrl = registryService.doUnRegister(jsfUrl);
            return returnUrl;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_unregister_urls.add(jsfUrl);
            throw t;
        } finally {
            registered_urls.remove(jsfUrl);
            JSFRegistryHelper.removeConfigDataVersion(jsfUrl);
        }
    }

    @Override
    public boolean doCheckUnRegister(JsfUrl jsfUrl) throws RpcException {
        checkConnection();
        return registryService.doCheckUnRegister(jsfUrl);
    }

    @Override
    public boolean doUnRegisterList(List<JsfUrl> jsfUrlList) throws RpcException {
        try {
            checkConnection();
            boolean returnUrl = registryService.doUnRegisterList(jsfUrlList);
            return returnUrl;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_unregister_urls.addAll(jsfUrlList);
            throw t;
        } finally {
            registered_urls.removeAll(jsfUrlList);
            JSFRegistryHelper.removeConfigDataVersions(jsfUrlList);
        }
    }

    @Override
    public SubscribeUrl doSubscribe(JsfUrl url, Callback subscribeData) throws RpcException {
        try {
            checkConnection();
            SubscribeUrl returnUrl = registryService.doSubscribe(url, subscribeData);
            subscribed_urls.put(url, subscribeData);
            JSFRegistryHelper.updateProviderDataVersion(url, returnUrl.getSourceUrl().getDataVersion());
            return returnUrl;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_subscribe_urls.put(url, subscribeData);
            throw t;
        }
    }

    @Override
    public boolean doUnSubscribe(JsfUrl url) throws RpcException {
        try {
            checkConnection();
            boolean returnUrl = registryService.doUnSubscribe(url);
            return returnUrl;
        } catch (RpcException t) { // 忽略所有异常，等待下次重试
            failed_unsubscribe_urls.add(url);
            throw t;
        } finally {
            subscribed_urls.remove(url);
            JSFRegistryHelper.removeProviderDataVersion(url);
            JSFRegistryHelper.removeConfigDataVersion(url);
        }
    }

    @Override
    public SubscribeUrl lookup(JsfUrl jsfUrl) throws RpcException {
        checkConnection();
        return registryService.lookup(jsfUrl);
    }

    @Override
    public List<SubscribeUrl> lookupList(List<JsfUrl> list) throws RpcException {
        checkConnection();
        return registryService.lookupList(list);
    }

    @Override
    public HbResult doHeartbeat(Heartbeat heartbeat) throws RpcException {
        checkConnection();
        return registryService.doHeartbeat(heartbeat);
    }

    @Override
    public JsfUrl subscribeConfig(JsfUrl url, Callback subscribeData) throws RpcException {
        try {
            checkConnection();
            JsfUrl returnUrl = registryService.subscribeConfig(url, subscribeData);
            configed_urls.put(url, subscribeData);
            JSFRegistryHelper.updateConfigDataVersion(url, returnUrl != null ? returnUrl.getDataVersion() : 0);
            return returnUrl;
        } catch (RpcException t) {
            failed_configed_urls.put(url, subscribeData);
            throw t;
        }
    }

    public void unsubscribeConfig(JsfUrl url) throws RpcException {
        try {
            Callback callback = configed_urls.remove(url);
            if (callback == null) {
                failed_configed_urls.remove(url);
            }
        } catch (RpcException t) {
            throw t;
        }
    }

    @Override
    public JsfUrl getConfig(JsfUrl jsfUrl) throws RpcException {
        checkConnection();
        return registryService.getConfig(jsfUrl);
    }

    @Override
    public List<JsfUrl> getConfigList(List<JsfUrl> list) throws RpcException {
        checkConnection();
        return registryService.getConfigList(list);
    }

    /**
     * 心跳定时器线程 + 定时重试失败服务
     */
    private ScheduledService retryScheduledService;

    /**
     * 定时查询线程
     */
    private ScheduledService checkerScheduledService;

    /**
     * 重试+心跳的间隔
     */
    private int retryPeriod;

    /**
     * Start daemon thread.
     */
    public void startDaemonThread() {
        retryPeriod = Math.max(5000, CommonUtils.parseInt(
                JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_HEARTBEAT_INTERVAL, null), 30000));
        int checkPeriod = Math.max(30000, CommonUtils.parseInt(
                JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_CHECK_INTERVAL, null), 300000));
        // 启动心跳线程  二合一线程 重试+心跳
        // 定时重试线程，实现Failback
        if (retryScheduledService == null) {
            retryScheduledService = new ScheduledService("JSF-jsfRegistry-HB&Retry",
                    ScheduledService.MODE_FIXEDDELAY,
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                checkAndBuildConnection();
                                sendHeartBeat();
                                retry();
                            } catch (Throwable e) {
                                LOGGER.error("Error when send heartbeat and retry", e);
                            }
                        }

                    }, //定时load任务
                    retryPeriod, // 延迟一个周期
                    retryPeriod, // 一个周期循环
                    TimeUnit.MILLISECONDS
            ).start();
        }
        if (checkerScheduledService == null) {
            // 增加定时扫描线程
            checkerScheduledService = new ScheduledService("JSF-jsfRegistry-Check",
                    ScheduledService.MODE_FIXEDRATE,
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                checkAndBuildConnection();
                                check();
                            } catch (Throwable e) {
                                LOGGER.error("Error when check data version with registry", e);
                            }
                        }
                    },
                    checkPeriod, // 延迟一个周期
                    checkPeriod, // 一个周期循环
                    TimeUnit.MILLISECONDS
            ).start();
        }
    }

    /**
     * 心跳服务
     */
    private void sendHeartBeat() {
        try {
            Heartbeat heartbeat = new Heartbeat();
            String instanceKey = (String) JSFContext.get(JSFContext.KEY_INSTANCEKEY);
            if (StringUtils.isEmpty(instanceKey)) { // instance_key 为空
                return;
            }
            heartbeat.setInsKey(instanceKey);
            if (registered_urls.isEmpty()) { // 没任何注册，注册中心可能没有实例信息
                HashMap<String, String> config = new HashMap<String, String>();
                config.put("uncheck", "true"); // 就不检查了
                heartbeat.setConfig(config);
            }
            HbResult result = doHeartbeat(heartbeat);
            JSFLOGGER.info("Send heartbeat to {} return :{}", currentConsumerConfig.getUrl(), result);
            JSFContext.put(JSFContext.KEY_LAST_HEARTBEAT_TIME, JSFContext.systemClock.now()); // 记录最后心跳时间
            List<String> config = result.getConfig();
            if (CommonUtils.isNotEmpty(config)) {
                if (config.contains("recover")) {
                    // 如果注册中心带回标记要求重新注册和订阅（recover）
                    // 用于长时间和注册中心断开连接后，注册中心已经删除本实例信息
                    LOGGER.info("Call for recover from registry.");
                    recover();
                } else if (config.contains("callback")) {
                    // 如果注册中心带回标记要求重新注册callback
                    // 则重新注册和订阅下
                    LOGGER.info("Call for re-register callback from registry.");
                    recover();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Send heartbeat to " + currentConsumerConfig.getUrl() + " error", e);
        }
    }

    /**
     * 已注册的urls
     */
    private final ConcurrentHashSet<JsfUrl> registered_urls = new ConcurrentHashSet<JsfUrl>();
    /**
     * 已订阅服务列表的urls
     */
    private final ConcurrentHashMap<JsfUrl, Callback> subscribed_urls = new ConcurrentHashMap<JsfUrl, Callback>();
    /**
     * 已订阅配置的urls
     */
    private final ConcurrentHashMap<JsfUrl, Callback> configed_urls = new ConcurrentHashMap<JsfUrl, Callback>();
    /**
     * 失败的注册的urls
     */
    private final ConcurrentHashSet<JsfUrl> failed_register_urls = new ConcurrentHashSet<JsfUrl>();
    /**
     * 失败的取消注册的urls
     */
    private final ConcurrentHashSet<JsfUrl> failed_unregister_urls = new ConcurrentHashSet<JsfUrl>();
    /**
     * 失败的订阅的urls
     */
    private final ConcurrentHashMap<JsfUrl, Callback> failed_subscribe_urls = new ConcurrentHashMap<JsfUrl, Callback>();
    /**
     * 失败的取消订阅的urls
     */
    private final ConcurrentHashSet<JsfUrl> failed_unsubscribe_urls = new ConcurrentHashSet<JsfUrl>();
    /**
     * 失败的订阅的配置urls
     */
    private final ConcurrentHashMap<JsfUrl, Callback> failed_configed_urls = new ConcurrentHashMap<JsfUrl, Callback>();
    /**
     * 上次recover调用时间
     */
    private long last_recover_time = 0;

    /**
     * 恢复服务（包括注册和订阅等动作）
     */
    public synchronized void recover() {
        // 比较上次时间， 大于四个心跳时间才能重新注册
        long now = JSFContext.systemClock.now();
        if (last_recover_time > 0 && ((now - last_recover_time) < retryPeriod * 6)) {
            LOGGER.info("Recover action has been canceled cause by called in last 6*{} ms", retryPeriod);
            return;
        } else {
            last_recover_time = now;
        }
        // 重新订阅配置
        if (!configed_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> succeeded = new ConcurrentHashMap<JsfUrl, Callback>(configed_urls);
            if (succeeded.size() > 0) {
                LOGGER.info("Recover config subscribe : {}", succeeded);
                for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                    JsfUrl url = entry.getKey();
                    Callback callback = entry.getValue();
                    try {
                        subscribeConfig(url, callback);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        LOGGER.warn("Failed to recover config subscribe " + succeeded +
                                ", waiting for again, cause: " + t.getMessage(), t);
                        configed_urls.remove(url);
                        failed_configed_urls.put(url, callback);
                    }
                }
            }
        }
        // 重新注册服务端和调用端
        if (!registered_urls.isEmpty()) {
            List<JsfUrl> succeeded = new ArrayList<JsfUrl>(registered_urls);
            if (succeeded.size() > 0) {
                if (JSFLogicSwitch.REGISTRY_REGISTER_BATCH) {
                    LOGGER.info("Recover register batch: {}", succeeded);
                    try {
                        registered_urls.clear();
                        doRegisterList(succeeded);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        LOGGER.warn("Failed to recover register " + succeeded +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                } else {
                    LOGGER.info("Recover register : {}", succeeded);
                    for (JsfUrl url : succeeded) {
                        try {
                            doRegister(url);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            LOGGER.warn("Failed to recover register " + succeeded +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                            registered_urls.remove(url);
                            failed_register_urls.add(url);
                        }
                    }
                }
            }
        }
        // 重新订阅调用端
        if (!subscribed_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> succeeded = new ConcurrentHashMap<JsfUrl, Callback>(subscribed_urls);
            if (succeeded.size() > 0) {
                LOGGER.info("Recover subscribe : {}", succeeded);
                for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                    JsfUrl url = entry.getKey();
                    Callback callback = entry.getValue();
                    try {
                        doSubscribe(url, callback);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        LOGGER.warn("Failed to recover subscribe " + succeeded +
                                ", waiting for again, cause: " + t.getMessage(), t);
                        subscribed_urls.remove(url);
                        failed_subscribe_urls.put(url, callback);
                    }
                }
            }
        }
    }

    /**
     * 连续失败次数
     */
    private AtomicInteger failed_counter = new AtomicInteger(0);

    /**
     * 重试失败请求.
     */
    private void retry() {
        if (!failed_configed_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> failed = new ConcurrentHashMap<JsfUrl, Callback>(failed_configed_urls);
            if (failed.size() > 0) {
                LOGGER.info("Retry config subscribe : {}", failed);
                for (Map.Entry<JsfUrl, Callback> entry : failed.entrySet()) {
                    JsfUrl url = entry.getKey();
                    Callback callback = entry.getValue();
                    try {
                        failed_configed_urls.remove(url);
                        subscribeConfig(url, callback);
                        failed_counter.set(0);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        failed_counter.incrementAndGet();
                        LOGGER.warn("Failed to retry subscribe " + failed +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
        if (!failed_register_urls.isEmpty()) {
            List<JsfUrl> failed = new ArrayList<JsfUrl>(failed_register_urls);
            if (failed.size() > 0) {
                if (JSFLogicSwitch.REGISTRY_REGISTER_BATCH) {
                    LOGGER.info("Retry register batch : {}", failed);
                    try {
                        failed_register_urls.clear();
                        doRegisterList(failed);
                        failed_counter.set(0);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        failed_counter.incrementAndGet();
                        LOGGER.warn("Failed to retry register " + failed +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                } else {
                    LOGGER.info("Retry register : {}", failed);
                    for (JsfUrl url : failed) {
                        try {
                            failed_register_urls.remove(url);
                            doRegister(url);
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            failed_counter.incrementAndGet();
                            LOGGER.warn("Failed to retry register " + failed +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                }
            }
        }
        if (!failed_unregister_urls.isEmpty()) {
            List<JsfUrl> failed = new ArrayList<JsfUrl>(failed_unregister_urls);
            if (failed.size() > 0) {
                if (JSFLogicSwitch.REGISTRY_REGISTER_BATCH) {
                    LOGGER.info("Retry unregister batch : {}", failed);
                    try {
                        failed_unregister_urls.clear();
                        doUnRegisterList(failed);
                        failed_counter.set(0);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        failed_counter.incrementAndGet();
                        LOGGER.warn("Failed to retry unregister " + failed +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                } else {
                    LOGGER.info("Retry unregister : {}", failed);
                    for (JsfUrl url : failed) {
                        try {
                            failed_unregister_urls.remove(url);
                            doUnRegister(url);
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            failed_counter.incrementAndGet();
                            LOGGER.warn("Failed to retry unregister " + failed +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                }
            }
        }
        if (!failed_subscribe_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> failed = new ConcurrentHashMap<JsfUrl, Callback>(failed_subscribe_urls);
            if (failed.size() > 0) {
                LOGGER.info("Retry subscribe : {}", failed);
                for (Map.Entry<JsfUrl, Callback> entry : failed.entrySet()) {
                    JsfUrl url = entry.getKey();
                    Callback callback = entry.getValue();
                    try {
                        failed_subscribe_urls.remove(url);
                        String urkInsKey = url.getInsKey();
                        if (urkInsKey == null) { // 如果没有insKey设置一个
                            String insKey = (String) JSFContext.get(JSFContext.KEY_INSTANCEKEY);
                            if (insKey != null) {
                                url.setInsKey(insKey);
                            }
                        }
                        SubscribeUrl subscribeUrl = doSubscribe(url, callback);
                        if (subscribeUrl != null) { // 通知客户端
                            callback.notify(subscribeUrl);
                        }
                        failed_counter.set(0);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        failed_counter.incrementAndGet();
                        LOGGER.warn("Failed to retry subscribe " + failed +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
        if (!failed_unsubscribe_urls.isEmpty()) {
            ConcurrentHashSet<JsfUrl> failed = new ConcurrentHashSet<JsfUrl>(failed_unsubscribe_urls);
            if (failed.size() > 0) {
                LOGGER.info("Retry unsubscribe : {}", failed);
                for (JsfUrl url : failed) {
                    try {
                        failed_unsubscribe_urls.remove(url);
                        String urkInsKey = url.getInsKey();
                        if (urkInsKey == null) { // 如果没有insKey设置一个
                            String insKey = (String) JSFContext.get(JSFContext.KEY_INSTANCEKEY);
                            if (insKey != null) {
                                url.setInsKey(insKey);
                            }
                        }
                        doUnSubscribe(url);
                        failed_counter.set(0);
                    } catch (Throwable t) { // 忽略所有异常，等待下次重试
                        failed_counter.incrementAndGet();
                        LOGGER.warn("Failed to retry unsubscribe " + failed +
                                ", waiting for again, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    /**
     * 定时检查服务
     */
    private void check() {
        int batch = config.getBatchCheck();
        if (!subscribed_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> succeeded = new ConcurrentHashMap<JsfUrl, Callback>(subscribed_urls);
            if (succeeded.size() > 0) {
                LOGGER.info("Check subscribe : {}", succeeded);
                if (batch > 1) { // 采用批量检查提高性能
                    List<JsfUrl> allUrls = new ArrayList<JsfUrl>();
                    for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                        JsfUrl url = entry.getKey();
                        long currentDv = JSFRegistryHelper.getProviderDataVersion(url);
                        url.setDataVersion(currentDv); // 设置当前版本
                        allUrls.add(url);
                    }
                    int bs = allUrls.size() % batch == 0 ? allUrls.size() / batch : allUrls.size() / batch + 1;
                    for (int i = 0; i < bs; i++) {
                        List<JsfUrl> urls = new ArrayList<JsfUrl>(
                                allUrls.subList(i * batch, (i == bs - 1) ? allUrls.size() : ((i + 1) * batch)));
                        try {
                            List<SubscribeUrl> subscribeUrls = registryService.lookupList(urls);
                            for (int j = 0; j < subscribeUrls.size(); j++) {
                                SubscribeUrl subscribeUrl = subscribeUrls.get(j);
                                JsfUrl sourceUrl = urls.get(j);
                                long currentDv = JSFRegistryHelper.getProviderDataVersion(sourceUrl);
                                if (subscribeUrl.getSourceUrl().getDataVersion() > currentDv) {
                                    succeeded.get(sourceUrl).notify(subscribeUrl);
                                }
                            }
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            LOGGER.warn("Failed to check subscribe " + succeeded +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                            failed_counter.incrementAndGet();
                        }
                    }
                } else {
                    for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                        JsfUrl url = entry.getKey();
                        Callback callback = entry.getValue();
                        try {
                            long currentDv = JSFRegistryHelper.getProviderDataVersion(url);
                            url.setDataVersion(currentDv); // 设置当前版本
                            SubscribeUrl subscribeUrl = registryService.lookup(url);
                            if (subscribeUrl.getSourceUrl().getDataVersion() > currentDv) {
                                callback.notify(subscribeUrl);
                            }
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            LOGGER.warn("Failed to check subscribe " + succeeded +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                            failed_counter.incrementAndGet();
                        }
                    }
                }
            }
        }
        if (!configed_urls.isEmpty()) {
            ConcurrentHashMap<JsfUrl, Callback> succeeded = new ConcurrentHashMap<JsfUrl, Callback>(configed_urls);
            if (succeeded.size() > 0) {
                LOGGER.info("Check config subscribe : {}", succeeded);
                if (batch > 1) { // 采用批量检查提高性能
                    List<JsfUrl> allUrls = new ArrayList<JsfUrl>();
                    for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                        JsfUrl url = entry.getKey();
                        long currentDv = JSFRegistryHelper.getConfigDataVersion(url);
                        url.setDataVersion(currentDv); // 设置当前版本
                        allUrls.add(url);
                    }
                    int bs = allUrls.size() % batch == 0 ? allUrls.size() / batch : allUrls.size() / batch + 1;
                    for (int i = 0; i < bs; i++) {
                        List<JsfUrl> urls = new ArrayList<JsfUrl>(
                                allUrls.subList(i * batch, (i == bs - 1) ? allUrls.size() : ((i + 1) * batch)));
                        try {
                            List<JsfUrl> regJsUrls = registryService.getConfigList(urls);
                            for (int j = 0; j < regJsUrls.size(); j++) {
                                JsfUrl regJsfUrl = regJsUrls.get(j);
                                JsfUrl sourceUrl = urls.get(j);
                                long currentDv = JSFRegistryHelper.getConfigDataVersion(sourceUrl);
                                // 注册中心的比本地新
                                if (regJsfUrl.getDataVersion() > currentDv) {
                                    SubscribeUrl event = new SubscribeUrl();
                                    event.setType(SubscribeUrl.CONFIG_UPDATE);
                                    event.setSourceUrl(sourceUrl);
                                    event.setConfig(regJsfUrl.getAttrs());
                                    succeeded.get(sourceUrl).notify(event);
                                }
                            }
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            LOGGER.warn("Failed to check config subscribe " + succeeded +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                            failed_counter.incrementAndGet();
                        }
                    }
                } else {
                    for (Map.Entry<JsfUrl, Callback> entry : succeeded.entrySet()) {
                        JsfUrl url = entry.getKey();
                        Callback callback = entry.getValue();
                        try {
                            long currentDv = JSFRegistryHelper.getConfigDataVersion(url);
                            url.setDataVersion(currentDv); // 设置当前版本
                            JsfUrl regJsfUrl = registryService.getConfig(url);
                            // 注册中心的比本地新
                            if (regJsfUrl.getDataVersion() > currentDv) {
                                SubscribeUrl event = new SubscribeUrl();
                                event.setType(SubscribeUrl.CONFIG_UPDATE);
                                event.setSourceUrl(url);
                                event.setConfig(regJsfUrl.getAttrs());
                                callback.notify(event);
                            }
                            failed_counter.set(0);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            LOGGER.warn("Failed to check config subscribe " + succeeded +
                                    ", waiting for again, cause: " + t.getMessage(), t);
                            failed_counter.incrementAndGet();
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查连接是否可以，如果状态变化，通知相应的Lister<br>
     * 如果有问题，则重建
     */
    private void checkAndBuildConnection() {
        if(failed_counter.get() >= 3 && registryAddressList.size() > 1) {
            reconnect();
        }
        if (available && !isAvailable()) {
            // 从可用变成变成不可用
            if (disconnectListener != null) {
                last_recover_time = 0; // 掉线可以重新recover，不受间隔限制
                JSFContext.put(JSFContext.KEY_CONNECTED_REGISTRY, "");
                if (disconnectListener != null) { // 掉线通知Listener
                    disconnectListener.notifyListener();
                }
            }
            available = false;
            LOGGER.warn("Need to reconnect registry:{} clientAvailable:{}", available,
                    currentConsumerConfig.getClient().isAvailable());
        }
        if (!available) { // 不可用，重连注册中心
            if (currentConsumerConfig == null) { // 启动就没连上
                buildRegistryRef(null);
            } else {
                if (registryAddressList.size() == 1) { // 一个注册中心，就检查自己的
                    Client client = currentConsumerConfig.getClient();
                    if (client != null && client.isAvailable()) {
                        available = true; // 不可用变成可用
                        JSFContext.put(JSFContext.KEY_CONNECTED_REGISTRY, registryAddressList.get(0));
                        if (reconnectListener != null) { // 重连上通知Listener
                            reconnectListener.notifyListener();
                        }
                    }
                } else if (registryAddressList.size() > 1) { // 多个注册中心实例，则重试其它的
                    buildRegistryRef(currentConsumerConfig.getUrl());
                } else {
                    LOGGER.error("Registry address list can not LESS than 1!");
                }
            }
        }
    }

    /**
     * 检查连接是否可用
     */
    private void checkConnection() {
        if (!available) {
            failed_counter.incrementAndGet(); // TODO 是否切换
            throw new RpcException("Registry is not available now! Current registry address list is " +
                    registryAddressList);
        }
    }

    private ConsumerConfig<RegistryService> buildConfig(String registryAddress) {
        ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
        consumerConfig.setUrl(registryAddress);
        consumerConfig.setInterfaceId(RegistryService.class.getCanonicalName());
        consumerConfig.setAlias("reg");
        consumerConfig.setCluster(Constants.CLUSTER_TRANSPORT_RESETTABLE);
        consumerConfig.setRegister(false);
        consumerConfig.setCheck(false);
        consumerConfig.setSubscribe(false);
        consumerConfig.setRetries(0);
        consumerConfig.setTimeout(config.getTimeout());
        consumerConfig.setConnectTimeout(config.getConnectTimeout());
        consumerConfig.setParameter(Constants.HIDDEN_KEY_MONITOR, "false");
        consumerConfig.setParameter(Constants.HIDDEN_KEY_DESTROY, "false");
        consumerConfig.setParameter(Constants.HIDDEN_KEY_WARNNING, "false");
        consumerConfig.setReconnect(15000); // 重连间隔
        consumerConfig.setHeartbeat(0); // 不发心跳了
        return consumerConfig;
    }

    /**
     * 建立连接，同时只能一个线程进行重建
     *
     * @param oldAddr
     *         旧地址（将会被放到列表最后）
     * @return 是否成功
     */
    private synchronized void buildRegistryRef(String oldAddr) {
        try {
            List<String> addressList = new ArrayList<String>(registryAddressList);
            if (StringUtils.isNotBlank(oldAddr)) {
                addressList.remove(oldAddr);
                addressList.add(oldAddr); // 旧地址排最后面
            }

            for (String newaddr : addressList) { // 循环连接，一个一个地址试
                try {
                    TransportResettableClient client;
                    if (currentConsumerConfig == null) { // 第一次连接生成代理
                        currentConsumerConfig = buildConfig(newaddr);
                        // 睡个1s内的随机数，防止一个注册中心重启时，所有请求瞬间打向第二个注册中心
                        try {
                            Thread.sleep(new Random().nextInt(1000));
                        } catch (Exception e) {
                        }
                        registryService = currentConsumerConfig.refer();
                        client = (TransportResettableClient) currentConsumerConfig.getClient();
                    } else { // 不是第一次切换地址
                        client = (TransportResettableClient) currentConsumerConfig.getClient();
                        client.resetTransport(newaddr);
                    }
                    if (client.isAvailable()) {
                        available = true;
                        JSFContext.put(JSFContext.KEY_CONNECTED_REGISTRY, newaddr);
                        currentConsumerConfig.setUrl(newaddr); // 设置新的地址
                        if (StringUtils.isNotBlank(oldAddr)) {
                            LOGGER.debug("registry reconnected to {}", newaddr);
                            if (reconnectListener != null) { // 重连上通知Listener
                                reconnectListener.notifyListener();
                            }
                        } else { // 第一次连接
                            LOGGER.debug("registry connected to {}", newaddr);
                            if (connectListener != null) { // 第一次连上通知Listener
                                connectListener.notifyListener();
                            }
                        }
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            LOGGER.info("Build registry reference end...");
        }
    }

    private RegistryStatListener connectListener;
    private RegistryStatListener disconnectListener;
    private RegistryStatListener reconnectListener;

    /**
     * On connect.
     *
     * @param listener
     *         the registry service listener
     */
    public void onConnect(RegistryStatListener listener) {
        this.connectListener = listener;
    }

    /**
     * On disconnect.
     *
     * @param listener
     *         the registry service listener
     */
    public void onDisconnect(RegistryStatListener listener) {
        this.disconnectListener = listener;
    }

    /**
     * On reconnect.
     *
     * @param listener
     *         the registry service listener
     */
    public void onReconnect(RegistryStatListener listener) {
        this.reconnectListener = listener;
    }

    /**
     * 重连另外一个注册中心
     */
    public void reconnect() {
        if (currentConsumerConfig == null) {
            last_recover_time = 0;
            buildRegistryRef(null);
        } else {
            String oldaddr = currentConsumerConfig.getUrl();
            TransportResettableClient client = (TransportResettableClient) currentConsumerConfig.getClient();
            if (client != null) {
                client.closeTransports(); // 先关
            }
            last_recover_time = 0;
            buildRegistryRef(oldaddr); // 再连
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        // 销毁各个线程
        try {
            if (retryScheduledService != null) {
                retryScheduledService.shutdown();
                retryScheduledService = null;
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        try {
            if (checkerScheduledService != null) {
                checkerScheduledService.shutdown();
                checkerScheduledService = null;
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        // 销毁reference
        currentConsumerConfig.unrefer();
    }

    /**
     * 实际连接是否可用（如果一个节点长连接建立，确联系业务失败多次，也要求切换）
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return currentConsumerConfig != null
                && currentConsumerConfig.getClient() != null
                && currentConsumerConfig.getClient().isAvailable();
    }

    /**
     * 批量反注册
     */
    public void batchUnregister() {
        if (JSFLogicSwitch.REGISTRY_REGISTER_BATCH && registered_urls.size() > 0) {
            try {
                LOGGER.info("Batch unregister: {}", registered_urls);
                doUnRegisterList(new ArrayList<JsfUrl>(registered_urls));
            } catch (Throwable t) { // 忽略所有异常
                LOGGER.warn("Failed to batch unregister, cause: " + t.getMessage(), t);
            }
        }
    }
}