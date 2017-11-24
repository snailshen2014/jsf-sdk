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

import com.ipd.jsf.gd.error.CallbackStubException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.server.Invoker;
import com.ipd.jsf.gd.util.NetUtils;

/**
 *
 * server-side callback Invoker for call client's service from server
 */
public class ChannelWapperedInvoker implements Invoker {

    private ClientTransport clientTransport;

    private final String callbackInsId;

    private final int codecType;

    /**
     * notify实际的参数类型
     */
    private String[] argTypes;



    public ChannelWapperedInvoker(ClientTransport transport,String insId,int codecType){
       this.clientTransport = transport;
       this.callbackInsId = insId;
        this.codecType = codecType;
   }



    @Override
    public ResponseMessage invoke(BaseMessage msg) {
        if (clientTransport == null || !clientTransport.isOpen()) {
            CallbackStubException callbackException = new CallbackStubException(
                    "[JSF-23011]Callback fail cause by channel closed, you can remove the callback stub now, channel is"
                            + (clientTransport == null ? " null" : ": " +
                            NetUtils.connectToString(clientTransport.getLocalAddress(), clientTransport.getRemoteAddress())));
            CallbackUtil.removeFromProxyMap(this.callbackInsId);
            throw callbackException;
        }

        if (msg instanceof RequestMessage && argTypes != null) {
            // 将T 设置为实际类型
            RequestMessage request = (RequestMessage) msg;
            request.getInvocationBody().setArgsType(argTypes);
        }

        msg.getMsgHeader().setCompressType(Constants.CompressType.snappy.value()); // 默认开启压缩
        msg.getMsgHeader().setProtocolType(Constants.ProtocolType.jsf.value());
        msg.getMsgHeader().setCodecType(codecType);
        msg.getMsgHeader().setMsgType(Constants.CALLBACK_REQUEST_MSG);
        msg.getMsgHeader().addHeadKey(Constants.HeadKey.callbackInsId,this.callbackInsId);
        return clientTransport.send(msg,3000);
    }

    public String getCallbackInsId() {
        return callbackInsId;
    }

    public void setArgTypes(String[] argTypes) {
        this.argTypes = argTypes;
    }
}