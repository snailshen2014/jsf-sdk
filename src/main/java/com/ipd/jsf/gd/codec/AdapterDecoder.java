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
package com.ipd.jsf.gd.codec;

import com.ipd.jsf.gd.codec.dubbo.DubboAdapter;
import com.ipd.jsf.gd.codec.dubbo.DubboDecoder;
import com.ipd.jsf.gd.codec.dubbo.DubboEncoder;
import com.ipd.jsf.gd.codec.http.HttpJsonHandler;
import com.ipd.jsf.gd.codec.jsf.JSFDecoder;
import com.ipd.jsf.gd.codec.jsf.JSFEncoder;
import com.ipd.jsf.gd.server.TelnetChannelHandler;
import com.ipd.jsf.gd.transport.ServerChannelHandler;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Title: Decoder分发器<br>
 * <p/>
 * Description: 由于同一端口监听不通的协议，所以内部的decoder不能直接初始化<br>
 * 根据第一次请求的前两个字节，识别请求类型<br>
 * <p/>
 */
public class AdapterDecoder extends ByteToMessageDecoder {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AdapterDecoder.class);

    public AdapterDecoder(ServerChannelHandler serverChannelHandler, int payload, boolean telnet) {
        this.serverChannelHandler = serverChannelHandler;
        this.payload = payload;
        this.telnet = telnet;
    }

    private final ServerChannelHandler serverChannelHandler;

    /**
     * 是否允许telnet
     */
    private boolean telnet;

    /**
     * 最大数据包大小 maxFrameLength
     */
    private int payload = 8 * 1024 * 1024;

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

        // jsf协议
        if (isJSF(b1, b2)) {
            LOGGER.info("Accept jsf connection {}", NetUtils.connectToString(remoteAddress, localAddress));
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new JSFDecoder(payload));
            pipeline.addLast(new JSFEncoder());
            pipeline.addLast(serverChannelHandler);
            pipeline.remove(this);

            pipeline.fireChannelActive(); // 重新触发连接建立事件
        }
        // 1.x dubbo协议
        else if (DubboAdapter.match(b1, b2)) {
            LOGGER.info("Accept dubbo connection {}", NetUtils.connectToString(remoteAddress, localAddress));
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new DubboDecoder(payload));
            pipeline.addLast(new DubboEncoder());
            pipeline.addLast(serverChannelHandler);
            pipeline.remove(this);

            pipeline.fireChannelActive(); // 重新触发连接建立事件
        }

        // http协议
        else if (isHttp(b1, b2)) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Accept http connection {}", NetUtils.connectToString(remoteAddress, localAddress));
            }
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("http-aggregator", new HttpObjectAggregator(payload));
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("jsonDecoder", new HttpJsonHandler(serverChannelHandler.getServerHandler()));
            pipeline.remove(this);
        }

        // telnet
        else {
            LOGGER.info("Accept telnet connection {}", NetUtils.connectToString(remoteAddress, localAddress));

            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new TelnetCodec());
            pipeline.addLast(new TelnetChannelHandler());
            pipeline.remove(this);

            if (telnet) {
                pipeline.fireChannelActive(); // 重新触发连接建立事件
            } else {
                ctx.channel().writeAndFlush("Sorry! Not support telnet");
                ctx.channel().close();
            }
        }
    }

    private boolean isJSF(short magic1, short magic2) {
        return magic1 == Constants.MAGICCODEBYTE[0]
                && magic2 == Constants.MAGICCODEBYTE[1];
    }

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