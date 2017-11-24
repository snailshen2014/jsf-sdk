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
package com.ipd.jsf.gd.server;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.transport.ServerTransportConfig;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Title: 基础ServerHandler<br>
 * <p/>
 * Description: 简单的注册和订阅，分发task到线程池<br>
 * <p/>
 */
public class BaseServerHandler implements ServerHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(BaseServerHandler.class);

    /**
     * 当前handler的Invoker列表 一个接口+alias对应一个Invoker
     */
    private Map<String, Invoker> instanceMap = new ConcurrentHashMap<String, Invoker>();

    /**
     * 一个端口对应一个ServerHandler
     */
    private static Map<String,BaseServerHandler> serverHandlerMap = new ConcurrentHashMap<String, BaseServerHandler>();

    /**
     * 业务线程池（一个端口一个）
     */
    private final ExecutorService bizThreadPool; // 业务线程池

    /**
     * 长连接列表
     */
    private static final ConcurrentHashMap<String, Channel> channelsMap = new ConcurrentHashMap<String, Channel>();

    /**
     * Server Transport Config
     */
    private final ServerTransportConfig serverTransportConfig;

    /**
     * Instantiates a new Base server handler.
     *
     * @param transportConfig the transport config
     */
    public BaseServerHandler(ServerTransportConfig transportConfig){
        this.serverTransportConfig = transportConfig;
        this.bizThreadPool = BusinessPool.getBusinessPool(this.serverTransportConfig);
    }

    /**
     * Get instance.
     *
     * @param transportConfig the transport config
     * @return the base server handler
     */
    public synchronized static BaseServerHandler getInstance(ServerTransportConfig transportConfig) {
        BaseServerHandler serverHandler = null;
        serverHandler = serverHandlerMap.get(transportConfig.getServerTransportKey());
        if (serverHandler == null) {
            serverHandler = new BaseServerHandler(transportConfig);
            serverHandlerMap.put(transportConfig.getServerTransportKey(), serverHandler);
        }
        return serverHandler;
    }

    /**
     * Get invoker.
     *
     * @param interfaceId the interface id
     * @param alias the alias
     * @return the invoker
     */
    public static Invoker getInvoker(String interfaceId, String alias) {
        return InvokerHolder.getInvoker(interfaceId, alias);
    }

    @Override
    public void registerProcessor(String instanceName, Invoker instance) {
        instanceMap.put(instanceName, instance);
        InvokerHolder.cacheInvoker(instanceName, instance);
    }

    @Override
    public void unregisterProcessor(String instanceName) {
        if (instanceMap.containsKey(instanceName)) {
            instanceMap.remove(instanceName);
            InvokerHolder.invalidateInvoker(instanceName);
        } else {
            throw new RuntimeException("[JSF-23005]No such invoker key when unregister processor:" + instanceName);
        }
    }

    /**
     * 得到全部invoker
     *
     * @return all invoker
     */
    public static Map<String, Invoker> getAllInvoker() {
        return new HashMap<String, Invoker>(InvokerHolder.getAllInvoker());
    }

    /**
     * 得到当前自己handler里的Invoker
     *
     * @param interfaceId the interface id
     * @param alias the alias
     * @return the invoker
     */
    public Invoker getOwnInvoker(String interfaceId, String alias) {
        String key = genInstanceName(interfaceId, alias);
        return instanceMap.get(key);
    }


    /**
     * 得到当前handler全部invoker
     *
     * @return all invoker
     */
    public Map<String, Invoker> getAllOwnInvoker() {
        return instanceMap;
    }

    /**
     * Gen instance name.
     *
     * @param interfaceId the interface id
     * @param alias the alias
     * @return the string
     */
    public static String genInstanceName(String interfaceId,String alias){
        if( interfaceId == null || interfaceId.trim().length() <= 0){
            throw new RpcException("interfaceId cannot be null!");
        }
        if( alias == null || alias.trim().length() <=0){
            return interfaceId;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(interfaceId);
        builder.append("/");
        builder.append(alias);
        return builder.toString();
    }

    public void shutdown() {
        if(!bizThreadPool.isShutdown()) {
            logger.debug("ServerHandler's business thread pool shutdown..");
            bizThreadPool.shutdown();
        }
    }

    public void handlerRequest(Channel channel,Object requestMsg) {
        if(logger.isTraceEnabled()) {
            logger.trace("handler the Request in ServerChannelHandler..");
        }
        RequestMessage msg = (RequestMessage)requestMsg;
        try {

            BaseTask task = getHandleTask(channel,msg);
            submitTask(task);
            // TODO task的优先级设置 此时未反序列化，不知道接口和方法等信息
        } catch (Exception e) {
            RpcException rpcException = ExceptionUtils.handlerException(msg.getMsgHeader(), e);
            throw rpcException;
        }
    }

    /**
     * Submit task.
     *
     * @param task the task
     */
    protected void submitTask(BaseTask task){
        bizThreadPool.submit(task);
    }

    /**
     * Gets handle task.
     *
     * @param channel the channel
     * @param msg the msg
     * @return the handle task
     */
    private BaseTask getHandleTask(final Channel channel, final RequestMessage msg) {
        Constants.ProtocolType protocolType = Constants.ProtocolType.valueOf(msg.getProtocolType());
        BaseTask task = null;
        switch (protocolType){
            case jsf:
                task = new JSFTask(this, msg, channel);
                break;
            case dubbo:
                task = new DubboTask(this, msg, channel);
                break;
            case http:
                task = new JSFHttpTask(this, msg, channel);
                break;
            default:
                throw new JSFCodecException("[JSF-23004]Unsupported protocol type of task:" + protocolType);
        }
        return task;
    }


    /**
     * Add channel.
     *
     * @param channel the channel
     */
    public static void addChannel(Channel channel){
        String key = getKey(channel);
        channelsMap.put(key,channel);
    }

    /**
     * Remove channel.
     *
     * @param channel the channel
     * @return the channel
     */
    public static Channel removeChannel(Channel channel){
        String key = getKey(channel);
        return channelsMap.remove(key) ;
    }

    /**
     * Remove channel by key.
     *
     * @param key the key
     * @return the channel
     */
    public static Channel removeChannelByKey(String key){
        return channelsMap.remove(key);
    }

    /**
     * Get key.
     *
     * @param channel the channel
     * @return the string
     */
    public static String getKey(Channel channel){
        InetSocketAddress local = (InetSocketAddress)channel.localAddress();
        InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
        StringBuilder sb = new StringBuilder();
        sb.append(NetUtils.toIpString(address));
        sb.append(":");
        sb.append(address.getPort());
        sb.append(" --> ");
        sb.append(NetUtils.toIpString(local));
        sb.append(":");
        sb.append(local.getPort());

        String key = sb.toString();
        return key;
    }

    /**
     * Get all channel.
     *
     * @return the list
     */
    public static List<Channel> getAllChannel(){
        Collection<Channel> channels = channelsMap.values();
        List<Channel> channelList = new ArrayList<Channel>(channels);
        return channelList;
    }

    /**
     * Get channel by key.
     *
     * @param key the key
     * @return the channel
     */
    public static Channel getChannelByKey(String key){
        return channelsMap.get(key);
    }

    /**
     * 丢到业务线程池执行
     *
     * @return
     */
    public ExecutorService getBizThreadPool() {
        return bizThreadPool;
    }

    /**
     * 得到配置
     *
     * @return 配置
     */
    public ServerTransportConfig getServerTransportConfig() {
        return serverTransportConfig;
    }
}