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

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.*;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


/**
 * Title: 调用端channel处理器<br>
 * <p/>
 * Description: 处理建立/断开连接，收到数据等事件<br>
 * <p/>
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);

    private JSFClientTransport clientTransport;
    
    private List<ConnectListener> connectListeners;

    public ClientChannelHandler(){
        
    }

    public ClientChannelHandler(JSFClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        if (clientTransport != null) {
            this.connectListeners = clientTransport.getConfig().getConnectListeners();
        }
    }

    public ClientChannelHandler(ClientTransportConfig clientTransportConfig) {
        this.connectListeners = clientTransportConfig.getConnectListeners();
    }

    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (connectListeners != null) {
            CallbackUtil.getCallbackThreadPool().execute(new Runnable() {
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
        logger.info("Channel inactive: {}", channel);
        clientTransport.removeFutureWhenChannelInactive(); // 结束已有请求
        if (connectListeners != null) {
            CallbackUtil.getCallbackThreadPool().execute(new Runnable() {
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
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        try {
            if(msg instanceof ResponseMessage){
                ResponseMessage responseMessage = (ResponseMessage) msg;
                if(responseMessage.getMsgHeader().getMsgType() == Constants.SHAKEHAND_RESULT_MSG){
                    //TODO:set HandShake status here
                    // this.clientTransport.setShakeHandStatus();
                }
                clientTransport.receiveResponse(responseMessage);

            } else if (msg instanceof RequestMessage) {
                RequestMessage request = (RequestMessage) msg;
                // handle heartbeat Request (dubbo)
                if(request.isHeartBeat()) {
                    ResponseMessage response = MessageBuilder.buildHeartbeatResponse(request);
                    channel.writeAndFlush(response);
                }
                // handle the callback Request
                else if (request.getMsgHeader().getMsgType() == Constants.CALLBACK_REQUEST_MSG) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("handler callback request...");
                    }
                    ClientCallbackHandler.getInstance().handleCallback(channel, request);
                } else {
                    throw new RpcException(request.getMsgHeader(), "Should receive callback msg in channel "
                            + NetUtils.channelToString(channel.localAddress(), channel.remoteAddress())
                            + "! " + request.toString());
                }

            } else if (msg instanceof BaseMessage) {
                BaseMessage base = (BaseMessage) msg;
                if (logger.isTraceEnabled()) {
                    logger.trace("msg id:{},msg type:{}", base.getMsgHeader().getMsgId(), base.getMsgHeader().getMsgType());
                }
                throw new RpcException(base.getMsgHeader(), "error type of BaseMessage...");
            } else {
                logger.error("not a type of CustomMsg ...:{} ",msg);
                throw new RpcException("error type..");
                //ctx.
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            BaseMessage base = (BaseMessage)msg;
            MessageHeader header = base != null? base.getMsgHeader():null;
            RpcException rpcException = ExceptionUtils.handlerException(header,e);
            throw rpcException;
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("event triggered:{}",evt);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        if (cause instanceof IOException) {
            logger.warn("catch IOException at {} : {}",
                    NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                    cause.getMessage());
        } else if (cause instanceof RpcException) {
            RpcException rpcException = (RpcException) cause;
            final MessageHeader header = rpcException.getMsgHeader();
            if (header != null && header.getMsgType() == Constants.CALLBACK_REQUEST_MSG) {
                ResponseMessage response = new ResponseMessage();
                response.getMsgHeader().copyHeader(header);
                response.getMsgHeader().setMsgType(Constants.CALLBACK_RESPONSE_MSG);
                Future future = channel.writeAndFlush(response);
                future.addListener(new FutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (future.isSuccess()) {
                            logger.debug("error of callback msg has been send to serverside..{}", header);
                            return;
                        } else {
                            logger.error("error of callback msg to the serverSide have failed. {}", header);
                            logger.error(cause.getMessage(), cause);
                        }
                    }
                });
            }
        } else {
            logger.warn("catch " + cause.getClass().getName() + " at {} : {}",
                    NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()),
                    cause.getMessage());
        }
    }

    public void setClientTransport(JSFClientTransport transport){
        this.clientTransport = transport;
    }
}