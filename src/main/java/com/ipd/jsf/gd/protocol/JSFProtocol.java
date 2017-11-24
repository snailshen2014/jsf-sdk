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

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.CodecFactory;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.compress.CompressUtil;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CodecUtils;
import io.netty.buffer.ByteBuf;

/**
 * Title: 自定义JSFProtocol<br>
 * <p/>
 * Description: 自主研发的JSF协议 结构如下:
 *
 * <pre>
 * JSF Protocol
 * MEGICCODE(2B)      Protocol Identify ADAF
 * =================Protocol Header Begin========================
 * FULLLENGTH(4B):    length of (header+body), not include MEGICCODE
 * HEADERLENGTH(2B):    length of (PROTOCOLTYPE+...+header tail), not include FULLLENGTH and HEADERLENGTH
 * PROTOCOLTYPE(1B)
 * CODECTYPE(1B):     serialize/deserialize type
 * MESSAGETYPE(1B)    request/response/heartbeat/callback?
 * COMPRESSTYPE(1B)   compress type NONE/snappy...
 * MSGID(4B):         message id
 * [OPT]ATTRMAP:
 *    MAP_SIZE(1B)  size of attr map
 *    {
 *      ATTR_KEY(1B)  key of attr
 *      ATTR_TYPE(1B) 1:int; 2:string; 3:byte; 4:short
 *      ATTR_VAL(?B)  int:(4B); string:length(2B)+data; byte:(1B); short:(2B)
 *    }
 * ===============Protocol Header End=============================
 * ===============Protocol Body Begin===========================
 * String className
 * String alias
 * String methodName
 * String[] argsType
 * Object[] args
 * Map<String,String> attachments
 * ===============Protocol Body End=============================
 *
 * </pre>
 * @see CodecUtils
 */
public class JSFProtocol implements Protocol {


    private static final Logger logger = LoggerFactory.getLogger(JSFProtocol.class);

    private final Codec codec;

    public JSFProtocol(Constants.CodecType codecType) {
        this.codec = CodecFactory.getInstance(codecType);
    }

    @Override
    public Object decode(ByteBuf datas, Class t) {
        BaseMessage msg = null;
        byte[] dataArr = new byte[datas.readableBytes()];
        datas.readBytes(dataArr);
        Object result = codec.decode(dataArr, t);
        return result;
    }

    @Override
    public Object decode(ByteBuf datas, String classTypeName) {
        byte[] dataArr = new byte[datas.readableBytes()];
        datas.readBytes(dataArr);
        Object result = codec.decode(dataArr, classTypeName);
        return result;
    }


    @Override
    public ByteBuf encode(Object obj, ByteBuf buffer) {
        if(logger.isTraceEnabled()) {
            logger.trace("readable byte here:{}", buffer.readableBytes());
        }
        if (obj instanceof RequestMessage) {
            RequestMessage request = (RequestMessage) obj;
            MessageHeader msgHeader = request.getMsgHeader();
            msgHeader.setCodecType(msgHeader.getCodecType());
            msgHeader.setProtocolType(msgHeader.getProtocolType());
            msgHeader.setMsgType(request.getMsgHeader().getMsgType());

            Invocation invocation = request.getInvocationBody();
            if (invocation != null) {
                byte[] invocationData = codec.encode(invocation);
                // 添加压缩判断
                byte compressType = msgHeader.getCompressType();
                if (compressType > 0 && CompressUtil.compressOpen) {
                    if (invocationData.length < CompressUtil.compressThresholdSize) { //值的大小 小于数据太小不压缩
                        msgHeader.setCompressType((byte) 0);
                    } else {
                        /*logger.debug("request msgId [{}] is deCompressed with processType {} for msgBody length {} ",
                                new Object[]{request.getMsgHeader().getMsgId(), Constants.CompressType.valueOf(
                                        (byte) request.getMsgHeader().getCompressType()), invocationData.length}
                        );*/
                        invocationData = CompressUtil.compress(invocationData, compressType);
                    }
                }
                request.getMsgHeader().setHeaderLength(CodecUtils.encodeHeader(msgHeader, buffer)); // header
                buffer = buffer.writeBytes(invocationData); // body
                request.getMsgHeader().setLength(buffer.readableBytes());
            } else {
                CodecUtils.encodeHeader(msgHeader, buffer);// header only
            }
        } else if (obj instanceof ResponseMessage) {
            ResponseMessage response = (ResponseMessage) obj;
            MessageHeader msgHeader = response.getMsgHeader();
            byte[] responseData = codec.encode(obj);

            // 添加压缩判断
            byte compressType = msgHeader.getCompressType();
            if (compressType > 0 && CompressUtil.compressOpen) {
                if (responseData.length < CompressUtil.compressThresholdSize) { //值的大小 小于数据太小不压缩
                    msgHeader.setCompressType((byte) 0); //CompressType.NONE.value();
                } else {
                /*logger.debug("Response msgId:[{}] is compressed  with processType {} for msgBody length {}.",
                        new Object[]{bm.getMsgHeader().getMsgId(), Constants.CompressType.valueOf(
                                (byte) bm.getMsgHeader().getCompressType()), responseData.length}
                );*/
                    responseData = CompressUtil.compress(responseData, compressType);
                }
            }
            response.getMsgHeader().setHeaderLength(CodecUtils.encodeHeader(msgHeader, buffer)); // header
            buffer = buffer.writeBytes(responseData); // body
            response.getMsgHeader().setLength(buffer.readableBytes());

        } else {
            throw new JSFCodecException("no such kind of  message..");

        }
        return buffer;
    }


}