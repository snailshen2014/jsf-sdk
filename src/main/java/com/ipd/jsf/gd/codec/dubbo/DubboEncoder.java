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

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.protocol.DubboProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class DubboEncoder extends MessageToByteEncoder {
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DubboEncoder.class);

    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (out == null) {
            LOGGER.info("ByteBuf out is null..");
            out = ctx.alloc().buffer();
        }
        // 0-1位 MAGIC CODE
        out.writeByte(DubboAdapter.MAGIC_HIGH);
        out.writeByte(DubboAdapter.MAGIC_LOW);

        if (msg instanceof ResponseMessage) {
            try {
                ResponseMessage response = (ResponseMessage) msg;
                int codeType = response.getMsgHeader().getCodecType();

                ByteBuf body = response.getMsg(); // 正常请求的在业务线程已经序列化
                if(body == null) { // 未序列化的此处序列化
                    DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(Constants.ProtocolType.dubbo.value(),
                            codeType);
                    body = ctx.alloc().buffer();
                    body = protocol.encode(msg, body);
                }

                byte flag = (byte) codeType;
                if (response.isHeartBeat()) {
                    flag |= DubboAdapter.FLAG_EVENT;
                }

                // 2位 请求+序列化等
                out.writeByte(flag);
                // 3位 responsecode（request 无用）
                out.writeByte(DubboAdapter.OK);
                // 4-11位 请求Id
                int requestId = response.getRequestId();
                out.writeLong(requestId);
                // 12-15 body的长度
                int bodyLength = body.readableBytes();
                out.writeInt(bodyLength);
                out.writeBytes(body);
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Encode response failed: " + t.getMessage(), t);
                }
                throw new JSFCodecException("Encode response failed: ", t);
            }

        } else if (msg instanceof RequestMessage) {

            try {
                RequestMessage request = (RequestMessage) msg;
                byte codeType = (byte) request.getMsgHeader().getCodecType();
                DubboProtocol protocol = (DubboProtocol) ProtocolFactory.getProtocol(Constants.ProtocolType.dubbo.value(),
                        codeType);

                ByteBuf body = ctx.alloc().buffer();
                body = protocol.encode(msg, body);

                byte flag = (byte) (DubboAdapter.FLAG_REQUEST | codeType);
                //if (req.isTwoWay()) header[2] |= FLAG_TWOWAY;
                //if (req.isEvent()) header[2] |= FLAG_EVENT;
                flag |= DubboAdapter.FLAG_TWOWAY;
                if (request.isHeartBeat()) {
                    flag |= DubboAdapter.FLAG_EVENT;
                }

                // 2位 请求+序列化等
                out.writeByte(flag);
                // 3位 responsecode（request 无用）
                out.writeByte(DubboAdapter.OK);
                // 4-11位 请求Id
                int requestId = request.getRequestId();
                out.writeLong(requestId);
                // 12-15 body的长度
                int bodyLength = body.readableBytes();
                out.writeInt(bodyLength);
                out.writeBytes(body);
            } catch(Throwable t) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Encode request failed: " + t.getMessage(), t);
                }
                throw new JSFCodecException("Encode request failed: ", t);
            }
        } else {

        }
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("readerable byte in netty Encoder:{}", out.readableBytes());
        }
    }
}