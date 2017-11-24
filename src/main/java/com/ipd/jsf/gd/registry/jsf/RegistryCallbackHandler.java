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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.ipd.jsf.gd.client.Client;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.filter.limiter.LimiterFactory;
import com.ipd.jsf.gd.filter.mock.MockDataFactroy;
import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.server.BusinessPool;
import com.ipd.jsf.gd.transport.CallbackUtil;
import com.ipd.jsf.gd.transport.ClientTransport;
import com.ipd.jsf.gd.transport.ClientTransportFactory;
import com.ipd.jsf.gd.transport.JSFClientTransport;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.RpcStatus;
import com.ipd.jsf.gd.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.server.telnet.ServiceInfoTelnetHandler;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;

/**
 * Title: 注册中心回调事件处理器<br>
 *
 * Description: 部分方法挪到此类<br>
 */
public class RegistryCallbackHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistryCallbackHandler.class);

    /**
     * jsf Logger for this class
     */
    private final static JSFLogger JSF_LOGGER = JSFLoggerFactory.getLogger(RegistryCallbackHandler.class);

    /**
     * fetch consumer providers.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchConsumerProviders(JsfUrl source) {
        LOGGER.debug("Get consumer provider list fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String alias = source.getAlias();
        try {
            List<ConsumerConfig> consumerConfigs = JSFContext.getConsumerConfigs();
            for (ConsumerConfig cc : consumerConfigs) {
                if (JSFRegistryHelper.matchConsumer(cc, source)) {
                    Client client = cc.getClient();
                    Map<String, Set<Provider>> providers = client.currentProviderMap();
                    Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
                    tmpmap.put("dataVersion", JSFRegistryHelper.getProviderDataVersion(source));
                    int size = 0;
                    for (Map.Entry<String, Set<Provider>> entry : providers.entrySet()) {
                        tmpmap.put(entry.getKey(), entry.getValue());
                        size += entry.getValue().size();
                    }
                    tmpmap.put("providerSize", size);
                    return JsonUtils.toJSONString(tmpmap);
                }
            }
            return "{\"error\":\"not found consumer of " + interfaceId + ":" + alias + "\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch consumer provider!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch consumer config.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchConsumerConfig(JsfUrl source) {
        LOGGER.debug("Get consumer config fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String alias = source.getAlias();
        try {
            List<ConsumerConfig> consumerConfigs = JSFContext.getConsumerConfigs();
            for (ConsumerConfig cc : consumerConfigs) {
                if (JSFRegistryHelper.matchConsumer(cc, source)) {
                    Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
                    tmpmap.put("configVersion", JSFRegistryHelper.getConfigDataVersion(source));
                    tmpmap.put("consumer", cc);
                    tmpmap.put("globalConfig", JSFContext.getConfigMap(Constants.GLOBAL_SETTING));
                    tmpmap.put("interfaceConfig", JSFContext.getConfigMap(interfaceId));
                    String appLimit = LimiterFactory.getLimitDetails(interfaceId,false);
                    if (StringUtils.isNotEmpty(appLimit)) {
                        tmpmap.put("appLimit", appLimit);
                    }
                    Map ifaceIds = JSFContext.getClassNameIfaceIdMap();
                    if (CommonUtils.isNotEmpty(ifaceIds)) {
                        tmpmap.put("ifaceIds", ifaceIds);
                    }
                    tmpmap.put("context", JSFContext.getContext());
                    return JsonUtils.toJSONString(tmpmap);
                }
            }
            return "{\"error\":\"not found consumer of " + interfaceId + ":" + alias + "\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch consumer config!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch consumer concurrent.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchConsumerConcurrent(JsfUrl source) {
        LOGGER.debug("Get consumer concurrent fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String alias = source.getAlias();
        String methodName = source.getAttrs() != null ? source.getAttrs().get("method") : null;
        try {
            // 查看实时并发值
            if (StringUtils.isNotBlank(methodName)) { // 方法级
                Map<AbstractInterfaceConfig, ConcurrentMap<String, RpcStatus>> statuses = RpcStatus.getMethodStatuses();
                for (Map.Entry<AbstractInterfaceConfig, ConcurrentMap<String, RpcStatus>> status : statuses.entrySet()) {
                    AbstractInterfaceConfig config = status.getKey();
                    if (config instanceof ConsumerConfig
                            && JSFRegistryHelper.matchConsumer((ConsumerConfig) config, source)) {
                        ConcurrentMap<String, RpcStatus> ms = status.getValue();
                        RpcStatus rpcStatus = ms.get(methodName);
                        if (rpcStatus != null) {
                            return JsonUtils.toJSONString(rpcStatus);
                        }
                    }
                }
            } else { // 接口级
                Map<AbstractInterfaceConfig, RpcStatus> statuses = RpcStatus.getInterfaceStatuses();
                for (Map.Entry<AbstractInterfaceConfig, RpcStatus> status : statuses.entrySet()) {
                    AbstractInterfaceConfig config = status.getKey();
                    if (config instanceof ConsumerConfig
                            && JSFRegistryHelper.matchConsumer((ConsumerConfig) config, source)) {
                        return JsonUtils.toJSONString(status.getValue());
                    }
                }
            }

            // 都没有找到
            List<ConsumerConfig> consumerConfigs = JSFContext.getConsumerConfigs();
            for (ConsumerConfig cc : consumerConfigs) {
                if (JSFRegistryHelper.matchConsumer(cc, source)) {
                    int concurrents = cc.getConcurrents();
                    Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
                    if (concurrents > 0) {
                        tmpmap.put("concurrent", cc.getConcurrents());
                        tmpmap.put("info", "never called");
                    } else {
                        tmpmap.put("concurrent", concurrents + "");
                    }
                    return JsonUtils.toJSONString(tmpmap);
                }
            }
            return "{\"error\":\"not found consumer of " + interfaceId + ":" + alias + "\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch consumer concurrent!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch provider config.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchProviderConfig(JsfUrl source) {
        LOGGER.debug("Get provider config fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String alias = source.getAlias();
        try {
            List<ProviderConfig> providerConfigs = JSFContext.getProviderConfigs();
            for (ProviderConfig cc : providerConfigs) {
                if (JSFRegistryHelper.matchProvider(cc, source)) {
                    Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
                    tmpmap.put("configVersion", JSFRegistryHelper.getConfigDataVersion(source));
                    tmpmap.put("provider", cc);
                    tmpmap.put("globalConfig", JSFContext.getConfigMap(Constants.GLOBAL_SETTING));
                    tmpmap.put("interfaceConfig", JSFContext.getConfigMap(interfaceId));
                    Map ifaceIds = JSFContext.getClassNameIfaceIdMap();
                    if (CommonUtils.isNotEmpty(ifaceIds)) {
                        tmpmap.put("ifaceIds", ifaceIds);
                    }
                    String providerLimit = LimiterFactory.getLimitDetails(interfaceId,true);
                    tmpmap.put("global.provider.limit.open",LimiterFactory.isGlobalProviderLimitOpen());
                    if (StringUtils.isNotEmpty(providerLimit)) {
                        tmpmap.put("providerLimit", providerLimit);
                    }
                    tmpmap.put("context", JSFContext.getContext());
                    return JsonUtils.toJSONString(tmpmap);
                }
            }

            return "{\"error\":\"not found provider of " + interfaceId + ":" + alias + "\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch provider config!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch provider concurrent.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchProviderConcurrent(JsfUrl source) {
        LOGGER.debug("Get provider concurrent fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String alias = source.getAlias();
        String methodName = source.getAttrs() != null ? source.getAttrs().get("method") : null;
        try {
            // 查看实时并发值
            if (StringUtils.isNotBlank(methodName)) { // 方法级
                Map<AbstractInterfaceConfig, ConcurrentMap<String, RpcStatus>> statuses = RpcStatus.getMethodStatuses();
                for (Map.Entry<AbstractInterfaceConfig, ConcurrentMap<String, RpcStatus>> status : statuses.entrySet()) {
                    AbstractInterfaceConfig config = status.getKey();
                    if (config instanceof ProviderConfig
                            && JSFRegistryHelper.matchProvider((ProviderConfig) config, source)) {
                        ConcurrentMap<String, RpcStatus> ms = status.getValue();
                        RpcStatus rpcStatus = ms.get(methodName);
                        if (rpcStatus != null) {
                            return JsonUtils.toJSONString(rpcStatus);
                        }
                    }
                }
            } else { // 接口级
                Map<AbstractInterfaceConfig, RpcStatus> statuses = RpcStatus.getInterfaceStatuses();
                for (Map.Entry<AbstractInterfaceConfig, RpcStatus> status : statuses.entrySet()) {
                    AbstractInterfaceConfig config = status.getKey();
                    if (config instanceof ProviderConfig
                            && JSFRegistryHelper.matchProvider((ProviderConfig) config, source)) {
                        return JsonUtils.toJSONString(status.getValue());
                    }
                }
            }
            // 都没有找到
            List<ProviderConfig> providerConfigs = JSFContext.getProviderConfigs();
            for (ProviderConfig cc : providerConfigs) {
                if (JSFRegistryHelper.matchProvider(cc, source)) {
                    int concurrents = cc.getConcurrents();
                    Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
                    if (concurrents > 0) {
                        tmpmap.put("concurrent", cc.getConcurrents());
                        tmpmap.put("info", "never called");
                    } else {
                        tmpmap.put("concurrent", concurrents + "");
                    }
                    return JsonUtils.toJSONString(tmpmap);
                }
            }

            return "{\"error\":\"not found provider of " + interfaceId + ":" + alias + "\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch provider concurrent!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch provider interface.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchProviderInterfaceEvent(JsfUrl source) {
        LOGGER.debug("Get provider interface fetch callback event, source: {}", source);
        String interfaceId = source.getIface();
        String methodName = source.getAttrs() != null ? source.getAttrs().get("method") : null;
        try {
            ServiceInfoTelnetHandler handler = new ServiceInfoTelnetHandler();
            String result = handler.telnet(null, methodName == null ? interfaceId : interfaceId + " " + methodName);
            return result;
        } catch (Exception e) {
            LOGGER.warn("Error when fetch provider concurrent!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch server status.
     *
     * @param source
     *         the source
     * @param attrs
     *         the attrs
     * @return the string
     */
    protected String fetchServerStatus(JsfUrl source, Map<String, String> attrs) {
        LOGGER.debug("Get server status fetch callback event, source: {}, attrs: {}", source, attrs);
        try {
            if (CommonUtils.isNotEmpty(attrs)) {
                int port = Integer.parseInt(attrs.get("port"));
                ThreadPoolExecutor pool = BusinessPool.getBusinessPool(port);
                if (pool != null) {
                    LinkedHashMap poolmap = new LinkedHashMap();
                    poolmap.put("port", port);
                    poolmap.put("core", pool.getCorePoolSize());
                    poolmap.put("max", pool.getMaximumPoolSize());
                    poolmap.put("current", pool.getPoolSize());
                    poolmap.put("active", pool.getActiveCount());
                    poolmap.put("queue", pool.getQueue().size());
                    return JsonUtils.toJSONString(poolmap);
                } else {
                    return "{\"error\":\"not found business pool of port " + port + "! \"}";
                }
            }
            return "{\"error\":\"attrs is null!\"}";
        } catch (Exception e) {
            LOGGER.warn("Error when fetch server status!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch instance configs.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchInstanceConfigs(JsfUrl source) {
        LOGGER.debug("Get instance config fetch callback event, source: {}", source);
        try {
            Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
            tmpmap.put("context", JSFContext.getContext());
            tmpmap.put("globalConfig", JSFContext.getConfigMap(Constants.GLOBAL_SETTING));
            tmpmap.put("providers", JSFContext.getProviderConfigs());
            tmpmap.put("consumers", JSFContext.getConsumerConfigs());
            return JsonUtils.toJSONString(tmpmap);
        } catch (Exception e) {
            LOGGER.warn("Error when fetch instance config!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * fetch instance concurrent.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fetchInstanceConcurrent(JsfUrl source) {
        LOGGER.debug("Get instance concurrent fetch callback event, source: {}", source);
        try {
            Map tmpmap = new LinkedHashMap();
            Map<String, ClientTransport> clientpool = ClientTransportFactory.getConnectionPool();
            if (CommonUtils.isNotEmpty(clientpool)) {
                Map clientmap = new LinkedHashMap();
                for (Map.Entry<String, ClientTransport> entry : clientpool.entrySet()) {
                    ClientTransport t1 = entry.getValue();
                    if (t1 instanceof JSFClientTransport) {  // future列表
                        JSFClientTransport transport = (JSFClientTransport) t1;
                        Channel channel = transport.getChannel();
                        clientmap.put(NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                                transport.getFutureMapSize());
                    }
                }
                tmpmap.put("clientFutureSize", clientmap);
            }

            ConcurrentMap<Integer, ThreadPoolExecutor> serverpool = BusinessPool.getBusinessPools();
            if (CommonUtils.isNotEmpty(serverpool)) {
                // 线程池情况列表
                Map servermap = new LinkedHashMap();
                for (Map.Entry<Integer, ThreadPoolExecutor> entry : serverpool.entrySet()) {
                    Map poolmap = new LinkedHashMap();
                    ThreadPoolExecutor pool = entry.getValue();
                    poolmap.put("port", entry.getKey());
                    poolmap.put("core", pool.getCorePoolSize());
                    poolmap.put("max", pool.getMaximumPoolSize());
                    poolmap.put("current", pool.getPoolSize());
                    poolmap.put("active", pool.getActiveCount());
                    poolmap.put("queue", pool.getQueue().size());

                    servermap.put(entry.getKey(), poolmap);
                }
                tmpmap.put("serverThreadPool", servermap);
            }

            ThreadPoolExecutor pool = CallbackUtil.getCallbackThreadPool(false);
            if (pool != null) {
                Map poolmap = new LinkedHashMap();
                poolmap.put("core", pool.getCorePoolSize());
                poolmap.put("max", pool.getMaximumPoolSize());
                poolmap.put("current", pool.getPoolSize());
                poolmap.put("active", pool.getActiveCount());
                poolmap.put("queue", pool.getQueue().size());
                tmpmap.put("callbackPool", poolmap);
            }
            return JsonUtils.toJSONString(tmpmap);
        } catch (Exception e) {
            LOGGER.warn("Error when fetch instance concurrent!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire instance config update event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireInstanceConfigUpdateEvent(JsfUrl source) {
        LOGGER.info("Get instance config update callback event, source: {}", source);
        try {
            Map<String, String> map = source.getAttrs();
            JSF_LOGGER.info("All config is {}", map);
            if (CommonUtils.isNotEmpty(map)) {
                // 是否开启debug日志输出，默认false
                if (map.containsKey(JSFContext.KEY_DEBUG_MODE)) {
                    String debugmode = map.get(JSFContext.KEY_DEBUG_MODE);
                    if (CommonUtils.isTrue(debugmode)) { // 开
                        JSFContext.put(JSFContext.KEY_DEBUG_MODE, true);
                        JSFLogger.print = false;
                    } else if (CommonUtils.isFalse(debugmode)) { // 关
                        JSFContext.put(JSFContext.KEY_DEBUG_MODE, false);
                        JSFLogger.print = false;
                    }
                }

                // 可添加其它属性
            }
            return "";
        } catch (Exception e) {
            LOGGER.warn("Error when fire instance recover!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire instance reset scheduled service event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireInstanceResetScheduledServiceEvent(JsfUrl source) {
        LOGGER.info("Get instance reset scheduled service callback event, source: {}", source);
        try {
            ScheduledService.reset();
            return "Reset scheduled service finished!";
        } catch (Exception e) {
            LOGGER.warn("Error when fire instance reset scheduled service!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire instance notification event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireInstanceNotificationEvent(JsfUrl source) {
        try {
            Map<String, String> map = source.getAttrs();
            if (CommonUtils.isNotEmpty(map)) {
                String message = map.get("msg");
                if (StringUtils.isNotBlank(message)) {
                    String level = map.get("level");
                    if ("info".equalsIgnoreCase(level)) {
                        LOGGER.info(message);
                    } else if ("warn".equalsIgnoreCase(level)) {
                        LOGGER.warn(message);
                    } else {
                        LOGGER.error(message);
                    }
                }
            }
        } catch (Exception e) {
            return ExceptionUtils.toString(e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Fire app limit switch event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireSwitchAppLimitEvent(JsfUrl source) {
        JSF_LOGGER.info("Get set switch of app limit callback event, source: {}", source);
        try {
            Map<String, String> attrs = source.getAttrs();
            LimiterFactory.addMonitorLimiterRule(source.getIface(), attrs.get("method"), source.getAlias(),
                    attrs.get("appId"), Boolean.parseBoolean(attrs.get("canInvoke"))); // 服务按app限制更新
            return "Switch app limit finished";
        } catch (Exception e) {
            LOGGER.warn("Error when switch app limit!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire mock result switch event.
     *
     * @param source
     *         the source
     * @return the string
     */
    protected String fireSwitchMockResult(JsfUrl source) {
        JSF_LOGGER.info("Get set switch of mock result callback event, source: {}", source);
        try {
            Map<String, String> attrs = source.getAttrs();
            MockDataFactroy.addMockResult(source.getIface(), attrs.get("method"), source.getAlias(), attrs.get
                    ("mockValue"), "1".equals(attrs.get("mockOpen")));// 服务mock设置, 0关1开
            return StringUtils.EMPTY;
        } catch (Exception e) {
            LOGGER.warn("Error when switch app limit!", e);
            return ExceptionUtils.toString(e);
        }
    }

    /**
     * Fire server attribute update event.
     *
     * @param source
     *         the source
     * @param attrs
     *         the config
     */
    protected void fireServerAttributeUpdateEvent(JsfUrl source, Map<String, String> attrs) {
        LOGGER.info("Get server attribute update callback event, source: {}, data: {}", source, attrs);
        if (CommonUtils.isNotEmpty(attrs)) { // 需要区分alias
            try {
                int port = Integer.parseInt(attrs.get("port"));
                ThreadPoolExecutor executor = BusinessPool.getBusinessPool(port);
                if (executor != null) {
                    if (attrs.containsKey("core")) {
                        int coreNew = Integer.parseInt(attrs.get("core"));
                        if (coreNew != executor.getCorePoolSize()) {
                            LOGGER.info("Core pool size of business pool at port {} change from {} to {}",
                                    new Object[]{port, executor.getCorePoolSize(), coreNew});
                            executor.setCorePoolSize(coreNew);
                        }
                    }
                    if (attrs.containsKey("max")) {
                        int maxNew = Integer.parseInt(attrs.get("max"));
                        if (maxNew != executor.getMaximumPoolSize()) {
                            LOGGER.info("Maximum pool size of business pool at port {} change from {} to {}",
                                    new Object[]{port, executor.getMaximumPoolSize(), maxNew});
                            executor.setMaximumPoolSize(maxNew);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Fire server attribute update event error!", e);
            }
        }
    }
}