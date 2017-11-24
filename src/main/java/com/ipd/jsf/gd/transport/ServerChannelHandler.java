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

import java.io.IOException;
import java.util.List;

import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.msg.ConnectListener;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Title: ServerChannelHandler处理服务端连接，消息读取等信息<br>
 * <p/>
 * Description: 如果收到是字符串，转到telnet执行<br>
 * <p/>
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private ServerTransportConfig transportConfig;

    private BaseServerHandler serverHandler;

    private final List<ConnectListener> connectListeners;

    public ServerChannelHandler(ServerTransportConfig serverTransportConfig) {
        this.transportConfig = serverTransportConfig;
        this.connectListeners = serverTransportConfig.getConnectListeners();
        serverHandler = BaseServerHandler.getInstance(transportConfig);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        Channel channel = ctx.channel();
        if (msg instanceof RequestMessage) {
            RequestMessage requestMsg = (RequestMessage) msg;
            //
            if (handleOtherMsg(ctx, requestMsg)) return;

            serverHandler.handlerRequest(channel, requestMsg);


        } else if (msg instanceof ResponseMessage) {
            //receive the callback ResponseMessage
            ResponseMessage responseMsg = (ResponseMessage) msg;
            if (responseMsg.getMsgHeader().getMsgType() != Constants.CALLBACK_RESPONSE_MSG) {
                throw new RpcException(responseMsg.getMsgHeader(), "Can not handle normal response message" +
                        " in server channel handler : " + responseMsg.toString());
            }
            //find the transport
            JSFClientTransport clientTransport = CallbackUtil.getClientTransport(channel);
            if (clientTransport != null) {
                clientTransport.receiveResponse(responseMsg);
            } else {
                logger.error("no such clientTransport for channel:{}", channel);
                throw new RpcException(responseMsg.getMsgHeader(), "No such clientTransport");
            }
        } else {
            throw new RpcException("Only support base message");
        }

    }

    /*
     *
     */
    private boolean handleOtherMsg(ChannelHandlerContext ctx, RequestMessage requestMsg) {

        int msgType = requestMsg.getMsgHeader().getMsgType();
        if (msgType == Constants.REQUEST_MSG) return false; // 正常的请求
        Channel channel = ctx.channel();
        ResponseMessage response = null;
        switch (msgType) {
            case Constants.SHAKEHAND_MSG:
                response = new ResponseMessage();
                response.getMsgHeader().setMsgType(Constants.SHAKEHAND_RESULT_MSG);
                response.getMsgHeader().setMsgId(requestMsg.getRequestId());
                //DO SHAKEHAND CHECK HERE

                break;

            case Constants.HEARTBEAT_REQUEST_MSG:
                response = MessageBuilder.buildHeartbeatResponse(requestMsg);
                break;
            default:
                throw new RpcException(requestMsg.getMsgHeader(), " no such msgType:" + msgType);

        }
        channel.writeAndFlush(response);
        return true;
    }

    /*
     *handle the error
     */
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) {
        Channel channel = ctx.channel();
        if (cause instanceof IOException) {
            logger.warn("catch IOException at {} : {}",
                    NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()),
                    cause.getMessage());
        } else if (cause instanceof RpcException) {
            RpcException rpc = (RpcException) cause;
            MessageHeader header = rpc.getMsgHeader();
            if (header != null) {
                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.getMsgHeader().copyHeader(header);
                responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
                String causeMsg = cause.getMessage();
                String channelInfo = BaseServerHandler.getKey(ctx.channel());
                String causeMsg2 = "Remote Error Channel:" + channelInfo + " cause: " + causeMsg;
                ((RpcException) cause).setErrorMsg(causeMsg2);
                responseMessage.setException(cause);
                ChannelFuture channelFuture = ctx.writeAndFlush(responseMessage);
                channelFuture.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            if(logger.isTraceEnabled()) {
                                logger.trace("have write the error message back to clientside..");
                            }
                            return;
                        } else {
                            logger.error("fail to write error back status: {}", future.isSuccess());

                        }
                    }
                });
            }
        } else {
            logger.warn("catch " + cause.getClass().getName() + " at {} : {}",
                    NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()),
                    cause.getMessage());
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //logger.info("connected from {}", NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()));
        BaseServerHandler.addChannel(channel);
        if (connectListeners != null) {
            serverHandler.getBizThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    for (ConnectListener connectListener : connectListeners) {
                        try {
                            connectListener.connected(ctx);
                        } catch (Exception e) {
                            logger.warn("Failed to call connect listener when channel active", e);
                        }
                    }
                }
            });
        }
    }


    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info("Disconnected from {}", NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()));
        BaseServerHandler.removeChannel(channel);
        if (connectListeners != null) {
            serverHandler.getBizThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    for (ConnectListener connectListener : connectListeners) {
                        try {
                            connectListener.disconnected(ctx);
                        } catch (Exception e) {
                            logger.warn("Failed to call connect listener when channel inactive", e);
                        }
                    }
                }
            });
        }
        CallbackUtil.removeTransport(channel);
    }

    public BaseServerHandler getServerHandler() {
        return serverHandler;
    }
}