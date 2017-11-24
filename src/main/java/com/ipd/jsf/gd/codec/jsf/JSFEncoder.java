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
package com.ipd.jsf.gd.codec.jsf;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.protocol.JSFProtocol;
import com.ipd.jsf.gd.protocol.ProtocolUtil;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Title: JSF协议的NettyEncoder<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFEncoder extends MessageToByteEncoder {

    private final static Logger logger = LoggerFactory.getLogger(JSFEncoder.class);

    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        ByteBuf headBody = null;
        if(out == null) {
            //logger.debug("ByteBuf out is null..");
            out = ctx.alloc().buffer();
        }
        try {
            if(msg instanceof BaseMessage){
                 BaseMessage base = (BaseMessage)msg;
                 if(base.getMsg() != null){
                     write(base.getMsg(),out);
                     base.getMsg().release();
                 }else{
                     headBody = ctx.alloc().heapBuffer();
                     ProtocolUtil.encode(msg, headBody);
                     write(headBody,out);
                 }

            }else{
                throw new JSFCodecException("Not support this type of Object.");
            }

        } finally {
            if(headBody != null)headBody.release();
        }
    }

    /**
     * 复制数据
     *
     * @param data
     *         序列化后的数据
     * @param out
     *         回传的数据
     * @see JSFProtocol
     */
    private void write(ByteBuf data, ByteBuf out) {
        int totalLength = 2 + 4 + data.readableBytes();
        if(out.capacity() < totalLength) out.capacity(totalLength);
        out.writeBytes(Constants.MAGICCODEBYTE); // 写入magiccode
        int length = totalLength - 2 ; //  data.readableBytes() + 4  (4指的是FULLLENGTH)
        out.writeInt(length);   //4 for Length Field
        out.writeBytes(data, data.readerIndex(), data.readableBytes());
        //logger.trace("out length:{}",out.readableBytes());
    }
}