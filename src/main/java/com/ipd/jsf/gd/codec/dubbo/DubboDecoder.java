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
package com.ipd.jsf.gd.codec.dubbo;

import com.ipd.jsf.gd.codec.LengthFieldBasedFrameDecoder;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.protocol.DubboProtocol;
import com.ipd.service.rpc.service.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * Title: dubbo解析<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class DubboDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DubboDecoder.class);

    public DubboDecoder(int maxFrameLength) {
        /*
        int maxFrameLength,     最大值
        int lengthFieldOffset,  第12-15位是长度，所以偏移：12
        int lengthFieldLength,  总长度占4B，所以长度是：4
        int lengthAdjustment,   总长度就是body长度，所以调整值是：0
        int initialBytesToStrip 代码里跳过，这里不主动跳，所以跳过值是：0
        */
        super(maxFrameLength, 12, 4, 0, 0);
    }

    // header length.


    @Override
    public Object decodeFrame(ByteBuf frame) {
        // 0-1位 MAGIC CODE
        byte[] magiccode = new byte[2];
        frame.readBytes(magiccode);
            // 2位 请求+序列化等
        byte flag = frame.readByte();
        // 3位 responsecode（request 无用）
        byte status = frame.readByte();
        // 4-11位 请求Id
        long requestId = frame.readLong();
        // 12-15 body的长度
        int bodyLength = frame.readInt();

        // @see CodecType hessian2:2  java:3
        byte codeType = (byte) (flag & DubboAdapter.SERIALIZATION_MASK);

        if ((flag & DubboAdapter.FLAG_REQUEST) == 0) { // response

            ByteBuf body = null;
            try {
                int length = frame.readableBytes();
                body = frame.slice(frame.readerIndex(), length);
                ResponseMessage response = null;
                if ((flag & DubboAdapter.FLAG_EVENT) != 0) {
                    response = new ResponseMessage();
                    response.getMsgHeader().setValues(Constants.ProtocolType.dubbo.value(), codeType, Constants.HEARTBEAT_RESPONSE_MSG, Constants.CompressType.NONE.value(), (int) requestId);
                } else {
                    if (status == DubboAdapter.OK) {

                        DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(Constants.ProtocolType.dubbo.value(),
                                codeType);
                        // response
                        response = (ResponseMessage) protocol.decode(body, ResponseMessage.class);
                        response.getMsgHeader().setLength(length + 16);
                        response.getMsgHeader().setValues(Constants.ProtocolType.dubbo.value(), codeType, Constants.RESPONSE_MSG, Constants.CompressType.NONE.value(), (int) requestId);

                        Throwable throwable = response.getException();
                        if (throwable != null) {
                            /*
                            当JSF调用SAF的时候
                            saf-1.0.9及以上会返回 com.ipd.service.rpc.RpcException和GenericException
                            saf-1.0.8.1及以下会返回 com.alibaba.dubbo.rpc.RpcException和GenericException
                                如果是hessian序列化，会被自动映射转换为com.ipd.service.RpcException和GenericException
                                    @see HessianObjectMapping
                                    @see Hessian2Input#readObjectDefinition
                                如果是java序列化，无能为力，抛序列化异常了
                            */
                            if (throwable instanceof com.ipd.service.rpc.RpcException) {
                                com.ipd.service.rpc.RpcException dubboexception = (com.ipd.service.rpc.RpcException) throwable;
                                // 转为jsf的错误
                                throwable = new RpcException(dubboexception.getMessage(), dubboexception.getCause());
                            } else if (throwable instanceof GenericException) {
                                GenericException dubboexception = (GenericException) throwable;
                                throwable = new RpcException(dubboexception.getMessage());
                            }
                            response.setException(throwable);
                        }

                    } else {
                        response = new ResponseMessage();
                        response.getMsgHeader().setLength(length + 16);
                        response.getMsgHeader().setValues(Constants.ProtocolType.dubbo.value(), codeType, Constants.RESPONSE_MSG, Constants.CompressType.NONE.value(), (int) requestId);
                        DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(Constants.ProtocolType.dubbo.value(),
                                codeType);
                        String throwable = (String) protocol.decode(body, String.class);
//                        if (throwable instanceof RpcException) {
//                            RpcException dubboexception = (RpcException) throwable;
//                            // 转为jsf的错误
//                            throwable = new RpcException(dubboexception.getMessage(), dubboexception.getCause());
//                        } else if (throwable instanceof GenericException) {
//                            GenericException dubboexception = (GenericException) throwable;
//                            throwable = new RpcException(dubboexception.getMessage());
//                        }
                        response.setException(new RpcException(throwable));
                    }
                }
                return response;
            } catch(Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Decode response failed: " + t.getMessage(), t);
                }
                throw new JSFCodecException("Decode response failed: ", t);
            } finally {
                if (body != null) {
                    body.release();
                }
            }
        } else { // request

            ByteBuf body = null;

            try {
                int length = frame.readableBytes();
                body = frame.slice(frame.readerIndex(), length);
                int msgType;
                if ((flag & DubboAdapter.FLAG_EVENT) != 0) {
                    msgType = Constants.HEARTBEAT_REQUEST_MSG;
                    RequestMessage req = new RequestMessage();
                    req.getMsgHeader().setValues(Constants.ProtocolType.dubbo.value(), codeType, msgType, Constants.CompressType.NONE.value(), (int) requestId);
                    return req;
                } else {
                    msgType = Constants.REQUEST_MSG;
                    DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(Constants.ProtocolType.dubbo.value(),
                            codeType);
                    // request
                    RequestMessage req = (RequestMessage) protocol.decode(body, RequestMessage.class);
//                req.setTwoWay((flag & FLAG_TWOWAY) != 0);

                    MessageHeader header = new MessageHeader();
                    header.setValues(Constants.ProtocolType.dubbo.value(), codeType, msgType, Constants.CompressType.NONE.value(), (int) requestId);
                    //req.setMsgBody(frame.copy(frame.readerIndex(), frame.readableBytes()));
                    req.getMsgHeader().copyHeader(header);
                    req.getMsgHeader().setLength(length + 16);
                    return req;
                }


            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Decode request failed: " + t.getMessage(), t);
                }
                throw new JSFCodecException("Decode request failed: ", t);
            } finally {
                if (body != null) {
                    body.release();
                }
            }
        }
    }
}