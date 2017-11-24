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

import com.ipd.jsf.gd.client.DefaultGroupRouter;
import com.ipd.jsf.gd.client.GroupRouter;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.reflect.ProxyFactory;
import com.ipd.jsf.gd.registry.ConfigListener;
import com.ipd.jsf.gd.util.BeanUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.client.MultiClientProxyInvoker;
import com.ipd.jsf.gd.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Title: 服务调用者分组配置<br>
 * <p/>
 * Description: 对应jsf:consumerGroup，对应一个接口的多个服务调用者<br>
 * <p/>
 *
 * @param <T>
 *         接口类名
 */
public class ConsumerGroupConfig<T> extends AbstractConsumerConfig<T> implements Serializable {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerGroupConfig.class);

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 2784519526975849938L;

    /*---------- 参数配置项开始 ------------*/

    /**
     * 分组路由器
     */
    protected GroupRouter groupRouter;

    /**
     * 目标参数（机房/分组）索引，第一个参数从0开始
     */
    protected Integer dstParam;

    /**
     * 分组下调用者配置
     */
    protected Map<String, ConsumerConfig<T>> consumerConfigs;

    /**
     * alias自适应，当没有分组时，可以自动增加
     */
    protected boolean aliasAdaptive;

     /*---------- 参数配置项结束 ------------*/

    /**
     * 当前存在的分组信息，作为缓存
     */
    private transient List<String> aliasesCache = new ArrayList<String>();

    /**
     * Refer t.
     *
     * @return the t
     * @throws InitErrorException
     *         the init error exception
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
        // 检查是否有自定义组路由
        if (groupRouter == null) {
            LOGGER.debug("Custom group router is null, use default random group router instead of it");
            groupRouter = new DefaultGroupRouter();
        }

        LOGGER.info("Refer consumer group config : {} with bean id {}", key, getId());

        // 解析多个alias，生成多个ConsumerConfig
        String[] aliases = StringUtils.splitWithCommaOrSemicolon(alias);
        for (String alias : aliases) {
            ConsumerConfig<T> actualConfig = generateConsumerConfig(alias, null);
            actualConfig.refer();
            consumerConfigs.put(alias, actualConfig);
            aliasesCache.add(alias);
        }
        // 多客户端筛选的Invoker
        proxyInvoker = new MultiClientProxyInvoker(this, groupRouter);
        try {
            // 创建代理类
            proxyIns = (T) ProxyFactory.buildProxy(getProxy(), getProxyClass(), proxyInvoker);
        } catch (Exception e) {
            unrefer();
            throw new InitErrorException("[JSF-21307]Build consumer group proxy error!", e);
        }
        JSFContext.cacheConsumerGroupConfig(this);
        return proxyIns;
    }

    /**
     * 订阅服务列表
     *
     * @return 当前服务列表 list
    protected void doRegister() {
        boolean oldSub = isSubscribe();
        if (oldSub) {
            // 从注册中心订阅
            for (RegistryConfig registryConfig : getRegistry()) {
                ClientRegistry registry = RegistryFactory
                        .getRegistry(registryConfig);
                try {
                    setSubscribe(false); // 只注册不订阅
                    registry.subscribe(this, null, new ConsumerGroupAttributeListener());
                } catch (InitErrorException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Catch exception when subscribe from registry: " + registryConfig.getId()
                            + ", but you can ignore if it's called by JVM shutdown hook", e);
                }
            }
            setSubscribe(true);
        }
    }*/

    /**
     * Unrefer void.
     */
    public synchronized void unrefer() {
        if (proxyIns == null) {
            return;
        }
        String key = buildKey();
        LOGGER.info("Unrefer consumer group config : {} with bean id {}", key, getId());
        for (Map.Entry<String, ConsumerConfig<T>> consumer : consumerConfigs.entrySet()) {
            try {
                consumer.getValue().setRegister(isRegister());
                consumer.getValue().unrefer();
            } catch (Exception e) {
                LOGGER.warn("Catch exception when unrefer consumer config of consumer group : " + key, e);
            }
        }
        proxyIns = null;
        JSFContext.invalidateConsumerGroupConfig(this);
    }

    /**
     * Gets groupRouter.
     *
     * @return the groupRouter
     */
    public GroupRouter getGroupRouter() {
        return groupRouter;
    }

    /**
     * Sets groupRouter.
     *
     * @param groupRouter
     *         the groupRouter
     */
    public void setGroupRouter(GroupRouter groupRouter) {
        this.groupRouter = groupRouter;
    }

    /**
     * Gets dst room param.
     *
     * @return the dst room param
     */
    public Integer getDstParam() {
        return dstParam;
    }

    /**
     * Sets dst room param.
     *
     * @param dstParam
     *         the dst room param
     */
    public void setDstParam(Integer dstParam) {
        this.dstParam = dstParam;
    }

    /**
     * Gets consumer configs.
     *
     * @return the consumer configs
     */
    public Map<String, ConsumerConfig<T>> getConsumerConfigs() {
        return consumerConfigs;
    }

    /**
     * Sets consumer configs.
     *
     * @param consumerConfigs
     *         the consumer configs
     */
    public void setConsumerConfigs(Map<String, ConsumerConfig<T>> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    /**
     * Is alias adaptive.
     *
     * @return the boolean
     */
    public boolean isAliasAdaptive() {
        return aliasAdaptive;
    }

    /**
     * Sets alias adaptive.
     *
     * @param aliasAdaptive
     *         the alias adaptive
     */
    public void setAliasAdaptive(boolean aliasAdaptive) {
        this.aliasAdaptive = aliasAdaptive;
    }

    /**
     * Sets alias.
     *
     * @param alias
     *         the alias
     */
    public void setAlias(String alias) {
        // 比普通的alias多允许逗号
        checkNormalWithCommaColon("alias", alias);
        this.alias = alias;
    }

    /**
     * 从一个ConsumerGroupConfig
     *
     * @param actualAlias
     *         实际分组别名
     * @return 需要合成的配置 consumer config
     */
    private ConsumerConfig<T> generateConsumerConfig(String actualAlias, ConsumerConfig<T> actualConfig) {
        if (consumerConfigs == null) {
            consumerConfigs = new ConcurrentHashMap<String, ConsumerConfig<T>>();
        }
        ConsumerConfig<T> customConfig = consumerConfigs.get(actualAlias);
        if (customConfig == null && actualConfig == null) { // 无此alias的单独配置
            // 构建一个新的ConsumerConfig
            customConfig = new ConsumerConfig<T>();
            BeanUtils.copyProperties(this, customConfig, "id", "alias");
            customConfig.setAlias(actualAlias);
        } else { // ConsumerConfig存在此alias的单独配置，以单独配置为准，覆盖分组配置的。
            if (customConfig == null) {
                customConfig = actualConfig;
            }
            // 计算出单独配置和默认配置改动了哪些字段。
            List<String> modifiedFields = BeanUtils.getModifiedFields(new ConsumerConfig(), customConfig,
                    "id", "interfaceId", "alias");
            modifiedFields.add("id");
            modifiedFields.add("alias");
            // 未修改的字段用分组配置覆盖单独配置
            BeanUtils.copyProperties(this, customConfig, modifiedFields.toArray(new String[modifiedFields.size()]));
            // 合并参数
            if (modifiedFields.contains("parameters") && parameters != null) {
                Map<String, String> customParameters = customConfig.getParameters();
                if (customParameters != null) {
                    for (Map.Entry<String, String> gpEntry : parameters.entrySet()) {
                        String gKey = gpEntry.getKey(); // 分组里的值
                        // 需要合并到用户自定义的参数里
                        if (!customParameters.containsKey(gKey)) { // 自定义没有和分组里有
                            customParameters.put(gKey, gpEntry.getValue());
                        }
                    }
                } else {
                    customConfig.setParameters(parameters);
                }
            }
            // 合并方法
            if (modifiedFields.contains("methods") && methods != null) {
                Map<String, MethodConfig> customMethodConfigs = customConfig.getMethods();
                if (customMethodConfigs == null) {  // 无单独配置，用分组配置为准
                    customConfig.setMethods(methods);
                } else {  // 有单独配置，需要合并
                    for (Map.Entry<String, MethodConfig> groupEntry : methods.entrySet()) {
                        String methodName = groupEntry.getKey();
                        MethodConfig groupMethod = groupEntry.getValue();
                        MethodConfig customMethod = customMethodConfigs.get(methodName);
                        if (customMethod == null) { // 用户未配置这个方法
                            customMethodConfigs.put(methodName, groupMethod);
                        } else { // 用户配置了，需要合并下
                            List<String> mModifiedFields = BeanUtils.getModifiedFields(new MethodConfig(),
                                    customMethod, "id", "name");
                            mModifiedFields.add("id");
                            mModifiedFields.add("name");
                            BeanUtils.copyProperties(groupMethod, customMethod,
                                    mModifiedFields.toArray(new String[mModifiedFields.size()]));

                            Map<String, String> groupMethodParameters = groupMethod.getParameters();
                            if (mModifiedFields.contains("parameters") && groupMethodParameters != null) { // 合并参数
                                Map<String, String> customMethodParameters = customMethod.getParameters();
                                if (customMethodParameters != null) {
                                    for (Map.Entry<String, String> gpEntry : groupMethodParameters.entrySet()) {
                                        String gKey = gpEntry.getKey(); // 分组里的值
                                        // 需要合并到用户自定义的参数里
                                        if (!customMethodParameters.containsKey(gKey)) { // 自定义没有和分组里有
                                            customMethodParameters.put(gKey, gpEntry.getValue());
                                        }
                                    }
                                } else {
                                    customMethod.setParameters(groupMethodParameters);
                                }
                            }
                        }
                    }
                }
            }
        }
        customConfig.setId(getId()); // 使用分组的id
        customConfig.setParameter("_fromGroup", "true"); // 注册的时候标记属于分组调用的group
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("build consumer node for group : {}", JsonUtils.toJSONString(customConfig));
        }
        return customConfig;
    }

    /**
     * Consumer配置发生变化监听器 TODO
     */
    private class ConsumerGroupAttributeListener implements ConfigListener {

        @Override
        public void configChanged(Map newValue) {
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
                LOGGER.error("Catch exception when consumer group attribute comparing", e);
                return;
            }
            if (rerefer) {
                // 需要重新发布
                LOGGER.info("Rerefer consumer group {}", buildKey());
            }
        }
    }

    /**
     * 重载锁
     */
    private ReentrantLock changeAliasLock = new ReentrantLock();

    /**
     * 增加一个分组
     *
     * @param addAlias
     *         增加的分组
     */
    public void addConsumerConfig(String addAlias) {
        if (StringUtils.isEmpty(addAlias)) {
            throw new RpcException("[JSF-22109]Alias of consumer config which need add to consumer group is null.");
        }
        changeAliasLock.lock();
        try {
            if (!consumerConfigs.containsKey(addAlias)) {
                LOGGER.info("Match unknown alias : {}/{}, need refer it !", interfaceId, addAlias);
                mergeAlias(addAlias); // 合并分组到alias字段
                ConsumerConfig<T> actualConfig = generateConsumerConfig(addAlias, null);
                actualConfig.refer();

                LOGGER.info("add new alias : {}/{}", interfaceId, addAlias);
                consumerConfigs.put(addAlias, actualConfig);
                aliasesCache.add(addAlias);
            }
        } finally {
            changeAliasLock.unlock();
        }
    }

    /**
     * 增加一个分组
     *
     * @param consumerConfig
     *         增加的引用
     */
    public void addConsumerConfig(ConsumerConfig<T> consumerConfig) {
        if (consumerConfig == null) {
            return;
        }
        String addAlias = consumerConfig.getAlias();
        if (StringUtils.isEmpty(addAlias)) {
            throw new RpcException("[JSF-22109]Alias of consumer config which need add to consumer group is null.");
        }
        changeAliasLock.lock();
        try {
            if (!consumerConfigs.containsKey(addAlias)) {
                LOGGER.info("Match unknown alias : {}/{}, need refer it !", interfaceId, addAlias);
                mergeAlias(addAlias); // 合并分组到alias字段
                ConsumerConfig<T> actualConfig = generateConsumerConfig(addAlias, consumerConfig);
                actualConfig.refer();
                consumerConfigs.put(addAlias, actualConfig); // 先放进去一个默认的

                LOGGER.info("add new alias : {}/{}", interfaceId, addAlias);
                consumerConfigs.put(addAlias, actualConfig);
                aliasesCache.add(addAlias);
            }
        } finally {
            changeAliasLock.unlock();
        }
    }

    private void mergeAlias(String addAlias) {
        if (StringUtils.isNotEmpty(alias)) {
            String[] aliases = StringUtils.split(alias, ",");
            boolean contain = false;
            for (String alias : aliases) {
                if (alias.equals(addAlias)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                this.alias = alias.concat(",").concat(addAlias);
            }
        } else {
            this.alias = addAlias;
        }
    }

    /**
     * 删除一个分组
     *
     * @param delAlias
     *         增加的分组
     */
    public void delConsumerConfig(String delAlias) {
        changeAliasLock.lock();
        try {
            deleteAlias(delAlias);
            aliasesCache.remove(delAlias);
            ConsumerConfig actualConfig = consumerConfigs.remove(delAlias);
            if (actualConfig != null) {
                actualConfig.unrefer();
            }
        } finally {
            changeAliasLock.unlock();
        }
    }

    private void deleteAlias(String delAlias) {
        if (StringUtils.isNotEmpty(alias)) {
            List<String> aliases = new ArrayList<String>(Arrays.asList(alias.split(",", -1)));
            if (aliases.contains(delAlias)) { // 删除
                aliases.remove(delAlias);
                this.alias = CommonUtils.join(aliases, ",");
            }
        }
    }

    /**
     * 得到全部的分组列表
     *
     * @return the current aliases
     */
    public List<String> currentAliases() {
        return aliasesCache;
    }
}