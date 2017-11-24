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
package com.ipd.jsf.gd.server;

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.reflect.ProxyFactory;
import com.ipd.jsf.gd.server.http.NettyJaxrsServer;
import com.ipd.jsf.gd.transport.ServerTransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: rest协议的http服务端<br>
 * <p/>
 * Description: 基于resteasy和netty<br>
 * <p/>
 */
public class HttpRestServer extends Server {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpRestServer.class);

    private NettyJaxrsServer httpServer;

    private final ServerTransportConfig serverConfig;

    private final String basePath;

    public HttpRestServer(ServerTransportConfig serverConfig) {
        super(serverConfig);
        this.serverConfig = serverConfig;
        // 为了一个端口发布多个服务，增加path以示区分
        basePath = serverConfig.getContextPath();
    }

    @Override
	public boolean isStarted() {
        return httpServer != null;
    }

    @Override
	public void start() {
        if (isStarted()) {
            return;
        }
        int port = serverConfig.getPort();
        try {
            httpServer = new NettyJaxrsServer();
            int bossThreads = serverConfig.getParentNioEventThreads();
            if (bossThreads == 0) {
                bossThreads = Math.max(4, bossThreads / 2);
            }
            httpServer.setIoWorkerCount(bossThreads); // 其实是boss线程
            httpServer.setExecutorThreadCount(serverConfig.getServerBusinessPoolSize()); // worker线程
            httpServer.setMaxRequestSize(serverConfig.getPayload());
            httpServer.setBacklog(serverConfig.getBACKLOG());
            httpServer.setPort(port);
            httpServer.setDebug(serverConfig.isPrintMessage());
            httpServer.setTelnet(serverConfig.isTelnet());
            httpServer.setKeepalive(false); // 不keepalive
            httpServer.setTelnet(serverConfig.isTelnet());
            httpServer.setDaemon(serverConfig.isDaemon());
            httpServer.start();
        } catch(Exception e) {
            throw new InitErrorException("Failed to start jetty server at port " + port
                    + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
	public void stop() {
        if (isStarted()) {
            try {
                LOGGER.info("Stop the http rest server at port {}", serverConfig.getPort());
                httpServer.stop();
            } catch (Exception e) {
                LOGGER.error("Stop the http rest server at port " + serverConfig.getPort() + " error !", e);
            }
        }
    }

    /**
     *
     * @param providerConfig
     * @param instance
     */
    @Override
	public void registerProcessor(ProviderConfig providerConfig, Invoker instance) {
        if (!isStarted()) {
            start();
        }
        // 在httpserver中注册此jaxrs服务
        LOGGER.info("Register jaxrs service to base url http://" + serverConfig.getHost() + ":"
                + serverConfig.getPort() + serverConfig.getContextPath());
		try {
            // 服务端加上代理，这样方便统计，以及取一些值
			Object proxy = ProxyFactory.buildProxy(providerConfig.getProxy(),
					providerConfig.getProxyClass(), instance);
			httpServer.getDeployment().getRegistry()
					.addSingletonResource(proxy, basePath);
		} catch (Exception e) {
			LOGGER.error("Register jaxrs service error", e);
		}
    }


    @Override
	public void unregisterProcessor(ProviderConfig providerConfig) {
        if (!isStarted()) {
            LOGGER.warn("Server is not started");
            return;
        }
        LOGGER.info("Unregister jaxrs service to port {} and base path is {}", serverConfig.getPort(),
                serverConfig.getContextPath());
        try {
            httpServer.getDeployment().getRegistry()
                    .removeRegistrations(providerConfig.getRef().getClass(), basePath);
        } catch(Exception e) {
            LOGGER.error("Unregister jaxrs service error", e);
        }

    }
}