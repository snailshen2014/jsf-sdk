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
package com.ipd.jsf.gd.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.client.ClientFactory;
import com.ipd.jsf.gd.client.ClientProxyInvoker;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.reflect.ProxyFactory;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.registry.ProviderListener;
import com.ipd.jsf.gd.registry.RegistryFactory;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.registry.ClientRegistry;
import com.ipd.jsf.gd.registry.ConfigListener;
import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.transport.CallbackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.client.Client;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.RpcStatus;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 服务调用者配置<br>
 * <p/>
 * Description: 对应jsf:consumer，对应一个接口的服务调用者<br>
 * <p/>
 *
 * @param <T>    接口类名
 */
public class ConsumerConfig<T> extends AbstractConsumerConfig<T> implements Serializable {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerConfig.class);

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -2910502986608678372L;

    /**
     * 调用类
     */
    protected transient volatile Client client;

    /**
     * 服务配置的listener
     */
    protected transient volatile ProviderListener providerListener;

    /**
     * 发布的调用者配置（含计数器）
     */
    protected final static ConcurrentHashMap<String, AtomicInteger> REFERRED_KEYS
            = new ConcurrentHashMap<String, AtomicInteger>();

    /**
     * Refer t.
     *
     * @return the t
     * @throws InitErrorException the init error exception
     */
    public synchronized T refer() throws InitErrorException {
        if (proxyIns != null) {
            return proxyIns;
        }
        String key = buildKey();
        // 检查参数
        // alias不能为空
        if (StringUtils.isBlank(alias)) {
            throw new InitErrorException("[JSF-21300]Value of \"alias\" value is not specified in consumer" +
                    " config with key " + key + " !");
        }
        // 提前检查接口类
        getProxyClass();

        LOGGER.info("Refer consumer config : {} with bean id {}", key, getId());

        // 注意同一interface，同一alias，同一protocol情况
        AtomicInteger cnt = REFERRED_KEYS.get(key); // 计数器
        if (cnt == null) { // 没有发布过
            cnt = CommonUtils.putToConcurrentMap(REFERRED_KEYS, key, new AtomicInteger(0));
        }
        int c = cnt.incrementAndGet();
        if (c > 3) {
            if(!CommonUtils.isFalse(getParameter(Constants.HIDDEN_KEY_WARNNING))){
                throw new InitErrorException("[JSF-21304]Duplicate consumer config with key " + key
                        + " has been referred more than 3 times!"
                        + " Maybe it's wrong config, please check it."
                        + " Ignore this if you did that on purpose!");
            } else {
                LOGGER.warn("[JSF-21304]Duplicate consumer config with key {} "
                        + "has been referred more than 3 times!"
                        + " Maybe it's wrong config, please check it."
                        + " Ignore this if you did that on purpose!", key);
            }
        } else if (c > 1) {
            LOGGER.warn("[JSF-21303]Duplicate consumer config with key {} has been referred!"
                    + " Maybe it's wrong config, please check it."
                    + " Ignore this if you did that on purpose!", key);
        }

        // 检查是否有回调函数
        CallbackUtil.autoRegisterCallBack(getProxyClass());
        // 注册接口类反序列化模板
        if(!isGeneric()){
            try {
                CodecUtils.registryService(serialization, getProxyClass());
            } catch (InitErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new InitErrorException("[JSF-21305]Registry codec template error!", e);
            }
        }
        // 如果本地发布了服务，则优选走本地代理，没有则走远程代理
        if (isInjvm() && BaseServerHandler.getInvoker(getInterfaceId(), getAlias()) != null) {
            LOGGER.info("Find matched provider invoker in current jvm, " +
                    "will invoke preferentially until it unexported");
        }

        configListener = new ConsumerAttributeListener();
        providerListener = new ClientProviderListener();
        try {
            // 生成客户端
            client = ClientFactory.getClient(this);
            // 构造Invoker对象（执行链）
            proxyInvoker = new ClientProxyInvoker(this);
            // 提前检查协议+序列化方式
            ProtocolFactory.check(Constants.ProtocolType.valueOf(getProtocol()),
                    Constants.CodecType.valueOf(getSerialization()));
            // 创建代理类
            proxyIns = (T) ProxyFactory.buildProxy(getProxy(), getProxyClass(), proxyInvoker);
        } catch (Exception e) {
            if (client != null) {
                client.destroy();
                client = null;
            }
            cnt.decrementAndGet(); // 发布失败不计数
            if (e instanceof InitErrorException) {
                throw (InitErrorException) e;
            } else {
                throw new InitErrorException("[JSF-21306]Build consumer proxy error!", e);
            }
        }
        if (onavailable != null && client != null) {
            client.checkStateChange(false); // 状态变化通知监听器
        }
        JSFContext.cacheConsumerConfig(this);
        return proxyIns;
    }

    /**
     * Unrefer void.
     */
    public synchronized void unrefer() {
        if (proxyIns == null) {
            return;
        }
        String key = buildKey();
        LOGGER.info("Unrefer consumer config : {} with bean id {}", key, getId());
        try {
            client.destroy();
        } catch (Exception e) {
            LOGGER.warn("Catch exception when unrefer consumer config : " + key
                    + ", but you can ignore if it's called by JVM shutdown hook", e);
        }
        // 清除一些缓存
        AtomicInteger cnt = REFERRED_KEYS.get(key);
        if (cnt != null && cnt.decrementAndGet() <= 0) {
            REFERRED_KEYS.remove(key);
        }
        configListener = null;
        providerListener = null;
        JSFContext.invalidateConsumerConfig(this);
        RpcStatus.removeStatus(this);
        proxyIns = null;

        // 取消订阅到注册中心
        unsubscribe();
    }

    /**
     * 订阅服务列表
     *
     * @return 当前服务列表 list
     */
    public List<Provider> subscribe() {
        List<Provider> tmpProviderList = new ArrayList<Provider>();
        // 从注册中心订阅
        for (RegistryConfig registryConfig : getRegistry()) {
            ClientRegistry registry = RegistryFactory
                    .getRegistry(registryConfig);
            try {
                List<Provider> providers = registry.subscribe(this, providerListener, configListener);
                if (CommonUtils.isNotEmpty(providers)) {
                    tmpProviderList.addAll(providers);
                }
            } catch (InitErrorException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.warn("Catch exception when subscribe from registry: " + registryConfig.getId()
                        + ", but you can ignore if it's called by JVM shutdown hook", e);
            }
        }
        return tmpProviderList;
    }

    /**
     * 取消订阅服务列表
     */
    public void unsubscribe() {
        if (StringUtils.isEmpty(url) && isSubscribe()) {
            List<RegistryConfig> registryConfigs = super.getRegistry();
            if (registryConfigs != null) {
                for (RegistryConfig registryConfig : registryConfigs) {
                    ClientRegistry registry = RegistryFactory.getRegistry(registryConfig);
                    try {
                        registry.unsubscribe(this);
                    } catch (Exception e) {
                        LOGGER.warn("Catch exception when unsubscribe from registry: " + registryConfig.getId()
                                + ", but you can ignore if it's called by JVM shutdown hook", e);
                    }
                }
            }
        }
    }

    /**
     * 客户端节点变化监听器
     */
    private class ClientProviderListener implements ProviderListener {

        @Override
        public void addProvider(List<Provider> providers) {
            if (client != null) {
                boolean originalState = client.isAvailable();
                client.addProvider(providers);
                client.checkStateChange(originalState);
            }
        }

        @Override
        public void removeProvider(List<Provider> providers) {
            if (client != null) {
                boolean originalState = client.isAvailable();
                client.removeProvider(providers);
                client.checkStateChange(originalState);
            }
        }

        @Override
        public void updateProvider(List<Provider> newProviders) {
            if (client != null) {
                boolean originalState = client.isAvailable();
                client.updateProvider(newProviders);
                client.checkStateChange(originalState);
            }
        }
    }

    /**
     * Consumer配置发生变化监听器
     */
    private class ConsumerAttributeListener implements ConfigListener {

        @Override
        public void configChanged(Map newValue) {
            if (client != null) {
                if (newValue.containsKey(Constants.SETTING_ROUTER_OPEN) ||
                        newValue.containsKey(Constants.SETTING_ROUTER_RULE)) {
                    // 是否比较变化？
                    client.resetRouters();
                }
            }
        }

        @Override
        public void providerAttrUpdated(Map newValue) {
        }

        @Override
        public synchronized void consumerAttrUpdated(Map newValueMap) {
            // 重要： proxyIns不能换，只能换client。。。。
            // 修改调用的alias cluster(loadblance) timeout, retries？
            Map<String, String> newValues = (Map<String, String>) newValueMap;
            Map<String, String> oldValues = new HashMap<String, String>();
            boolean rerefer = false;
            try { // 检查是否有变化
                // 是否过滤map?
                for (Map.Entry<String, String> entry : newValues.entrySet()) {
                    String newValue = entry.getValue();
                    String oldValue = queryAttribute(entry.getKey());
                    boolean changed = oldValue == null ? newValue != null : !oldValue.equals(newValue);
                    if (changed) { // 记住旧的值
                        oldValues.put(entry.getKey(), oldValue);
                    }
                    rerefer = rerefer || changed;
                }
            } catch (Exception e) {
                LOGGER.error("Catch exception when consumer attribute comparing", e);
                return;
            }
            if (rerefer) {
                // 需要重新发布
                LOGGER.info("Rerefer consumer {}", buildKey());
                try {
                    unsubscribe();// 取消订阅旧的
                    for (Map.Entry<String, String> entry : newValues.entrySet()) { // change attrs
                        updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                } catch (Exception e) { // 切换属性出现异常
                    LOGGER.error("Catch exception when consumer attribute changed", e);
                    for (Map.Entry<String, String> entry : oldValues.entrySet()) { //rollback old attrs
                        updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                    subscribe(); // 重新订阅回滚后的旧的
                    return;
                }
                try {
                    switchClient();
                } catch (Exception e) { //切换客户端出现异常
                    LOGGER.error("Catch exception when consumer refer after attribute changed", e);
                    unsubscribe(); // 取消订阅新的
                    for (Map.Entry<String, String> entry : oldValues.entrySet()) { //rollback old attrs
                        updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                    subscribe(); // 重新订阅回滚后的旧的
                }
            }
        }

        /**
         * Switch client.
         * @throws Exception the exception
         */
        private void switchClient() throws Exception {
            Client newclient = null;
            Client oldClient;
            try { // 构建新的
                newclient = ClientFactory.getClient(ConsumerConfig.this); //生成新的 会再重新订阅
                oldClient = ((ClientProxyInvoker) proxyInvoker).setClient(newclient);
            } catch (Exception e) {
                if (newclient != null) {
                    newclient.destroy();
                }
                throw e;
            }
            try { // 切换
                client = newclient;
                if (oldClient != null) {
                    oldClient.destroy(); // 旧的关掉
                }
            } catch (Exception e) {
                LOGGER.warn("Catch exception when destroy");
            }
        }
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public Client getClient() {
        return client;
    }
}