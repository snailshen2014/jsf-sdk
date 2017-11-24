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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import com.ipd.jsf.gd.codec.TelnetCodec;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.server.TelnetChannelHandler;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.embedded.EmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.plugins.server.netty.RestEasyHttpRequestDecoder;
import org.jboss.resteasy.plugins.server.netty.RestEasyHttpResponseEncoder;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;

/**
 * Title: NettyJaxrsServer<br>
 * <p/>
 * Description: 参考NettyJaxrsServer的实现，增加了自定义功能<br>
 * <p/>
 * @see org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer
 */
public class NettyJaxrsServer implements EmbeddedJaxrsServer {

    protected ServerBootstrap bootstrap = new ServerBootstrap();
    protected int port = 8080;
    protected ResteasyDeployment deployment = new ResteasyDeployment();
    protected String root = "";
    protected SecurityDomain domain;
    private EventLoopGroup eventLoopGroup;
    private EventLoopGroup eventExecutor;
    private int ioWorkerCount = Constants.CPU_CORES * 2;
    private int executorThreadCount = 16;
    private SSLContext sslContext;
    private int maxRequestSize = 1024 * 1024 * 10;
    private int backlog = 128;
    protected boolean debug = false; // 是否打印debug信息
    protected boolean keepalive = false; // 是否长连接
    protected boolean telnet = true;
    protected boolean daemon = true;

    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * Specify the worker count to use. For more information about this please see the javadocs of {@link EventLoopGroup}
     *
     * @param ioWorkerCount
     */
    public void setIoWorkerCount(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    /**
     * Set the number of threads to use for the EventExecutor. For more information please see the javadocs of {@link io.netty.util.concurrent.EventExecutor}.
     * If you want to disable the use of the {@link io.netty.util.concurrent.EventExecutor} specify a value <= 0.  This should only be done if you are 100% sure that you don't have any blocking
     * code in there.
     *
     * @param executorThreadCount
     */
    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    /**
     * Set the max. request size in bytes. If this size is exceed we will send a "413 Request Entity Too Large" to the client.
     *
    * @param maxRequestSize the max request size. This is 10mb by default.
     */
    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    @Override
    public void setDeployment(ResteasyDeployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public void setRootResourcePath(String rootResourcePath) {
        root = rootResourcePath;
        if (root != null && root.equals("/")) root = "";
    }

    @Override
    public ResteasyDeployment getDeployment() {
        return deployment;
    }

    @Override
    public void setSecurityDomain(SecurityDomain sc) {
        this.domain = sc;
    }

    protected RequestDispatcher createRequestDispatcher() {
        return new RequestDispatcher((SynchronousDispatcher) deployment.getDispatcher(),
                deployment.getProviderFactory(), domain);
    }

    @Override
    public void start()
    {
        eventLoopGroup = new NioEventLoopGroup(ioWorkerCount, new NamedThreadFactory("JSF-REST-BOSS", daemon));
        eventExecutor = new NioEventLoopGroup(executorThreadCount, new NamedThreadFactory("JSF-REST-WORKER", daemon));
        deployment.start();
        final RequestDispatcher dispatcher = this.createRequestDispatcher();
        // Configure the server.
        if (sslContext == null) {
            bootstrap.group(eventLoopGroup, eventExecutor)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpAdapterDecoder(telnet) {
                                @Override
                                public void buildHttpPipeline(ChannelHandlerContext ctx) {
                                    ctx.pipeline().addLast(new HttpRequestDecoder());
                                    ctx.pipeline().addLast(new RestHttpObjectAggregator(maxRequestSize));
                                    ctx.pipeline().addLast(new HttpResponseEncoder());
                                    ctx.pipeline().addLast(new RestEasyHttpRequestDecoder(dispatcher.getDispatcher(), root, RestEasyHttpRequestDecoder.Protocol.HTTP));
                                    ctx.pipeline().addLast(new RestEasyHttpResponseEncoder(dispatcher));
                                    ctx.pipeline().addLast(new RestRequestHandler(dispatcher));
                                }

                                @Override
                                public void buildTcpPipeline(ChannelHandlerContext ctx) {
                                    ctx.pipeline().addLast(new TelnetCodec());
                                    ctx.pipeline().addLast(new TelnetChannelHandler());
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, keepalive);
        } else {
            final SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            bootstrap.group(eventLoopGroup, eventExecutor)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpAdapterDecoder(telnet) {
                                @Override
                                public void buildHttpPipeline(ChannelHandlerContext ctx) {
                                    ctx.pipeline().addFirst(new SslHandler(engine));
                                    ctx.pipeline().addLast(new HttpRequestDecoder());
                                    ctx.pipeline().addLast(new RestHttpObjectAggregator(maxRequestSize));
                                    ctx.pipeline().addLast(new HttpResponseEncoder());
                                    ctx.pipeline().addLast(new RestEasyHttpRequestDecoder(dispatcher.getDispatcher(), root, RestEasyHttpRequestDecoder.Protocol.HTTPS));
                                    ctx.pipeline().addLast(new RestEasyHttpResponseEncoder(dispatcher));
                                    ctx.pipeline().addLast(eventExecutor, new RestRequestHandler(dispatcher));
                                }

                                @Override
                                public void buildTcpPipeline(ChannelHandlerContext ctx) {
                                    ctx.pipeline().addLast(new TelnetCodec());
                                    ctx.pipeline().addLast(new TelnetChannelHandler());
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, keepalive);
        }

        bootstrap.bind(port).syncUninterruptibly();
    }

    @Override
    public void stop() {
        eventLoopGroup.shutdownGracefully();
        eventExecutor.shutdownGracefully();
    }

    public void setKeepalive(boolean keepalive) {
        this.keepalive = keepalive;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setTelnet(boolean telnet) {
        this.telnet = telnet;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }
}