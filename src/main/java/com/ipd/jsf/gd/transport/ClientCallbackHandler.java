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
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.protocol.Protocol;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

 /**
 * Title: Callback处理<br>
 * <p/>
 * Description: handler the callback in a threadPool.<br>
 * <p/>
 */
public class ClientCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(ClientCallbackHandler.class);

    private static ConcurrentHashMap<String,Callback> callbackMap = new ConcurrentHashMap<String,Callback>();



    private static ClientCallbackHandler callbackHandler;

    /*
     *
     */
    public static void registerCallback(String key,Callback callback){
        callbackMap.put(key,callback);
    }



    /*
     *
     */
    public void handleCallback(final Channel channel,final RequestMessage msg){
         String callbackInsId = (String)msg.getMsgHeader().getAttrByKey(Constants.HeadKey.callbackInsId);
        final Callback callback = callbackMap.get(callbackInsId);
        CallbackUtil.getCallbackThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (logger.isTraceEnabled()) {
                    logger.trace("Here we handle the callback request in CallbackHandler. {}", msg);
                }
                //decode invocation here.
                Protocol protocol = ProtocolFactory.getProtocol(msg.getProtocolType(), msg.getMsgHeader().getCodecType());
                final ResponseMessage response = new ResponseMessage();
                ByteBuf bytebuf = msg.getMsgBody();
                try {
                    Invocation invocation = (Invocation) protocol.decode(bytebuf, Invocation.class.getCanonicalName());
                    msg.setInvocationBody(invocation);

                    Object s = callback.notify(invocation.getArgs()[0]);
                    response.setResponse(s);
                } catch (Exception e) {
                    logger.error("Callback handler catch exception in channel "
                            + NetUtils.channelToString(channel.remoteAddress(), channel.localAddress())
                            + ", error message is :" + e.getMessage(), e);
                    RpcException rpcException = ExceptionUtils.handlerException(msg.getMsgHeader(), e);
                    response.setException(rpcException);
                } finally {
                    if (bytebuf != null) {
                        bytebuf.release();
                    }
                }
                //write the callback response to the serverside

                response.setMsgHeader(msg.getMsgHeader());
                response.getMsgHeader().setMsgType(Constants.CALLBACK_RESPONSE_MSG);
                Future future = channel.writeAndFlush(response);
                future.addListener(new FutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (logger.isTraceEnabled()) {
                            logger.trace(" callback response write {}", future.isSuccess());
                        }
                        if (future.isSuccess()) {
                            return;
                        } else if (!future.isSuccess()) {
                            Throwable throwable = future.cause();
                            logger.error("send callback Response msg fail requestId:{} ,cause:{}", response.getMsgHeader().getMsgId(), throwable);
                            logger.error(throwable.getMessage(), throwable);
                            if (!channel.isActive()) {
                                logger.error("Channel {} not active,should close connection..", channel);

                            }
                            throw new RpcException("Fail to send callback Response msg :" + response.getMsgHeader(), throwable);

                        }
                    }
                });

            }
        });


    }

    public synchronized static ClientCallbackHandler getInstance(){
        if(callbackHandler == null){
            callbackHandler = new ClientCallbackHandler();
        }
        return callbackHandler;
    }
}