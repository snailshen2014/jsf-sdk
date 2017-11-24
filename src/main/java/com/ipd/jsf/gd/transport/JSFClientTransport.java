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
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.protocol.Protocol;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.RpcContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Title: 客户端传输层<br>
 * <p/>
 * Description: 包装了netty的channel，集成了批量提交等功能<br>
 * <p/>
 */
public class JSFClientTransport extends AbstractTCPClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(JSFClientTransport.class);

    private Channel channel;

    private boolean shakeHanded = Boolean.FALSE;

    public JSFClientTransport(final Channel channel){
        super(null);
        setChannel(channel);
    }

    /*
     * send shake hand request..
     */
    private void shakeHand(){
        RequestMessage shakehand = new RequestMessage();
        shakehand.getMsgHeader().setMsgType(Constants.SHAKEHAND_MSG);
        channel.writeAndFlush(shakehand);
    }

    public void setChannel(Channel channel){
        this.channel = channel;
        super.remoteAddress = channel.remoteAddress();
        super.localAddress = channel.localAddress();
    }

    public Channel getChannel(){
        return this.channel;
    }

    /*
     * actually do the reconnect logic here
     */
    public void reconnect() {
        if(!isOpen() && clientTransportConfig != null){
            try {
                ClientTransportFactory.reconn(this);
            } catch (InitErrorException e) {
                logger.debug(e.getMessage(), e);
                throw e;
            }

        }else{
           logger.debug("reconnect bypass for channel status:{} and ClientTransportConfig:{}.",channel,clientTransportConfig);
        }

    }


    @Override
    public void shutdown() {
        if(channel != null && channel.isOpen()){
            try {
                channel.close();
            } catch(Exception e) {
               logger.error(e.getMessage(),e);
            }
        }
    }

    @Override
    public MsgFuture doSendAsyn(final BaseMessage msg,int timeout) {
        if(msg == null){
            throw new RpcException("msg cannot be null.");
        }
        final MsgFuture resultFuture = new MsgFuture(getChannel(), msg.getMsgHeader(), timeout);
        Short providerJsfVersion = (Short) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
        //msg.getMsgHeader().addHeadKey(Constants.HeadKey.timeout,timeout); 外面已设置
        this.addFuture(msg,resultFuture);
        if (msg instanceof RequestMessage) {
            RequestMessage request = (RequestMessage) msg;
            ByteBuf byteBuf = PooledBufHolder.getBuffer();
            Protocol protocol = ProtocolFactory.getProtocol(msg.getProtocolType(), msg.getMsgHeader().getCodecType());
            request = callBackHandler(request);
            try {
                if (providerJsfVersion != null) { // 供序列化时特殊判断
                    resultFuture.setProviderJsfVersion(providerJsfVersion); // 记录下请求值
                }
                byteBuf = protocol.encode(request, byteBuf);
            } finally {
                if (providerJsfVersion != null) {
                    RpcContext.getContext().removeAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
                }
            }
            request.setMsg(byteBuf);
            Invocation invocation = request.getInvocationBody();
            if (invocation != null) {
                // 客户端批量发送默认开启
                RingBufferHolder clientRingBufferHolder = RingBufferHolder.getClientRingbuffer(invocation.getClazzName());
                if (clientRingBufferHolder != null) { // 批量发送
                    request.setChannel(this.channel);
                    clientRingBufferHolder.submit(request);
                } else {
                    channel.writeAndFlush(request, channel.voidPromise());
                }
            } else {  // heartbeat等
                channel.writeAndFlush(request, channel.voidPromise());
            }
        } else {
            channel.writeAndFlush(msg, channel.voidPromise());
        }
        resultFuture.setSentTime(JSFContext.systemClock.now());// 置为已发送
        return resultFuture;
    }

    /*
     * callback codec in server request sender
     */
    private RequestMessage callBackHandler(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        if (invocation == null) return request;
        Class[] classes = invocation.getArgClasses();
        Object[] objs = invocation.getArgs();
        //find and replace the callback
        int port =((InetSocketAddress)channel.localAddress()).getPort();
        int i = 0;
        for (Class clazz : classes) {
            if (CallbackUtil.isCallbackInterface(clazz)) {
                Object callbackIns = objs[i];
                String interfaceId = request.getClassName();
                String methodName = request.getMethodName();
                // 如果是callback本地实例 则置为null再发给服务端
                objs[i] = null;
                // 在Header加上callbackInsId关键字，服务端特殊处理
                String callbackinsId = CallbackUtil.clientRegisterCallback(interfaceId, methodName, callbackIns,port);
                request.getMsgHeader().addHeadKey(Constants.HeadKey.callbackInsId, callbackinsId);
                break;
            }
            i++;
        }
        invocation.setArgs(objs);
        request.setInvocationBody(invocation);
        return request;

    }

    public void setShakeHandStatus(boolean status){
        this.shakeHanded = status;
    }

    @Override
    public boolean isOpen() {
        return channel!=null && channel.isActive() && channel.isOpen();
    }

}