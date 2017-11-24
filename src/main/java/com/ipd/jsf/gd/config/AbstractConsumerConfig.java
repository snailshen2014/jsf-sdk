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

import java.util.List;

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.client.Router;
import com.ipd.jsf.gd.msg.ResponseListener;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.msg.ConnectListener;
import com.ipd.jsf.gd.msg.ConsumerStateListener;
import com.ipd.jsf.gd.server.Invoker;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 抽象出配置到一层<br>
 *
 * Description: 子类自行实现refer unrefer 方法<br>
 */
public abstract class AbstractConsumerConfig<T> extends AbstractInterfaceConfig<T> {

    /*---------- 参数配置项开始 ------------*/

    /**
     * 调用的协议
     */
    protected String protocol = Constants.DEFAULT_PROTOCOL;

    /**
     * 直连调用地址
     */
    protected String url;

    /**
     * 是否泛化调用
     */
    protected boolean generic = false;

    /**
     * 是否异步调用
     */
    protected boolean async = false;

    /**
     * 连接超时时间
     */
    protected int connectTimeout = Constants.DEFAULT_CLIENT_CONNECT_TIMEOUT;

    /**
     * 关闭超时时间（如果还有请求，会等待请求结束或者超时）
     */
    protected int disconnectTimeout = Constants.DEFAULT_CLIENT_DISCONNECT_TIMEOUT;

    /**
     * 集群处理，默认是failover
     */
    protected String cluster = Constants.CLUSTER_FAILOVER;

    /**
     * The Retries. 失败后重试次数
     */
    protected int retries = Constants.DEFAULT_RETRIES_TIME;

    /**
     * The Loadbalance. 负载均衡
     */
    protected String loadbalance = Constants.LOADBALANCE_RANDOM;

    /**
     * 是否延迟建立长连接,
     * connect transport when invoke, but not when init
     */
    protected boolean lazy = false;

    /**
     * 粘滞连接，一个断开才选下一个
     * change transport when current is disconnected
     */
    protected boolean sticky = false;

    /**
     * 是否jvm内部调用（provider和consumer配置在同一个jvm内，则走本地jvm内部，不走远程）
     */
    protected boolean injvm = true;

    /**
     * 是否强依赖（即没有服务节点就启动失败）
     */
    protected boolean check = false;

    /**
     * 默认序列化
     */
    protected String serialization = Constants.DEFAULT_CODEC;

    /**
     * 返回值之前的listener,处理结果或者异常
     */
    protected transient List<ResponseListener> onreturn;

    /**
     * 连接事件监听器实例，连接或者断开时触发
     */
    protected transient List<ConnectListener> onconnect;

    /**
     * 客户端状态变化监听器实例，状态可用和不可以时触发
     */
    protected transient List<ConsumerStateListener> onavailable;

    /**
     * 线程池类型
     */
    protected String threadpool = Constants.THREADPOOL_TYPE_CACHED;

    /**
     * 业务线程池大小
     */
    protected int threads = Constants.DEFAULT_CLIENT_BIZ_THREADS;

    /**
     * io线程池大小
     */
    protected int iothreads;

    /**
     * Consumer给Provider发心跳的间隔
     */
    protected int heartbeat = Constants.DEFAULT_HEARTBEAT_TIME;

    /**
     * Consumer给Provider重连的间隔
     */
    protected int reconnect = Constants.DEFAULT_RECONNECT_TIME;

    /**
     * 最大数据包大小
     */
    protected  int payload = Constants.DEFAULT_PAYLOAD;

    /**
     * 路由规则引用，多个用英文逗号隔开。List<Router>
     */
    protected transient List<Router> router;

    /*-------- 下面是方法级配置 --------*/

    /**
     * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     */
    protected int concurrents = 0;

	/*---------- 参数配置项结束 ------------*/

    /**
     * 代理实现类
     */
    protected transient volatile T proxyIns;

    /**
     * 代理的Invoker对象
     */
    protected transient volatile Invoker proxyInvoker;

    /**
     * 引用一个远程服务
     *
     * @return 接口代理类
     * @throws InitErrorException 初始异常
     */
    public abstract T refer() throws InitErrorException;

    /**
     * 取消引用一个远程服务端
     */
    public abstract void unrefer();

    /**
     * Build key.
     *
     * @return the string
     */
    @Override
    public String buildKey() {
        return protocol + "://" + interfaceId + ":" + alias;
    }

    /**
     * Gets proxy class.
     *
     * @return the proxyClass
     */
    @Override
    protected Class<?> getProxyClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        if (generic) {
            return GenericService.class;
        }
        try {
            if (StringUtils.isNotBlank(interfaceId)) {
                this.proxyClass = ClassLoaderUtils.forName(interfaceId);
                if (!proxyClass.isInterface()) {
                    throw new IllegalConfigureException(21301, "consumer.interface",
                            interfaceId, "interfaceId must set interface class, not implement class");
                }
            } else {
                throw new IllegalConfigureException(21302, "consumer.interface",
                        "null", "interfaceId must be not null");
            }
        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return proxyClass;
    }

    /**
     * Is subscribe.
     *
     * @return the boolean
     */
    @Override
    public boolean isSubscribe() {
        return subscribe;
    }

    /**
     * Sets subscribe.
     *
     * @param subscribe the subscribe
     */
    @Override
    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets cluster.
     *
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * Sets cluster.
     *
     * @param cluster the cluster
     */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**
     * Gets retries.
     *
     * @return the retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Gets loadbalance.
     *
     * @return the loadbalance
     */
    public String getLoadbalance() {
        return loadbalance;
    }

    /**
     * Sets loadbalance.
     *
     * @param loadbalance the loadbalance
     */
    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    /**
     * Gets generic.
     *
     * @return the generic
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Sets generic.
     *
     * @param generic the generic
     */
    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets threads.
     *
     * @return the threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Sets threads.
     *
     * @param threads the threads
     */
    @Deprecated
    public void setThreads(int threads) {
        this.threads = threads;
    }

    /**
     * Gets iothreads.
     *
     * @return the iothreads
     */
    public int getIothreads() {
        return iothreads;
    }

    /**
     * Sets iothreads.
     *
     * @param iothreads the iothreads
     */
    public void setIothreads(int iothreads) {
        this.iothreads = iothreads;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Gets disconnect timeout.
     *
     * @return the disconnect timeout
     */
    public int getDisconnectTimeout() {
        return disconnectTimeout;
    }

    /**
     * Sets disconnect timeout.
     *
     * @param disconnectTimeout the disconnect timeout
     */
    public void setDisconnectTimeout(int disconnectTimeout) {
        this.disconnectTimeout = disconnectTimeout;
    }

    /**
     * Gets threadpool.
     *
     * @return the threadpool
     */
    public String getThreadpool() {
        return threadpool;
    }

    /**
     * Sets threadpool.
     *
     * @param threadpool the threadpool
     */
    @Deprecated
    public void setThreadpool(String threadpool) {
        this.threadpool = threadpool;
    }

    /**
     * Is check.
     *
     * @return the boolean
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Sets check.
     *
     * @param check the check
     */
    public void setCheck(boolean check) {
        this.check = check;
    }


    /**
     * Gets serialization.
     *
     * @return the serialization
     */
    public String getSerialization() {
        return serialization;
    }

    /**
     * Sets serialization.
     *
     * @param serialization the serialization
     */
    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    /**
     * Gets onreturn.
     *
     * @return the onreturn
     */
    public List<ResponseListener> getOnreturn() {
        return onreturn;
    }

    /**
     * Sets onreturn.
     *
     * @param onreturn the onreturn
     */
    public void setOnreturn(List<ResponseListener> onreturn) {
        this.onreturn = onreturn;
    }

    /**
     * Gets onconnect.
     *
     * @return the onconnect
     */
    public List<ConnectListener> getOnconnect() {
        return onconnect;
    }

    /**
     * Sets onconnect.
     *
     * @param onconnect the onconnect
     */
    public void setOnconnect(List<ConnectListener> onconnect) {
        this.onconnect = onconnect;
    }

    /**
     * Gets onavailable.
     *
     * @return the onavailable
     */
    public List<ConsumerStateListener> getOnavailable() {
        return onavailable;
    }

    /**
     * Sets onavailable.
     *
     * @param onavailable  the onavailable
     */
    public void setOnavailable(List<ConsumerStateListener> onavailable) {
        this.onavailable = onavailable;
    }

    /**
     * Is async.
     *
     * @return the boolean
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Sets async.
     *
     * @param async the async
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Is injvm.
     *
     * @return the boolean
     */
    public boolean isInjvm() {
        return injvm;
    }

    /**
     * Sets injvm.
     *
     * @param injvm the injvm
     */
    public void setInjvm(boolean injvm) {
        this.injvm = injvm;
    }

    /**
     * Is lazy.
     *
     * @return the boolean
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Sets lazy.
     *
     * @param lazy the lazy
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * Is sticky.
     *
     * @return the boolean
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Sets sticky.
     *
     * @param sticky the sticky
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * Gets reconnect.
     *
     * @return the reconnect
     */
    public int getReconnect() {
        return reconnect;
    }

    /**
     * Sets reconnect.
     *
     * @param reconnect the reconnect
     */
    public void setReconnect(int reconnect) {
        this.reconnect = reconnect;
    }

    /**
     * Gets heartbeat.
     *
     * @return the heartbeat
     */
    public int getHeartbeat() {
        return heartbeat;
    }

    /**
     * Sets heartbeat.
     *
     * @param heartbeat the heartbeat
     */
    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public int getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload the payload
     */
    public void setPayload(int payload) {
        this.payload = payload;
    }

    /**
     * Gets router.
     *
     * @return the router
     */
    public List<Router> getRouter() {
        return router;
    }

    /**
     * Sets router.
     *
     * @param router the router
     */
    public void setRouter(List<Router> router) {
        this.router = router;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public int getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     */
    public void setConcurrents(int concurrents) {
        this.concurrents = concurrents;
    }

    /**
     * 是否有并发控制需求，有就打开过滤器
     * 配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     *
     * @return 是否配置了concurrents boolean
     */
    public boolean hasConcurrents() {
        return concurrents >= 0;
    }

    /**
     * 得到方法的重试次数，默认接口配置
     *
     * @param methodName 方法名
     * @return 方法的重试次数 method retries
     */
    public int getMethodRetries(String methodName) {
        return (Integer) getMethodConfigValue(methodName, Constants.CONFIG_KEY_RETRIES,
                getRetries());
    }

    /**
     * Gets time out.
     *
     * @param methodName the method name
     * @return the time out
     */
    public int getMethodTimeout(String methodName) {
        return (Integer) getMethodConfigValue(methodName, Constants.CONFIG_KEY_TIMEOUT,
                getTimeout());
    }

    /**
     * 得到方法名对应的自定义参数列表
     *
     * @param methodName 方法名，不支持重载
     * @return method onreturn
     */
    public List<ResponseListener> getMethodOnreturn(String methodName) {
        return (List<ResponseListener>) getMethodConfigValue(methodName, Constants.CONFIG_KEY_ONRETURN,
                getOnreturn());
    }

    /**
     * Gets time out.
     *
     * @param methodName the method name
     * @return the time out
     */
    public boolean getMethodAsync(String methodName) {
        return (Boolean) getMethodConfigValue(methodName, Constants.CONFIG_KEY_ASYNC,
                isAsync());
    }

    /**
     * 除了判断自己，还有判断下面方法的自定义判断
     *
     * @return the validation
     */
    public boolean hasAsyncMethod() {
        if (isAsync()) {
            return true;
        }
        if (methods != null && methods.size() > 0) {
            for (MethodConfig methodConfig : methods.values()) {
                if (CommonUtils.isTrue(methodConfig.getAsync())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 得到实现代理类
     *
     * @return 实现代理类
     */
    public T getProxyIns() {
        return proxyIns;
    }

    /**
     * 得到实现代理类Invoker
     *
     * @return 实现代理类Invoker
     */
    public Invoker getProxyInvoker() {
        return proxyInvoker;
    }
}