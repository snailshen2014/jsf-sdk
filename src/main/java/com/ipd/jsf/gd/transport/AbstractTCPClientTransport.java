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
package com.ipd.jsf.gd.transport;

import com.ipd.jsf.gd.client.MsgFuture;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.ClientClosedException;
import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: TCP类型的长连接<br>
 * <p/>
 * Description: 子类可自行实现调用方法<br>
 * <p/>
 */
abstract class AbstractTCPClientTransport extends AbstractClientTransport {

    /**
     * slf4j logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTCPClientTransport.class);

    /**
     * 请求id计数器（一个Transport一个）
     */
    private final AtomicInteger requestId = new AtomicInteger();

    private final ConcurrentHashMap<Integer, MsgFuture> futureMap = new ConcurrentHashMap<Integer, MsgFuture>();

    /**
     * 构造函数
     *
     * @param clientTransportConfig
     *         客户端配置
     */
    protected AbstractTCPClientTransport(ClientTransportConfig clientTransportConfig) {
        super(clientTransportConfig);
    }

    @Override
    public ResponseMessage send(BaseMessage msg, int timeout) {
        Integer msgId = null;
        try {
            super.currentRequests.incrementAndGet();
            msgId = genarateRequestId();
            msg.setRequestId(msgId);

            MsgFuture<ResponseMessage> future = doSendAsyn(msg, timeout);
            //futureMap.putIfAbsent(msgId,future); 子类已实现
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RpcException("[JSF-22113]Client request thread interrupted");
        } catch (ClientTimeoutException e) {
            try {
                if (msgId != null) {
                    futureMap.remove(msgId);
                }
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            throw e;
        } finally {
            super.currentRequests.decrementAndGet();
        }
    }

    /**
     * 长连接默认的异步调用方法
     *
     * @param msg
     *         消息
     * @param timeout
     *         超时时间
     * @return 返回结果Future
     */
    abstract MsgFuture doSendAsyn(BaseMessage msg, int timeout);

    @Override
    public MsgFuture sendAsyn(BaseMessage msg, int timeout) {
        Integer msgId = null;
        try {
            super.currentRequests.incrementAndGet();
            msgId = genarateRequestId();
            msg.setRequestId(msgId);
            MsgFuture<ResponseMessage> future = doSendAsyn(msg, timeout);
            future.setAsyncCall(true); // 标记为异步调用
            return future;
        } catch (ClientTimeoutException e) {
            throw e;
        } finally {
            super.currentRequests.decrementAndGet();
        }
    }

    /*
     *handle the Response
     */
    public void receiveResponse(ResponseMessage msg) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("receiveResponse..{}", msg);
        }
        Integer msgId = msg.getRequestId();
        MsgFuture future = futureMap.get(msgId);
        if (future == null) {
            LOGGER.warn("[JSF-22114]Not found future which msgId is {} when receive response. May be " +
                    "this future have been removed because of timeout", msgId);
           if( msg != null && msg.getMsgBody() != null ){
                msg.getMsgBody().release();
            }
            //throw new RpcException("No such Future maybe have been removed for Timeout..");
        } else {
            future.setSuccess(msg);
            futureMap.remove(msgId);
        }
    }

    /*
     *different FutureMap for different Request msg type
     */
    protected void addFuture(BaseMessage msg, MsgFuture msgFuture) {
        int msgType = msg.getMsgHeader().getMsgType();
        Integer msgId = msg.getMsgHeader().getMsgId();
        if (msgType == Constants.REQUEST_MSG
                || msgType == Constants.CALLBACK_REQUEST_MSG
                || msgType == Constants.HEARTBEAT_REQUEST_MSG) {
            this.futureMap.put(msgId, msgFuture);

        } else {
            LOGGER.error("cannot handle Future for this Msg:{}", msg);
        }


    }

    /*
     *impl
     */
    private int genarateRequestId() {

        return requestId.getAndIncrement();
    }

    /*
     * check the future map
     */
    public void checkFutureMap() {
        long current = JSFContext.systemClock.now();
        Set<Integer> keySet = futureMap.keySet();
        for (Integer msgId : keySet) {
            MsgFuture future = futureMap.get(msgId);
            if (future != null && future.isAsyncCall()) { // 异步调用
                // 当前时间减去初始化时间 大于 超时时间  说明已经超时
                if (current - future.getGenTime() > future.getTimeout()) {
                    LOGGER.debug("remove timeout future:{} from the FutureMap", future);
                    MsgFuture removedFuture = futureMap.remove(msgId);
                    //防止之前被处理过，这里判断下
                    if(!removedFuture.isDone()){
                        removedFuture.setFailure(removedFuture.clientTimeoutException(true));
                    }
                    removedFuture.releaseIfNeed();
                }
            }
        }
    }

    /**
     * 设置客户端选项
     *
     * @param transportConfig
     *         the transport config
     */
    public ClientTransport setClientTransportConfig(ClientTransportConfig transportConfig) {
        this.clientTransportConfig = transportConfig;
        return this;
    }

    /**
     * 连接断开后，已有请求都不再等待
     */
    public void removeFutureWhenChannelInactive() {
        LOGGER.debug("Interrupt wait of all futures : {} ", futureMap.size());
        Exception e = new ClientClosedException("[JSF-22112]Channel " + NetUtils.channelToString(localAddress, remoteAddress)
                + " has been closed, remove future when channel inactive");
        for (Map.Entry<Integer, MsgFuture> entry : futureMap.entrySet()) {
            MsgFuture future = entry.getValue();
            if (!future.isDone()) {
                future.setFailure(e);
            }
        }
    }

    /**
     * 得到当前Future列表的大小
     *
     * @return Future列表的大小
     */
    public int getFutureMapSize() {
        return futureMap.size();
    }
}