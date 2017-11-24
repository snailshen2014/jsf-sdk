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

import com.ipd.jsf.gd.transport.RingBufferHolder;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.monitor.Monitor;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.protocol.Protocol;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.protocol.ProtocolUtil;
import com.ipd.jsf.gd.transport.CallbackUtil;
import com.ipd.jsf.gd.transport.PooledBufHolder;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.RpcContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Title: JSF调用任务<br>
 * <p/>
 * Description: 会丢到业务线程执行<br>
 * <p/>
 */
public class JSFTask extends BaseTask {

    private final BaseServerHandler serverHandler;

    private final RequestMessage msg;

    private final Channel channel;

    private final static Logger logger = LoggerFactory.getLogger(JSFTask.class);

    public JSFTask(BaseServerHandler serverHandler, RequestMessage msg, Channel channel){
        this.serverHandler = serverHandler;
        this.msg = msg;
        this.channel = channel;
    }

    @Override
    void doRun() {
        try {
            long now = JSFContext.systemClock.now();
            Integer timeout = msg.getClientTimeout();
            if (timeout != null && now - msg.getReceiveTime() > timeout) { // 客户端已经超时的请求直接丢弃
                logger.warn("[JSF-23008]Discard request cause by timeout after receive the msg: {}", msg.getMsgHeader());
                return;
            }

            final InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
            final InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();

            // decode body
            Protocol protocol = ProtocolFactory.getProtocol(msg.getProtocolType(), msg.getMsgHeader().getCodecType());
            Invocation invocation = null;
            // 解析头部，供序列化时特殊判断
            Short consumerJsfVersion = (Short) msg.getMsgHeader().getAttrByKey(Constants.HeadKey.jsfVersion); // 客户端来源版本
            Byte consumerLanguage = (Byte) msg.getMsgHeader().getAttrByKey(Constants.HeadKey.srcLanguage); // 客户端来源语言
            try {
                if (consumerJsfVersion != null) { // 供序列化时特殊判断
                    RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION, consumerJsfVersion);
                }
                if (consumerLanguage != null) { // 供序列化时特殊判断
                    RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE, consumerLanguage);
                }
                invocation = (Invocation) protocol.decode(msg.getMsgBody(), Invocation.class.getCanonicalName());
            } finally {
                if (consumerJsfVersion != null) { // 供序列化时特殊判断
                    RpcContext.getContext().removeAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
                }
                if (consumerLanguage != null) { // 供序列化时特殊判断
                    RpcContext.getContext().removeAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE);
                }
            }
            msg.setInvocationBody(invocation);
            String className = msg.getClassName();
            String methodName = msg.getMethodName();
            String aliasName = msg.getAlias();

            //AUTH check for blacklist/whitelist
            if (!ServerAuthHelper.isValid(className, aliasName, NetUtils.toIpString(remoteAddress))) {
                throw new RpcException(msg.getMsgHeader(),
                        "[JSF-23007]Fail to pass the server auth check in server: " + localAddress
                                + ", May be your host in blacklist of server");
            }

            if (CallbackUtil.isCallbackRegister(className, methodName)) {
                CallbackUtil.msgHandle(msg, channel);
            }
            Invoker invoker = serverHandler.getOwnInvoker(className, aliasName);
            if (invoker == null) {
                //logger.warn("No such invoker! for interfaceId:{} alias:{}",className,aliasName);
                throw new RpcException(msg.getMsgHeader(), "[JSF-23006]Cannot found invoker of "
                        + BaseServerHandler.genInstanceName(className, aliasName)
                        + " in channel:" + NetUtils.channelToString(remoteAddress, localAddress)
                        + ", current invokers is " + serverHandler.getAllOwnInvoker().keySet());
            }
            invocation.addAttachment(Constants.INTERNAL_KEY_REMOTE, remoteAddress);
            invocation.addAttachment(Constants.INTERNAL_KEY_LOCAL, localAddress);
            //logger.debug("Handler HandleTask:{}", request);
            ResponseMessage responseMessage = invoker.invoke(msg); // 执行调用，包括过滤器链
            //logger.debug("Response:{}", responseMessage);

            methodName = msg.getMethodName(); // generic的方法名为$invoke已经变成了真正方法名

            ByteBuf buf = PooledBufHolder.getBuffer();
            try {
                if (consumerJsfVersion != null) { // 供序列化时特殊判断
                    RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION, consumerJsfVersion);
                }
                if (consumerLanguage != null) { // 供序列化时特殊判断
                    RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE, consumerLanguage);
                }
                buf = ProtocolUtil.encode(responseMessage, buf);
            } finally {
                if (consumerJsfVersion != null) { // 供序列化时特殊判断
                    RpcContext.getContext().removeAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
                }
                if (consumerLanguage != null) { // 供序列化时特殊判断
                    RpcContext.getContext().removeAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE);
                }
            }
            responseMessage.setMsg(buf);

            // 判断是否启动监控，如果启动则运行
            if (!CommonUtils.isFalse((String) invocation.getAttachment(Constants.INTERNAL_KEY_MONITOR))
                    && MonitorFactory.isMonitorOpen(className, methodName)) {
                String ip = NetUtils.toIpString(localAddress);
                int port = localAddress.getPort();
                Monitor monitor = MonitorFactory.getMonitor(MonitorFactory.MONITOR_PROVIDER_METRIC,
                        className, methodName, ip, port);
                if (monitor != null) { // 需要记录日志
                    boolean iserror = responseMessage.isError();
                    invocation.addAttachment(Constants.INTERNAL_KEY_INPUT, msg.getMsgHeader().getLength());
                    // 报文长度+magiccode(2) + totallength(4)
                    invocation.addAttachment(Constants.INTERNAL_KEY_OUTPUT, buf.readableBytes() + 6);
                    invocation.addAttachment(Constants.INTERNAL_KEY_RESULT, !iserror);
                    invocation.addAttachment(Constants.INTERNAL_KEY_PROTOCOL, Constants.ProtocolType.jsf.value() + "");
                    if (iserror) { // 失败
                        monitor.recordException(invocation, responseMessage.getException());
                    } else { // 成功
                        monitor.recordInvoked(invocation);
                    }
                }
            }

            // 设置服务端Ringbuffer
            RingBufferHolder serverRingBufferHolder = RingBufferHolder.getServerRingbuffer(className);
            // 开启服务端批量发送
            if (serverRingBufferHolder != null) {
                //responseMessage.setTargetAddress(BaseServerHandler.getKey(channel));
                responseMessage.setChannel(channel);
                serverRingBufferHolder.submit(responseMessage);
            } else {
                Future future1 = channel.writeAndFlush(responseMessage);

                future1.addListener(new FutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (future.isSuccess()) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Response write back {}", future.isSuccess());
                            }
                            return;
                        } else if (!future.isSuccess()) {
                            Throwable throwable = future.cause();
                            logger.error("[JSF-23009]Failed to send response to "
                                    + NetUtils.channelToString(localAddress, remoteAddress)
                                    + " for msg id: "
                                    + msg.getMsgHeader().getMsgId()
                                    + ", Cause by:", throwable);
                            //throw new RpcException("Fail to send Response msg for response:" + msg.getMsgHeader(), throwable);
                        }
                    }
                });
            }

        } catch (Throwable e) {
            if (msg != null && msg.getInvocationBody() != null) { // 解析出了请求，但是有别的异常
                logger.error("[JSF-23011]Error when run JSFTask, request to " + msg.getClassName()
                        + "/" + msg.getMethodName() + "/" + msg.getAlias() + ", error: " + e.getMessage()
                        + (channel != null ? ", channel: "
                        + NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()) : ""), e);
            }
            else { // 未解析出消息，一般是序列化异常
                logger.error("[JSF-23012]Error when run JSFTask, error: " + e.getMessage()
                        + (channel != null ? ", channel: "
                        + NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()) : ""), e);
            }

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMsgHeader(msg.getMsgHeader());
            responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
            responseMessage.setException(e);
            Future channelFuture = channel.writeAndFlush(responseMessage);
            channelFuture.addListener(new FutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("have write the error message back to clientside..");
                        }
                    } else {
                        Throwable throwable = future.cause();
                        logger.error("[JSF-23010]Failed to send error to remote  for msg id: "
                                + msg.getMsgHeader().getMsgId()
                                + " Cause by:", throwable);
                    }
                }
            });

        } finally {
            //release the byteBuf here
            ByteBuf byteBuf = msg.getMsgBody();
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }
}