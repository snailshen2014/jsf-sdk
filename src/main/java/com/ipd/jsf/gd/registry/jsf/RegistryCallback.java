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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.registry.JSFRegistry;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.registry.ProviderListener;
import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.registry.ConfigListener;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.ThreadPoolUtils;

/**
 * Title: 注册中心回调实现类<br>
 * <p/>
 * Description: 实现的Callback接口<br>
 * <p/>
 */
public class RegistryCallback implements Callback<SubscribeUrl, String> {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistryCallback.class);

    /**
     * jsf Logger for this class
     */
    private final static JSFLogger JSF_LOGGER = JSFLoggerFactory.getLogger(RegistryCallback.class);

    /**
     * The Provider add listener map.
     */
    private transient Map<ConsumerConfig, List<ProviderListener>> providerListenerMap
            = new ConcurrentHashMap<ConsumerConfig, List<ProviderListener>>();

    /**
     * The Config listener map.
     */
    private transient Map<AbstractInterfaceConfig, List<ConfigListener>> configListenerMap
            = new ConcurrentHashMap<AbstractInterfaceConfig, List<ConfigListener>>();

    /**
     * 回调事件消费线程
     */
    private final ThreadPoolExecutor eventConsumerThread;

    /**
     * 注册中心处理器（部分业务）
     */
    private RegistryCallbackHandler registryCallbackHandler;

    /**
     * 注册中心自己
     */
    private JSFRegistry jsfRegistry;

    /**
     * 构造函数
     */
    public RegistryCallback() {
        // 回调事件消费线程
        eventConsumerThread = ThreadPoolUtils.newCachedThreadPool(1, 2,
                new LinkedBlockingQueue<Runnable>(1024),
                new NamedThreadFactory("JSF-jsfRegistry-CB"));

        registryCallbackHandler = new RegistryCallbackHandler();
    }

    /**
     * Notify void.
     *
     * @param result
     *         the result
     * @return the string
     */
    @Override
    public String notify(final SubscribeUrl result) {
        JsfUrl source = result.getSourceUrl();
        if (source == null) {
            return StringUtils.EMPTY;
        }
        if (isSync(result)) {
            return handleEvent(result); // 同步调用
        } else {
            // 改为异步
            eventConsumerThread.execute(new Runnable() {
                @Override
                public void run() {
                    handleEvent(result);
                }
            });
            return StringUtils.EMPTY;
        }
    }

    /**
     * 指定类型的事件，需要同步返回。
     *
     * @param url
     *         SubscribeUrl
     * @return 是否需要同步返回 boolean
     */
    private boolean isSync(SubscribeUrl url) {
        int type = url.getType();
        return type == SubscribeUrl.CHECK_RUOK
                || type == SubscribeUrl.CONSUMER_CONFIG
                || type == SubscribeUrl.CONSUMER_PROVIDERLIST
                || type == SubscribeUrl.PROVIDER_CONFIG
                || type == SubscribeUrl.SERVER_STATUS
                || type == SubscribeUrl.INSTANCE_CONFIG
                || type == SubscribeUrl.PROVIDER_CONCURRENT
                || type == SubscribeUrl.PROVIDER_INTERFACE
                || type == SubscribeUrl.CONSUMER_CONCURRENT
                || type == SubscribeUrl.INSTANCE_CONCURRENT
                || type == SubscribeUrl.INSTANCE_RESET_SCHEDULED // 重置定时任务
                || type == SubscribeUrl.SWITCH_MOCK_RESULT // 下发mock调用结果
                ;
    }

    /**
     * 关闭callback，打断线程
     */
    public void stop() {
        // 设置线程关闭
        eventConsumerThread.shutdownNow();
    }

    /**
     * 处理回调事件
     *
     * @param result
     *         回调结果
     * @return the string
     */
    private String handleEvent(SubscribeUrl result) {
        JsfUrl source = result.getSourceUrl();
        int type = result.getType();
        String ret = StringUtils.EMPTY;
        // 按事件类型分发处理
        switch (type) {
            case SubscribeUrl.CHECK_RUOK:
                ret = fireCheckAreYouOKEvent(result);
                break;
            case SubscribeUrl.PROVIDER_ADD:
                fireProviderAddEvent(source, result);
                break;
            case SubscribeUrl.PROVIDER_DEL:
                fireProviderDelEvent(source, result, false);
                break;
            case SubscribeUrl.PROVIDER_UPDATE_ALL:
                fireProviderUpdateEvent(source, result, false);
                break;
            case SubscribeUrl.PROVIDER_CLEAR:
                fireProviderClearEvent(source);
                break;
            case SubscribeUrl.PROVIDER_FORCE_DEL:
                fireProviderDelEvent(source, result, true);
                break;
            case SubscribeUrl.PROVIDER_FORCE_UPDATE_ALL:
                fireProviderUpdateEvent(source, result, true);
                break;
            case SubscribeUrl.CONFIG_UPDATE:
                fireConfigUpdateEvent(source, result.getConfig());
                break;
            case SubscribeUrl.ATTRIBUTE_P_UPDATE:
                fireProviderAttributeUpdateEvent(source, result.getConfig());
                break;
            case SubscribeUrl.ATTRIBUTE_C_UPDATE:
                fireConsumerAttributeUpdateEvent(source, result.getConfig());
                break;
            case SubscribeUrl.ATTRIBUTE_S_UPDATE:
                registryCallbackHandler.fireServerAttributeUpdateEvent(source, result.getConfig());
                break;
            case SubscribeUrl.CONSUMER_CONFIG:
                ret = registryCallbackHandler.fetchConsumerConfig(source);
                break;
            case SubscribeUrl.CONSUMER_PROVIDERLIST:
                ret = registryCallbackHandler.fetchConsumerProviders(source);
                break;
            case SubscribeUrl.CONSUMER_CONCURRENT:
                ret = registryCallbackHandler.fetchConsumerConcurrent(source);
                break;
            case SubscribeUrl.PROVIDER_CONFIG:
                ret = registryCallbackHandler.fetchProviderConfig(source);
                break;
            case SubscribeUrl.PROVIDER_CONCURRENT:
                ret = registryCallbackHandler.fetchProviderConcurrent(source);
                break;
            case SubscribeUrl.PROVIDER_INTERFACE:
                ret = registryCallbackHandler.fetchProviderInterfaceEvent(source);
                break;
            case SubscribeUrl.SERVER_STATUS:
                ret = registryCallbackHandler.fetchServerStatus(source, result.getConfig());
                break;
            case SubscribeUrl.INSTANCE_CONFIG:
                ret = registryCallbackHandler.fetchInstanceConfigs(source);
                break;
            case SubscribeUrl.INSTANCE_RECOVER:
                ret = fireInstanceRecoverEvent(source);
                break;
            case SubscribeUrl.INSTANCE_CONCURRENT:
                ret = registryCallbackHandler.fetchInstanceConcurrent(source);
                break;
            case SubscribeUrl.INSTANCE_RECONNECT:
                ret = fireInstanceReconnectAndRecoverEvent(source);
                break;
            case SubscribeUrl.INSTANCE_CONFIG_UPDATE:
                ret = registryCallbackHandler.fireInstanceConfigUpdateEvent(source);
                break;
            case SubscribeUrl.INSTANCE_RESET_SCHEDULED:
                ret = registryCallbackHandler.fireInstanceResetScheduledServiceEvent(source);
                break;
            case SubscribeUrl.INSTANCE_NOTIFICATION:
                ret = registryCallbackHandler.fireInstanceNotificationEvent(source);
                break;
            case SubscribeUrl.SWITCH_APP_LIMIT:
                ret = registryCallbackHandler.fireSwitchAppLimitEvent(source);
                break;
            case SubscribeUrl.SWITCH_MOCK_RESULT:
                ret = registryCallbackHandler.fireSwitchMockResult(source);
                break;
            default:
                LOGGER.error("Unknown callback event type : {} source: {}", result.getType(), source);
                break;
        }
        return ret;
    }

    /**
     * Fire check are you ok event.
     *
     * @param result
     *         the result
     */
    private String fireCheckAreYouOKEvent(SubscribeUrl result) {
        return "imok";
    }

    /**
     * Fire provider add event.
     *
     * @param source
     *         the source
     * @param result
     *         the result
     */
    private void fireProviderAddEvent(JsfUrl source, SubscribeUrl result) {
        List<JsfUrl> addProviders = result.getProviderList();
        LOGGER.info("Get provider add callback event, source: {}, provider size: {}", source, addProviders == null ? null : addProviders.size());
        if (JSFRegistryHelper.getProviderDataVersion(source) >= source.getDataVersion()) { // 本地的版本更加新，忽略
            LOGGER.info("Local data version is equal or greater than event, ignore event.");
        }
        if (CommonUtils.isEmpty(addProviders)) {
            return;
        }
        JSF_LOGGER.info("Add provider list is {}", addProviders);
        if (CommonUtils.isEmpty(providerListenerMap)) {
            JSF_LOGGER.warn("There has no provider listener in map");
            return;
        }
        try {
            for (Map.Entry<ConsumerConfig, List<ProviderListener>> entry : providerListenerMap.entrySet()) {
                if (JSFRegistryHelper.matchConsumer(entry.getKey(), source)) {
                    // 接口+alias+protocol相同 命中
                    List<ProviderListener> listeners = entry.getValue();
                    if (CommonUtils.isNotEmpty(listeners)) {
                        List<Provider> providers = JSFRegistryHelper.convertUrlToProviders(addProviders, source.getIface());
                        for (ProviderListener listener : listeners) {
                            try {
                                listener.addProvider(providers);
                            } catch (Exception e) {
                                LOGGER.warn("Fire provider add event error!", e);
                            }
                        }
                    } else {
                        JSF_LOGGER.warn("The provider listener of consumer is empty");
                    }  // 通知listener结束
                }
            }
            JSFRegistryHelper.updateProviderDataVersion(source, source.getDataVersion());
        } catch (Exception e) {
            LOGGER.error("Fire provider add event error!", e);
        }
    }

    /**
     * Fire provider del event.
     *
     * @param source
     *         the source
     * @param result
     *         the result
     * @param force
     *         the force
     */
    protected void fireProviderDelEvent(JsfUrl source, SubscribeUrl result, boolean force) {
        List<JsfUrl> delProviders = result.getProviderList();
        LOGGER.info("Get provider delete callback event, source: {}, provider size: {}", source, delProviders == null ? null : delProviders.size());
        if (JSFRegistryHelper.getProviderDataVersion(source) >= source.getDataVersion()) { // 本地的版本更加新，忽略
            LOGGER.info("Local data version is equal or greater than event, ignore event.");
        }
        if (CommonUtils.isEmpty(delProviders)) {
            return;
        }
        JSF_LOGGER.info("Del provider list is {}", delProviders);
        if (CommonUtils.isEmpty(providerListenerMap)) {
            JSF_LOGGER.warn("There has no provider listener in map");
            return;
        }
        try {
            boolean gForce = CommonUtils.isTrue(JSFContext.getGlobalVal(
                    Constants.SETTING_CONSUMER_PROVIDER_NULLABLE, "false")); // 全局允许，默认false不允许
            for (Map.Entry<ConsumerConfig, List<ProviderListener>> entry : providerListenerMap.entrySet()) {
                ConsumerConfig consumerConfig = entry.getKey();
                if (JSFRegistryHelper.matchConsumer(consumerConfig, source)) {
                    // 接口+alias+protocol相同 命中
                    List<ProviderListener> listeners = entry.getValue();
                    if (CommonUtils.isNotEmpty(listeners)) {
                        // 要删除的服务列表
                        List<Provider> providers = JSFRegistryHelper.convertUrlToProviders(delProviders, source.getIface());
                        // 内存里的服务列表
                        Set<Provider> providersInMemory = consumerConfig.getClient().currentProviderList();
                        // 比较内存里的和要删除的。
                        if (!gForce && !force && JSFRegistryHelper.containsProvider(providers, providersInMemory)) {
                            // 如果要删除的包含内存里的全部的列表（危险操作），且不是强制删除，忽略
                            LOGGER.warn("Provider list should not be empty, ignore this delete event.");
                        } else {
                            for (ProviderListener listener : listeners) {
                                try {
                                    listener.removeProvider(providers);
                                } catch (Exception e) {
                                    LOGGER.warn("Fire provider delete event error!", e);
                                }
                            }
                        }
                    } else {
                        JSF_LOGGER.warn("The provider listener of consumer is empty");
                    }
                }  // 通知listener结束
            }
            JSFRegistryHelper.updateProviderDataVersion(source, source.getDataVersion());
        } catch (Exception e) {
            LOGGER.error("Fire provider delete event error!", e);
        }
    }

    /**
     * Fire provider update all event.
     *  @param source
     *         the source
     * @param result
     *         the result
     * @param force
     *         the force
     */
    protected void fireProviderUpdateEvent(JsfUrl source, SubscribeUrl result, boolean force) {
        List<JsfUrl> allProviders = result.getProviderList();
        LOGGER.info("Get provider update callback event, source: {}, provider size: {}", source, allProviders == null ? null : allProviders.size());
        if (JSFRegistryHelper.getProviderDataVersion(source) >= source.getDataVersion()) { // 本地的版本更加新，忽略
            LOGGER.info("Local data version is equal or greater than event, ignore event.");
        }
        JSF_LOGGER.info("All provider list is {}", allProviders);
        if (CommonUtils.isEmpty(providerListenerMap)) {
            JSF_LOGGER.warn("There has no provider listener in map");
            return;
        }
        try {
            boolean gForce = CommonUtils.isTrue(JSFContext.getGlobalVal(
                    Constants.SETTING_CONSUMER_PROVIDER_NULLABLE, "false")); // 全局允许，默认false不允许
            for (Map.Entry<ConsumerConfig, List<ProviderListener>> entry : providerListenerMap.entrySet()) {
                if (JSFRegistryHelper.matchConsumer(entry.getKey(), source)) {
                    // 接口+alias+protocol相同 命中
                    List<ProviderListener> listeners = entry.getValue();
                    if (CommonUtils.isNotEmpty(listeners)) {
                        if (CommonUtils.isNotEmpty(allProviders)) { // 列表不为空，更新
                            List<Provider> providers = JSFRegistryHelper.convertUrlToProviders(allProviders, source.getIface());
                            for (ProviderListener listener : listeners) {
                                try {
                                    listener.updateProvider(providers);
                                } catch (Exception e) {
                                    LOGGER.warn("Fire provider add event error!", e);
                                }
                            }
                        } else { // 列表为空
                            if (gForce || force) { // 强制清空服务列表
                                for (ProviderListener listener : listeners) {
                                    try {
                                        listener.updateProvider(null);
                                    } catch (Exception e) {
                                        LOGGER.warn("Fire provider clear event error!", e);
                                    }
                                }
                            } else { // 不是强制删除全部，忽略
                                LOGGER.warn("Provider list should not be empty, ignore this update event.");
                            }
                        }
                    } else {
                        JSF_LOGGER.warn("The provider listener of consumer is empty");
                    }
                } // 通知listener结束
            }
            JSFRegistryHelper.updateProviderDataVersion(source, source.getDataVersion());
        } catch (Exception e) {
            LOGGER.error("Fire provider update event error!", e);
        }
    }

    /**
     * Fire provider clear event.
     *
     * @param source
     *         the source
     */
    protected void fireProviderClearEvent(JsfUrl source) {
        LOGGER.warn("Get provider clear callback event (Maybe this host has been added to blacklist), source: {}", source);
        if (CommonUtils.isEmpty(providerListenerMap)) {
            JSF_LOGGER.warn("There has no provider listener in map");
            return;
        }
        try {
            for (Map.Entry<ConsumerConfig, List<ProviderListener>> entry : providerListenerMap.entrySet()) {
                if (JSFRegistryHelper.matchConsumer(entry.getKey(), source)) {
                    // 接口+alias+protocol相同 命中
                    List<ProviderListener> listeners = entry.getValue();
                    if (CommonUtils.isNotEmpty(listeners)) {
                        for (ProviderListener listener : listeners) {
                            try {
                                listener.updateProvider(null);
                            } catch (Exception e) {
                                LOGGER.warn("Fire provider clear event error!", e);
                            }
                        }
                    } else {
                        JSF_LOGGER.warn("The provider listener of consumer is empty");
                    }
                }
            }
            JSFRegistryHelper.updateProviderDataVersion(source, source.getDataVersion());
        } catch (Exception e) {
            LOGGER.error("Fire provider clear event error!", e);
        }
    }

    /**
     * Fire config update event.
     *
     * @param source
     *         the source
     * @param config
     *         the config
     */
    protected void fireConfigUpdateEvent(JsfUrl source, Map<String, String> config) {
        LOGGER.info("Get config update callback event, source: {}", source);
        if (JSFRegistryHelper.getConfigDataVersion(source) >= source.getDataVersion()) { // 本地的版本更加新，忽略
            LOGGER.info("Local config version is equal or greater than event, ignore event.");
        }
        if (CommonUtils.isNotEmpty(config)) {
            JSF_LOGGER.info("All config is {}", config);
            if (CommonUtils.isEmpty(configListenerMap)) {
                JSF_LOGGER.warn("There has no config listener in map");
                return;
            }
            try {
                for (Map.Entry<AbstractInterfaceConfig, List<ConfigListener>> entry : configListenerMap.entrySet()) {
                    if (JSFRegistryHelper.matchInterface(entry.getKey(), source)) {
                        // 接口相同 命中
                        List<ConfigListener> listeners = entry.getValue();
                        if (CommonUtils.isNotEmpty(listeners)) {
                            config.put(Constants.CONFIG_KEY_INTERFACE, source.getIface());
                            for (ConfigListener listener : listeners) {
                                try {
                                    listener.configChanged(config);
                                } catch (Exception e) {
                                    LOGGER.warn("Fire config update event error!", e);
                                }
                            }
                        } else {
                            JSF_LOGGER.warn("The config listener of consumer is empty");
                        }
                    }
                }
                JSFRegistryHelper.updateConfigDataVersion(source, source.getDataVersion());
            } catch (Exception e) {
                LOGGER.error("Fire config update event error!", e);
            }
        }
    }

    /**
     * Fire provider attribute update event.
     *
     * @param source
     *         the source
     * @param attrs
     *         the config
     */
    protected void fireProviderAttributeUpdateEvent(JsfUrl source, Map<String, String> attrs) {
        LOGGER.info("Get provider attribute update callback event, source: {}, data: {}", source, attrs);
        if (CommonUtils.isNotEmpty(attrs)) {
            if (CommonUtils.isEmpty(configListenerMap)) {
                JSF_LOGGER.warn("There has no config listener in map");
                return;
            }
            try {
                for (Map.Entry<AbstractInterfaceConfig, List<ConfigListener>> entry : configListenerMap.entrySet()) {
                    AbstractInterfaceConfig itf = entry.getKey();
                    if (itf instanceof ProviderConfig && JSFRegistryHelper.matchProvider((ProviderConfig) itf, source)) {
                        // 接口+别名相同 命中
                        List<ConfigListener> listeners = entry.getValue();
                        if (CommonUtils.isNotEmpty(listeners)) {
                            for (ConfigListener listener : listeners) {
                                try {
                                    listener.providerAttrUpdated(attrs);
                                } catch (Exception e) {
                                    LOGGER.warn("Fire provider attribute update event error!", e);
                                }
                            }
                        } else {
                            JSF_LOGGER.warn("The config listener of consumer is empty");
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Fire provider attribute update event error!", e);
            }
        }
    }

    /**
     * Fire consumer attribute update event.
     *
     * @param source
     *         the source
     * @param attrs
     *         the config
     */
    protected void fireConsumerAttributeUpdateEvent(JsfUrl source, Map<String, String> attrs) {
        LOGGER.info("Get consumer attribute update callback event, source: {}, data: {}", source, attrs);
        if (CommonUtils.isNotEmpty(attrs)) { // 需要区分alias
            if (CommonUtils.isEmpty(configListenerMap)) {
                JSF_LOGGER.warn("There has no config listener in map");
                return;
            }
            try {
                for (Map.Entry<AbstractInterfaceConfig, List<ConfigListener>> entry : configListenerMap.entrySet()) {
                    AbstractInterfaceConfig itf = entry.getKey();
                    if (itf instanceof ConsumerConfig && JSFRegistryHelper.matchConsumer((ConsumerConfig) itf, source)) {
                        // 接口+别名+协议相同 命中
                        List<ConfigListener> listeners = entry.getValue();
                        if (CommonUtils.isNotEmpty(listeners)) {
                            for (ConfigListener listener : listeners) {
                                try {
                                    listener.consumerAttrUpdated(attrs);
                                } catch (Exception e) {
                                    LOGGER.warn("Fire consumer attribute update event error!", e);
                                }
                            }
                        } else {
                            JSF_LOGGER.warn("The config listener of consumer is empty");
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Fire consumer attribute update event error!", e);
            }
        }
    }

    /**
     * Fire instance recover event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireInstanceRecoverEvent(JsfUrl source) {
        LOGGER.info("Get instance recover callback event, source: {}", source);
        try {
            // 主动重新注册订阅
            if (jsfRegistry != null) {
                jsfRegistry.recover();
            }
            return "";
        } catch (Exception e) {
            LOGGER.warn("Error when fire instance recover!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire instance reconnect and recover event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireInstanceReconnectAndRecoverEvent(JsfUrl source) {
        LOGGER.info("Get instance reconnect and recover callback event, source: {}", source);
        try {
            // 主动重连其它注册中心 然后重新注册订阅
            if (jsfRegistry != null) {
                jsfRegistry.reconnectAndRecover();
            }
            return "";
        } catch (Exception e) {
            LOGGER.warn("Error when fire instance recover!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Add provider listener.
     *
     * @param consumerConfig
     *         the consumer config
     * @param listener
     *         the listener
     */
    public void addProviderListener(ConsumerConfig consumerConfig, ProviderListener listener) {
        if (listener != null) {
            initOrAddList(providerListenerMap, consumerConfig, listener);
        }
    }

    /**
     * Remove provider listener.
     *
     * @param consumerConfig
     *         the consumer config
     */
    public void removeProviderListener(ConsumerConfig consumerConfig) {
        providerListenerMap.remove(consumerConfig);
    }

    /**
     * Add config listener.
     *
     * @param config
     *         the config
     * @param listener
     *         the listener
     */
    public void addConfigListener(AbstractInterfaceConfig config, ConfigListener listener) {
        if (listener != null) {
            initOrAddList(configListenerMap, config, listener);
        }
    }

    /**
     * Remove config listener.
     *
     * @param config
     *         the config
     */
    public void removeConfigListener(AbstractInterfaceConfig config) {
        configListenerMap.remove(config);
    }

    /**
     * Init or add list.
     *
     * @param <K>
     *         the key parameter
     * @param <V>
     *         the value parameter
     * @param orginMap
     *         the orgin map
     * @param key
     *         the key
     * @param needAdd
     *         the need add
     */
    private <K, V> void initOrAddList(Map<K, List<V>> orginMap, K key, V needAdd) {
        List<V> listeners = orginMap.get(key);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<V>();
            listeners.add(needAdd);
            orginMap.put(key, listeners);
        } else {
            listeners.add(needAdd);
        }
    }

    /**
     * Add config listener.
     *
     * @param listener
     *         the listener
     */
    public void addGlobalConfigListener(ConfigListener listener) {
        initOrAddList(configListenerMap, globalConfig, listener);
    }

    private static AbstractInterfaceConfig globalConfig = new AbstractInterfaceConfig() {
        @Override
        protected Class<?> getProxyClass() {
            return null;
        }

        @Override
        protected String buildKey() {
            return null;
        }

        public String getInterfaceId() {
            return Constants.GLOBAL_SETTING;
        }
    };

    /**
     * 设置注册中心
     *
     * @param jsfRegistry
     *         ClientRegistry
     */
    public void setRegistry(JSFRegistry jsfRegistry) {
        this.jsfRegistry = jsfRegistry;
    }

    /**
     * 得到业务处理类
     *
     * @return 业务处理类
     */
    public RegistryCallbackHandler getCallbackHandler() {
        return registryCallbackHandler;
    }
}