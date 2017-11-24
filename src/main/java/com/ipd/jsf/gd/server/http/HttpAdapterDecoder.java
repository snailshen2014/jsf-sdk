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

import java.net.InetSocketAddress;
import java.util.List;

import com.ipd.jsf.gd.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Title: http接口支持telnet命令 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public abstract class HttpAdapterDecoder extends ByteToMessageDecoder {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpAdapterDecoder.class);
    private final boolean telnet;

    protected HttpAdapterDecoder(boolean telnet) {
        this.telnet = telnet;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 2) {
            return;
        }
        Short magiccode_high = in.getUnsignedByte(0);
        Short magiccode_low = in.getUnsignedByte(1);
        byte b1 = magiccode_high.byteValue();
        byte b2 = magiccode_low.byteValue();

        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

        if (isHttp(b1, b2)) {
            ChannelPipeline pipeline = ctx.pipeline();
            buildHttpPipeline(ctx);
            pipeline.remove(this);

            ctx.fireChannelActive();
        }

        // telnet
        else {
            LOGGER.info("Accept telnet connection {}", NetUtils.connectToString(remoteAddress, localAddress));

            ChannelPipeline pipeline = ctx.pipeline();
            buildTcpPipeline(ctx);
            pipeline.remove(this);

            if (telnet) {
                ctx.fireChannelActive();
            } else {
                ctx.channel().writeAndFlush("Sorry! Not support telnet");
                ctx.channel().close();
            }
        }
    }

    public abstract void buildHttpPipeline(ChannelHandlerContext ctx);

    public abstract void buildTcpPipeline(ChannelHandlerContext ctx);

    private boolean isHttp(int magic1, int magic2) {
        return (magic1 == 'G' && magic2 == 'E') || // GET
                (magic1 == 'P' && magic2 == 'O') || // POST
                (magic1 == 'P' && magic2 == 'U') || // PUT
                (magic1 == 'H' && magic2 == 'E') || // HEAD
                (magic1 == 'O' && magic2 == 'P') || // OPTIONS
                (magic1 == 'P' && magic2 == 'A') || // PATCH
                (magic1 == 'D' && magic2 == 'E') || // DELETE
                (magic1 == 'T' && magic2 == 'R') || // TRACE
                (magic1 == 'C' && magic2 == 'O');   // CONNECT
    }
}