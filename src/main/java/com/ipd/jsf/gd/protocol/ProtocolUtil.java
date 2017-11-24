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
package com.ipd.jsf.gd.protocol;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.compress.CompressUtil;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.transport.PooledBufHolder;
import com.ipd.jsf.gd.util.CodecUtils;
import io.netty.buffer.ByteBuf;

/**
 * Date: 14-3-18
 * Time: 下午7:33
 */
public class ProtocolUtil {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);



    public static ByteBuf encode(Object object,ByteBuf byteBuf ){
        int protocolType;
        int codeType;
        MessageHeader header = null;
        try {
            if(object instanceof BaseMessage){
                BaseMessage msg = (BaseMessage)object;
                protocolType = msg.getProtocolType();
                codeType = msg.getMsgHeader().getCodecType();
            }else{
                throw new JSFCodecException("encode object shout be a instance of BaseMessage.");
            }
            Protocol protocol = ProtocolFactory.getProtocol(protocolType, codeType);
            byteBuf =protocol.encode(object,byteBuf);
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            RpcException rException = ExceptionUtils.handlerException(header,e);
            throw rException;
        }
        return byteBuf;
    }



    public static BaseMessage decode(ByteBuf byteBuf){
        MessageHeader header = null;
        Integer msgLength = byteBuf.readableBytes() + 6;//magiccode + msg length(4 byte)
        BaseMessage msg = null;
        ByteBuf deCompress = null;
        try {
            Short headerLength = byteBuf.readShort();
//            int readerIndex = byteBuf.readerIndex();
//            if(readerIndex > byteBuf.readableBytes()){
//                throw new JSFCodecException("codecError:header length error.");
//            }
            //ByteBuf headerBuf = byteBuf.slice(readerIndex,headerLength);
            //byteBuf.skipBytes(headerLength);
            header = CodecUtils.decodeHeader(byteBuf, headerLength);
            header.setHeaderLength(headerLength);
            //是否需要解压
            int compType = header.getCompressType();
            if (compType > 0) {
                if(logger.isTraceEnabled()) {
                    logger.trace("msgId [{}] is deCompressed with processType {}",
                            header.getMsgId(), Constants.CompressType.valueOf((byte) compType));
                }

                byte[] desc = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(desc);
                byte[] deCom = CompressUtil.deCompress(desc, (byte)compType);
                deCompress = PooledBufHolder.getBuffer(deCom.length);
                deCompress.writeBytes(deCom);
                byteBuf.release();

                header.setLength(deCompress.readableBytes() + 6 + headerLength);
                msg = enclosure(deCompress,header);
            } else {
                header.setLength(msgLength);
                msg = enclosure(byteBuf,header);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            RpcException rpcException = ExceptionUtils.handlerException(header, e);
            byteBuf.release();//release the byteBuf when decode hit on error
            if (deCompress != null) deCompress.release();
            throw rpcException;
        }
        return msg;
    }

    public static BaseMessage enclosure(ByteBuf byteBuf, MessageHeader header){
        int msgType = header.getMsgType();
        BaseMessage msg = null;
        try {
            switch(msgType){
//                case Constants.CALLBACK_REQUEST_MSG:
//                    Protocol protocol = ProtocolFactory.getProtocol(header.getProtocolType(), header.getCodecType());
//                    RequestMessage tmpCallbackMsg = new RequestMessage();
//                    Invocation tmp1 = (Invocation)protocol.decode(byteBuf,Invocation.class.getCanonicalName());
//                    tmpCallbackMsg.setInvocationBody(tmp1);
//                    byteBuf.release();
//                    msg = tmpCallbackMsg;
//                    break;
                case Constants.CALLBACK_REQUEST_MSG:
                case Constants.REQUEST_MSG:
                    RequestMessage tmp = new RequestMessage();
                    tmp.setReceiveTime(JSFContext.systemClock.now());
                    tmp.setMsgBody(byteBuf.slice(byteBuf.readerIndex(),byteBuf.readableBytes()));
                    msg = tmp;
                    break;
                case Constants.CALLBACK_RESPONSE_MSG:
                    Protocol protocol1 = ProtocolFactory.getProtocol(header.getProtocolType(), header.getCodecType());
                    ResponseMessage response1 =(ResponseMessage)protocol1.decode(byteBuf,ResponseMessage.class.getCanonicalName());
                    msg = response1;
                    byteBuf.release();
                    break;
                case Constants.RESPONSE_MSG:
                    ResponseMessage response = new ResponseMessage();
                    //byteBuf.retain();
                    response.setMsgBody(byteBuf.slice(byteBuf.readerIndex(),byteBuf.readableBytes()));
                    //Protocol protocol = ProtocolFactory.getProtocol(header.getProtocolType(), header.getCodecType());
                    //ResponseMessage response =(ResponseMessage)protocol.decode(byteBuf,ResponseMessage.class.getCanonicalName()); deserialized in the userThread
                    msg = response;
                    break;
                case Constants.HEARTBEAT_REQUEST_MSG:
                    msg = new RequestMessage();
                    byteBuf.release();
                    break;
                case Constants.HEARTBEAT_RESPONSE_MSG:
                    msg = new ResponseMessage();
                    byteBuf.release();
                    break;
                case Constants.SHAKEHAND_MSG:
                    RequestMessage shakeHand = new RequestMessage();
                    msg = shakeHand;
                    byteBuf.release();
                    break;
                default:
                    throw new RpcException(header,"Unknown message type in header!");
            }
            if (msg != null) {
                msg.setMsgHeader(header);
            }
        } catch (Exception e) {
            RpcException rException = ExceptionUtils.handlerException(header,e);
            throw rException;
        }
        return msg;

    }
}