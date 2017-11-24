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

import java.util.List;
import java.util.Map;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.config.RegistryConfig;
import com.ipd.jsf.gd.registry.jsf.JSFRegistryHelper;
import com.ipd.jsf.gd.registry.jsf.RegistryServiceClient;
import com.ipd.jsf.gd.registry.jsf.RegistryStatListener;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.registry.jsf.RegistryCallback;
import com.ipd.jsf.gd.registry.jsf.RegistryConfigListener;
import com.ipd.jsf.gd.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: JSF注册中心<br>
 * <p/>
 * Description: 自主研发的注册中心服务端<br>
 * <p/>
 */
public class JSFRegistry implements ClientRegistry {
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(JSFRegistry.class);

    /**
     * The Config.
     */
    private final RegistryConfig config;
    /**
     * The Callback.
     */
    private final RegistryCallback callback;
    /**
     * The Config Listener 一个注册中心一个
     */
    private final RegistryConfigListener configListener;
    /**
     * The Registry service.
     */
    private final RegistryServiceClient registryClient;
    /**
     * The File registry
     */
    private final FileRegistry fileRegistry;

    /**
     * Instantiates a new JSF registry.
     *
     * @param config
     *         the config
     */
    public JSFRegistry(RegistryConfig config) {
        LOGGER.info("Build jsf registry by {}", config);
        this.config = config;
        this.fileRegistry = new FileRegistry(config, false); // 内置FileRegistry，不运行扫描
        this.registryClient = new RegistryServiceClient(config); // 注册中心适配器（维护注册中心的重连等）
        this.callback = new RegistryCallback();
        callback.setRegistry(this);
        this.configListener = new RegistryConfigListener();

        addRegistryStatListeners(); // 监听注册中心连接变化
        try {
            registryClient.connect();
        } catch (Exception e) {
            LOGGER.error("Init connections of JSFRegistry error", e);
        }
        if (!registryClient.isAvailable()) {
            LOGGER.warn("JSFRegistry client is not available, use file at local");
            Map<String, String> globalConfig = JSFRegistryHelper.readGlobalConfigFromFile();
            if (globalConfig != null) { // 读取本地配置备份文件
                this.configListener.configChanged(globalConfig);
            }
            startDaemonThread();  // 注册中心不可用，提前启动
            this.fileRegistry.switchSubscribe(true);
            Map<String, String> ifaceNameId = JSFRegistryHelper.readInterfaceMapFromFile();
            if (ifaceNameId != null) { // 读取接口名和id映射关系
                for (Map.Entry<String, String> entry : ifaceNameId.entrySet()) {
                    JSFContext.cacheClassNameAndIfaceId(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * 加载配置，开启守护线程（包含心跳，重试，定时扫描等）
     */
    private void startDaemonThread() {
        String filedir = JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_BACKUP_DIR, null);
        fileRegistry.setAddress(filedir);
        registryClient.startDaemonThread(); // 开启守护线程（包含心跳，重试，定时扫描等）
    }

    @Override
    public void register(ProviderConfig providerConfig, ConfigListener configListener) {
        // 一个服务端可能发布到多协议，所以是list
        List<JsfUrl> urls = JSFRegistryHelper.convertProviderToUrl(providerConfig);
        if (urls == null || urls.isEmpty()) {
            return;
        }
        // 注册
        if (providerConfig.isRegister() && config.isRegister()) {
            for (JsfUrl url : urls) {
                try {
                    JsfUrl resultJsfUrl = registryClient.doRegister(url);
                    parseIfaceId(resultJsfUrl, providerConfig);
                    LOGGER.info("Do register success, the url is :{}", url);
                } catch (InitErrorException e) {
                    LOGGER.error("Do register provider error", e);
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Do register provider error", e);
                }
            }
        }
        // 订阅配置变化
        if (providerConfig.isSubscribe() && config.isSubscribe()) {
            JsfUrl url0 = urls.get(0);
            try {
                callback.addConfigListener(providerConfig, this.configListener);
                callback.addConfigListener(providerConfig, configListener);
                JsfUrl cfgUrl = registryClient.subscribeConfig(url0, callback);
                if (cfgUrl != null) {
                    Map<String, String> attrs = cfgUrl.getAttrs();
                    if (attrs != null) {
                        attrs.put(Constants.CONFIG_KEY_INTERFACE, providerConfig.getInterfaceId());
                        if (configListener != null) {
                            configListener.configChanged(attrs);
                        }
                        this.configListener.configChanged(attrs);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Do subscribe provider config error", e);
            }
        }
    }


    /**
     * Unregister void.
     *
     * @param providerConfig
     *         the provider config
     */
    @Override
    public void unregister(ProviderConfig providerConfig) {
        List<JsfUrl> urls = JSFRegistryHelper.convertProviderToUrl(providerConfig);
        if (urls == null || urls.isEmpty()) {
            return;
        }
        // 取消订阅配置变化
        if (providerConfig.isSubscribe() && config.isSubscribe()) {
            try {
                // 减少监听器
                registryClient.unsubscribeConfig(urls.get(0));
                callback.removeConfigListener(providerConfig);
            } catch (Exception e) {
                LOGGER.warn("Do unsubscribe provider error", e);
            }
        }
        // 反注册
        if (providerConfig.isRegister() && config.isRegister()) {
            for (JsfUrl url : urls) {
                try {
                    registryClient.doUnRegister(url);
                    LOGGER.info("Do unregister success, the url is :{}", url);
                } catch (Exception e) {
                    LOGGER.warn("Do unregister provider error", e);
                }
            }
        }
    }

    @Override
    public List<Provider> subscribe(final ConsumerConfig consumerConfig, ProviderListener providerListener,
                                    ConfigListener configListener) {
        JsfUrl url = JSFRegistryHelper.convertConsumerToUrl(consumerConfig);
        // 注册
        if (consumerConfig.isRegister() && config.isRegister()) {
            try {
                JsfUrl resultJsfUrl = registryClient.doRegister(url);
                parseIfaceId(resultJsfUrl, consumerConfig);
            } catch (InitErrorException e) {
                LOGGER.error("Do register consumer error", e);
                throw e;
            } catch (Exception e) {
                LOGGER.warn("Do register consumer error", e);
            }
        }

        List<Provider> providers = null;
        if (consumerConfig.isSubscribe() && config.isSubscribe()) {
            // 订阅配置变化
            try {
                // 增加监听器\
                callback.addConfigListener(consumerConfig, this.configListener);
                callback.addConfigListener(consumerConfig, configListener);
                JsfUrl cfgUrl = registryClient.subscribeConfig(url, callback);
                if (cfgUrl != null) {
                    Map<String, String> attrs = cfgUrl.getAttrs();
                    if (attrs != null) {
                        attrs.put(Constants.CONFIG_KEY_INTERFACE, consumerConfig.getInterfaceId());
                        if (configListener != null) {
                            configListener.configChanged(attrs);
                        }
                        this.configListener.configChanged(attrs);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Do subscribe consumer config error", e);
            }
            // 订阅服务列表
            try {
                // 增加监听器
                callback.addProviderListener(consumerConfig, providerListener);
                callback.addProviderListener(consumerConfig, new ProviderListener() {
                    public void addProvider(List<Provider> providers) {
                        fileRegistry.addProvider(consumerConfig, providers);
                    }

                    public void removeProvider(List<Provider> providers) {
                        fileRegistry.delProvider(consumerConfig, providers);
                    }

                    public void updateProvider(List<Provider> providers) {
                        fileRegistry.updateProvider(consumerConfig, providers);
                    }
                });
                SubscribeUrl subscribeUrl = registryClient.doSubscribe(url, callback);
                if (subscribeUrl != null) {
                    LOGGER.info("Do subscribe success, the url is :{}", url);
                    List<JsfUrl> jsfUrls = subscribeUrl.getProviderList();
                    if (jsfUrls != null && !jsfUrls.isEmpty()) {
                        providers = JSFRegistryHelper.convertUrlToProviders(jsfUrls, consumerConfig.getInterfaceId());
                        fileRegistry.addProvider(consumerConfig, providers);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Do subscribe consumer error", e);
                if (!registryClient.isAvailable()) { // 从文件注册中心读取
                    providers = fileRegistry.subscribe(consumerConfig, providerListener, configListener);
                }
            }
        }
        return providers;
    }

    /**
     * Unsubscribe void.
     *
     * @param consumerConfig
     *         the config
     */
    @Override
    public void unsubscribe(ConsumerConfig consumerConfig) {
        JsfUrl url = JSFRegistryHelper.convertConsumerToUrl(consumerConfig);
        // 反注册
        if (consumerConfig.isRegister() && config.isRegister()) {
            try {
                registryClient.doUnRegister(url);
            } catch (Exception e) {
                LOGGER.warn("Do unregister consumer error", e);
            }
        }
        // 取消订阅服务列表
        if (consumerConfig.isSubscribe() && config.isSubscribe()) {
            try {
                // 取消订阅配置变化
                callback.removeConfigListener(consumerConfig);
                registryClient.unsubscribeConfig(url);
            } catch (Exception e) {
                LOGGER.warn("Do unsubscribe consumer config error", e);
            }
            try {
                // 删除监听器
                callback.removeProviderListener(consumerConfig);
                registryClient.doUnSubscribe(url);
                LOGGER.info("Do unsubscribe success, the url is :{}", url);
            } catch (Exception e) {
                LOGGER.warn("Do unsubscribe consumer error", e);
            }
            try {
                fileRegistry.unsubscribe(consumerConfig);
            } catch (Exception e) {
                LOGGER.warn("Do unsubscribe consumer at file registry error", e);
            }
        }
    }

    /**
     * Destroy void.
     */
    @Override
    public void destroy() {
        // 关闭回调
        callback.stop();
        // 销毁文件注册中心
        fileRegistry.destroy();
        // 关闭重试定时器
        registryClient.destroy();
    }

    /**
     * 增加注册中心状态变化监听器
     */
    private void addRegistryStatListeners() {
        registryClient.onConnect(new RegistryStatListener() {
            @Override
            public void notifyListener() {
                try {
                    fileRegistry.switchSubscribe(false);
                    try {
                        loadGlobalConfig();
                    } catch (Exception e) {
                        LOGGER.info("Load global config from registry error, use file at local.");
                        Map<String, String> globalConfig = JSFRegistryHelper.readGlobalConfigFromFile();
                        if (globalConfig != null) { // 读取本地配置备份文件
                            configListener.configChanged(globalConfig);
                        }
                    }
                    startDaemonThread();
                } catch (Exception e) {
                    LOGGER.warn("Connect listener notify error", e);
                }
            }
        });
        registryClient.onDisconnect(new RegistryStatListener() {
            @Override
            public void notifyListener() {
                try {
                    fileRegistry.switchSubscribe(true);
                } catch (Exception e) {
                    LOGGER.warn("Disconnect listener notify error", e);
                }
            }
        });
        registryClient.onReconnect(new RegistryStatListener() {
            @Override
            public void notifyListener() {
                try {
                    fileRegistry.switchSubscribe(false);
                    registryClient.recover();
                } catch (Exception e) {
                    LOGGER.warn("Reconnect listener notify error", e);
                }
            }
        });
    }

    /**
     * 增加配置监听器
     */
    private void loadGlobalConfig() {
        JsfUrl url = JSFRegistryHelper.buildConfigJsfUrl();
        callback.addGlobalConfigListener(configListener);
        JsfUrl cfgUrl = registryClient.subscribeConfig(url, callback);
        if (cfgUrl != null) { // 第一次先解析
            Map<String, String> attrs = cfgUrl.getAttrs();
            if (attrs != null) {
                attrs.put(Constants.CONFIG_KEY_INTERFACE, Constants.GLOBAL_SETTING);
                this.configListener.configChanged(attrs);
            }
        }
        // 保存insKey
        if (cfgUrl != null && cfgUrl.getInsKey() != null) {
            JSFContext.putIfAbsent(JSFContext.KEY_INSTANCEKEY, cfgUrl.getInsKey());
        }
    }

    /**
     * 恢复（全部重新注册和订阅）
     */
    public void recover() {
        registryClient.recover();
    }

    /**
     * 重连其它注册中心，同时（全部重新注册和订阅）
     */
    public void reconnectAndRecover() {
        registryClient.reconnect();
    }

    /**
     * 解析jsfurl
     *
     * @param jsfUrl
     *         jsf对象
     * @param interfaceConfig
     *         接口配置
     */
    private void parseIfaceId(JsfUrl jsfUrl, AbstractInterfaceConfig interfaceConfig) {
        Map<String, String> attrs = jsfUrl.getAttrs();
        if (CommonUtils.isNotEmpty(attrs)) {
            String ifaceIdStr = attrs.get("ifaceId");
            if (StringUtils.isNotEmpty(ifaceIdStr)) {
                interfaceConfig.setIfaceId(ifaceIdStr);
                JSFContext.cacheClassNameAndIfaceId(interfaceConfig.getInterfaceId(), ifaceIdStr);
                JSFRegistryHelper.writeInterfaceMapToFile(); // 写入备份文件
            }
        }
    }

    /**
     * 批量反注册
     */
    public void batchUnregister() {
        registryClient.batchUnregister();
    }
}