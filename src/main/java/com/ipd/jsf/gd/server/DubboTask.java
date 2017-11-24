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

import java.net.InetSocketAddress;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.monitor.Monitor;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.transport.PooledBufHolder;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.protocol.DubboProtocol;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class DubboTask extends BaseTask {

    /**
     * slf4j Logger for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(DubboTask.class);

    private final BaseServerHandler serverHandler;

    private final RequestMessage msg;

    private final Channel channel;

    public DubboTask(BaseServerHandler serverHandler, RequestMessage msg, Channel channel) {
        this.serverHandler = serverHandler;
        this.msg = msg;
        this.channel = channel;

    }

    @Override
    void doRun() {
        try {
            ResponseMessage responseMessage = null;
            // 心跳包 在IO线程已处理掉
            // 不再decode，io线程中已完成
            if(logger.isTraceEnabled()){
                logger.trace("Handler HandleTask:{}", msg);
            }
            String className = msg.getClassName();
            String aliasName = msg.getAlias();
            Invoker invoker = serverHandler.getOwnInvoker(className, aliasName);

            final InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
            final InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();

            Invocation invocation = msg.getInvocationBody();
            if (invoker == null) {
                String erromsg = "[JSF-23006]Cannot found invoker of "
                        + BaseServerHandler.genInstanceName(className, aliasName)
                        + " in channel:" + NetUtils.channelToString(remoteAddress, localAddress)
                        + ", current invokers is " + serverHandler.getAllOwnInvoker().keySet();
                logger.error(erromsg);
                responseMessage = MessageBuilder.buildResponse(msg);
                responseMessage.setException(jsfToSaf(new RpcException(msg.getMsgHeader(), erromsg), invocation));
            } else {
                invocation.addAttachment(Constants.INTERNAL_KEY_REMOTE, remoteAddress);
                invocation.addAttachment(Constants.INTERNAL_KEY_LOCAL, localAddress);
                responseMessage = invoker.invoke(msg);

                Throwable throwable = responseMessage.getException();
                if (throwable != null) {
                    responseMessage.setException(jsfToSaf(throwable, invocation));
                }

                // 提前序列化response
                byte codeType = (byte) responseMessage.getMsgHeader().getCodecType();
                DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(
                        Constants.ProtocolType.dubbo.value(), codeType);
                ByteBuf buf = PooledBufHolder.getBuffer();
                buf = protocol.encode(responseMessage, buf);
                responseMessage.setMsg(buf);

                // 判断是否启动监控，如果启动则运行
                String methodName = msg.getMethodName(); // generic的方法名为$invoke已经变成了真正方法名
                if (!CommonUtils.isFalse((String) invocation.getAttachment(Constants.INTERNAL_KEY_MONITOR))
                        && MonitorFactory.isMonitorOpen(className, methodName)) {
                    String ip = NetUtils.toIpString(localAddress);
                    int port = localAddress.getPort();
                    Monitor monitor = MonitorFactory.getMonitor(MonitorFactory.MONITOR_PROVIDER_METRIC,
                            className, methodName, ip, port);
                    if (monitor != null) { // 需要记录日志
                        boolean isError = responseMessage.isError();
                        invocation.addAttachment(Constants.INTERNAL_KEY_INPUT, msg.getMsgHeader().getLength());
                        // 报文长度 + 16（header)
                        invocation.addAttachment(Constants.INTERNAL_KEY_OUTPUT, buf.readableBytes() + 16);
                        invocation.addAttachment(Constants.INTERNAL_KEY_RESULT, !isError);
                        invocation.addAttachment(Constants.INTERNAL_KEY_PROTOCOL, Constants.ProtocolType.dubbo.value() + "");
                        if (isError) { // 失败
                            monitor.recordException(invocation, responseMessage.getException());
                        } else { // 成功
                            monitor.recordInvoked(invocation);
                        }
                    }
                }
            }
            if(logger.isTraceEnabled()) {
                logger.trace("Response:{}", responseMessage);
            }
            Future future1 = channel.writeAndFlush(responseMessage);
            future1.addListener(new FutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Response write back {}", future.isSuccess());
                        }
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

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            RpcException rpcException = ExceptionUtils.handlerException(msg.getMsgHeader(), e);
            throw rpcException;
        }
    }


    private Throwable jsfToSaf(Throwable throwable, Invocation invocation) {
        if (throwable instanceof RpcException) {
            // 转为dubbo的错误
            RpcException jsfException = (RpcException) throwable;
            /*Throwable cause = jsfException.getCause();
            String dubboVersion = (String) invocation.getAttachment("dubboVersion");
            // 我们认为2.4.10的是SAF1.0.9+以上的，其它为自己引用的com.alibaba
            boolean jdservice = "2.4.10".equals(dubboVersion);

            if (cause != null) {
                throwable = jdservice ? new RpcException(jsfException.getMessage(),
                        cause) : new RuntimeException(jsfException.getMessage(), cause);
            } else {
                throwable = jdservice ? new RpcException(jsfException.getMessage())
                        : new RuntimeException(jsfException.getMessage());
            }*/
            throwable = new RuntimeException(jsfException.getMessage(), jsfException.getCause());
        }
        return throwable;
    }
}