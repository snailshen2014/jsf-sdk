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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipd.jsf.gd.reflect.ProxyFactory;
import com.ipd.jsf.gd.util.Constants;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.server.http.DispatcherServlet;
import com.ipd.jsf.gd.server.http.HttpHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.transport.ServerTransportConfig;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 基于CXF+JETTY的webservice服务端<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class HttpWsServer extends Server {


    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpWsServer.class);

    private final ExtensionManagerBus bus = new ExtensionManagerBus();

    private final HTTPTransportFactory transportFactory = new HTTPTransportFactory(bus);

    private final ServerTransportConfig serverConfig;

    private org.eclipse.jetty.server.Server jettyServer;

    private final String basePath;
    private final boolean isjaxws;

    public HttpWsServer(ServerTransportConfig serverConfig) {
        super(serverConfig);
        this.serverConfig = serverConfig;
        // 为了一个端口发布多个服务，增加path以示区分
        this.basePath = serverConfig.getContextPath();
        this.isjaxws = serverConfig.getProtocolType().equals(Constants.ProtocolType.jaxws);
        this.bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
    }

    @Override
    public boolean isStarted() {
        return jettyServer != null;
    }

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }
        jettyServer = new org.eclipse.jetty.server.Server();
        int port = serverConfig.getPort();
        DispatcherServlet.addHttpHandler(port, new WebServiceHandler());

        int threads = serverConfig.getServerBusinessPoolSize();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("JSF-WS-BZ");
        threadPool.setDaemon(true);
        threadPool.setMaxThreads(threads);
        threadPool.setMinThreads(threads);
        jettyServer.setThreadPool(threadPool);

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost(serverConfig.getHost());
        connector.setPort(serverConfig.getPort());
        jettyServer.addConnector(connector);

        ServletHandler servletHandler = new ServletHandler();
        ServletHolder servletHolder = servletHandler.addServletWithMapping(DispatcherServlet.class, "/*");
        servletHolder.setInitOrder(2);

        jettyServer.setHandler(servletHandler);

        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new InitErrorException("Failed to start jetty server at port " + port
                    + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (isStarted()) {
            try {
                LOGGER.info("Stop the http webservice server at port {}", serverConfig.getPort());
                jettyServer.stop();
                DispatcherServlet.removeHttpHandler(serverConfig.getPort());
                for (Map.Entry<ProviderConfig, ServerFactoryBean> entry : exported.entrySet()) {
                    try {
                        entry.getValue().destroy();
                    } catch (Exception e) {
                        LOGGER.warn("Destroy ServerFactoryBean error, message is:{}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Stop the http webservice server at port " + serverConfig.getPort() + " error !", e);
            }
        }
    }

    private final ConcurrentHashMap<ProviderConfig, ServerFactoryBean> exported = new
            ConcurrentHashMap<ProviderConfig, ServerFactoryBean>();

    /**
     * @param providerConfig
     * @param invoker
     */
    @Override
    public void registerProcessor(ProviderConfig providerConfig, Invoker invoker) {
        if (!isStarted()) {
            start();
        }
        if (exported.containsKey(providerConfig)) {
            LOGGER.warn("Webservice has been exported, provider config : {}", providerConfig);
        }
        // 在httpserver中注册此jaxws服务
        String url = "http://" + serverConfig.getHost() + ":" + serverConfig.getPort()
                + basePath + providerConfig.getInterfaceId();
        LOGGER.info("Registry {} service to base url {}", serverConfig.getProtocolType(), url);
        try {
            // 服务端加上代理，这样方便统计，以及取一些值
            //Object impl = providerConfig.getRef();
            Object proxy = ProxyFactory.buildProxy(providerConfig.getProxy(),
                    providerConfig.getProxyClass(), invoker);

            if (isjaxws) {
                JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();
                serverFactoryBean.setAddress(basePath + providerConfig.getInterfaceId());
                serverFactoryBean.setServiceClass(providerConfig.getProxyClass());
                serverFactoryBean.setServiceBean(proxy);
                if (serverConfig.isPrintMessage()) {
                    serverFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
                    serverFactoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
                }
                serverFactoryBean.setDestinationFactory(transportFactory);
                serverFactoryBean.setBus(bus);
                serverFactoryBean.create();
                exported.put(providerConfig, serverFactoryBean);
            } else {
                ServerFactoryBean serverFactoryBean = new ServerFactoryBean();
                serverFactoryBean.setAddress(basePath + providerConfig.getInterfaceId());
                serverFactoryBean.setServiceClass(providerConfig.getProxyClass());
                serverFactoryBean.setServiceBean(proxy);
                if (serverConfig.isPrintMessage()) {
                    serverFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
                    serverFactoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
                }
                serverFactoryBean.setDestinationFactory(transportFactory);
                serverFactoryBean.setBus(bus);
                serverFactoryBean.create();
                exported.put(providerConfig, serverFactoryBean);
            }
        } catch (Exception e) {
            LOGGER.error("Registry " + serverConfig.getProtocolType() +" service error", e);
        }
    }


    @Override
    public void unregisterProcessor(ProviderConfig providerConfig) {
        if (!isStarted()) {
            throw new InitErrorException("Server not started");
        }
        // 在httpserver中取消注册此jaxws服务
        String url = "http://" + serverConfig.getHost() + ":" + serverConfig.getPort()
                + basePath + providerConfig.getInterfaceId();
        LOGGER.info("Unregistry {} service to base path is ", serverConfig.getProtocolType(), url);
        ServerFactoryBean svrFactory = exported.get(providerConfig);
        if (svrFactory != null) {
            try {
                svrFactory.destroy();
            } catch (Exception e) {
                LOGGER.warn("Destroy ServerFactoryBean error, message is:{}", e);
            } finally {
                exported.remove(providerConfig);
            }
        }
    }


    private class WebServiceHandler implements HttpHandler {

        private volatile ServletController servletController;

        @Override
		public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if (servletController == null) {
                HttpServlet httpServlet = DispatcherServlet.getInstance();
                if (httpServlet == null) {
                    response.sendError(500, "No such DispatcherServlet instance.");
                    return;
                }
                synchronized (this) {
                    if (servletController == null) {
                        servletController = new ServletController(transportFactory.getRegistry(), httpServlet.getServletConfig(), httpServlet);
                    }
                }
            }
            String pathInfo = request.getPathInfo();
            if ("favicon.ico".equals(pathInfo)
                    || "/".equals(pathInfo)) {
                return;
            }

            RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
            servletController.invoke(request, response);
        }
    }
}