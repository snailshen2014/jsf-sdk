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

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.InitErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/*
 *make the abstract transport
 */

public class JSFServerTransport implements ServerTransport {

    private static final Logger logger = LoggerFactory.getLogger(JSFServerTransport.class);

    private ServerTransportConfig config;
    private ServerBootstrap serverBootstrap;



    public JSFServerTransport(ServerTransportConfig config){
         this.config = config;
    }

	/**
	 * 
	 */
	public Boolean start() {
        boolean flag = Boolean.FALSE;
        logger.debug("JSF server transport start! ");
        Class clazz = NioServerSocketChannel.class;

        if(config.isUseEpoll()){
            clazz = EpollServerSocketChannel.class;
        }
        Boolean reusePort = Boolean.FALSE;
        if(!Constants.isWindows) reusePort = Boolean.TRUE;
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(config.getParentEventLoopGroup(), config.getChildEventLoopGroup())
        .channel(clazz)
        .childHandler(new ServerChannelInitializer(config));
        serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getCONNECTTIMEOUT())
        .option(ChannelOption.SO_BACKLOG, config.getBACKLOG())
        .option(ChannelOption.SO_REUSEADDR, reusePort)   //disable this on windows, open it on linux
        .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
        .childOption(ChannelOption.SO_KEEPALIVE, config.isKEEPALIVE())
       // .childOption(ChannelOption.SO_TIMEOUT, config.getTIMEOUT())
        .childOption(ChannelOption.TCP_NODELAY, config.isTCPNODELAY())
        .childOption(ChannelOption.ALLOCATOR, PooledBufHolder.getInstance())
        .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        .childOption(ChannelOption.SO_RCVBUF,8192 * 128)
        .childOption(ChannelOption.SO_SNDBUF,8192 * 128)
        .childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);

        // 绑定到全部网卡 或者 指定网卡
        ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(config.getHost(), config.getPort()));
        ChannelFuture channelFuture = future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("Server have success bind to {}:{}", config.getHost(), config.getPort());

                } else {
                    logger.error("Server fail bind to {}:{}", config.getHost(), config.getPort());
                    config.getParentEventLoopGroup().shutdownGracefully();
                    config.getChildEventLoopGroup().shutdownGracefully();
                    throw new InitErrorException("Server start fail !", future.cause());
                }

            }
        });

        try {
            channelFuture.await(5000,TimeUnit.MILLISECONDS);
            if(channelFuture.isSuccess()){
                flag = Boolean.TRUE;
            }

        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
        return flag;
    }


	public void stop() {
        logger.info("Shutdown the JSF server transport now...");
        config.getParentEventLoopGroup().shutdownGracefully();
        config.getChildEventLoopGroup().shutdownGracefully();

	}

}