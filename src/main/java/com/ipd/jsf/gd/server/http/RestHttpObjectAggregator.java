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
package com.ipd.jsf.gd.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.RecyclableArrayList;

/**
 * Title: 增加一个释放FullHttpRequest的切入口 <br>
 *
 * Description: <br>
 * @see io.netty.handler.codec.MessageToMessageDecoder#channelRead(io.netty.channel.ChannelHandlerContext, Object)
 */
public class RestHttpObjectAggregator extends HttpObjectAggregator {

    public RestHttpObjectAggregator(int maxContentLength) {
        super(maxContentLength);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RecyclableArrayList out = RecyclableArrayList.newInstance();
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                HttpObject cast = (HttpObject) msg;
                try {
                    decode(ctx, cast, out);
                } finally {
                    ReferenceCountUtil.release(cast);
                }
            } else {
                out.add(msg);
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            int size = out.size();
            for (int i = 0; i < size; i++) {
                ctx.fireChannelRead(out.get(i));
            }
            // 增加释放逻辑
            for (Object obj : out) {
                if (obj.getClass().getName().equals("io.netty.handler.codec.http" +
                        ".HttpObjectAggregator$AggregatedFullHttpRequest")) {
                    FullHttpMessage msg1 = (FullHttpMessage) obj;
                    if (msg1.refCnt() > 0) {
                        msg1.release();
                    }
                }
            }
            out.recycle();
        }
    }
}