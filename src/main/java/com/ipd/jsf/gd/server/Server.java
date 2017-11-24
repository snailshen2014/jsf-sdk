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
import com.ipd.jsf.gd.transport.ServerTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.transport.ServerTransport;
import com.ipd.jsf.gd.transport.ServerTransportConfig;

/**
 * Title: 服务端对象<br>
 * <p/>
 * Description: 包含启动、关闭、注册服务、反注册服务<br>
 * <p/>
 */
public class Server {


    private ServerTransportConfig transportConfig;

    private ServerTransport serverTransport;

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerHandler serverHandler ;

    public Server(ServerTransportConfig transportConfig){

        this.transportConfig = transportConfig;
        serverHandler = ServerHandlerFactory.getServerHandler(this.transportConfig);
    }

    public void start() {
        if (isStarted()) {
            return;
        }
        serverTransport = ServerTransportFactory.getServerTransport(transportConfig);
        if (!serverTransport.start()) {
            throw new InitErrorException("Failed to start server at port " + transportConfig.getPort());
        }
    }

    public boolean isStarted() {
        return serverTransport != null;
    }

    /**
     * 绑定服务接口到Server
     *
     * @param providerConfig
     *         服务接口配置
     */
	public void registerProcessor(ProviderConfig providerConfig, Invoker instance) {
        serverHandler.registerProcessor(BaseServerHandler.genInstanceName(providerConfig.getInterfaceId(), providerConfig.getAlias()),instance);
        ServerAuthHelper.addInterface(providerConfig.getInterfaceId(), providerConfig.getAlias());
	}

    /**
     * 取消绑定服务接口到Server
     *
     * @param providerConfig
     *         服务接口配置
     */
    public void unregisterProcessor(ProviderConfig providerConfig) {
        String key = BaseServerHandler.genInstanceName(providerConfig.getInterfaceId(), providerConfig.getAlias());
        serverHandler.unregisterProcessor(key);
        ServerAuthHelper.delInterface(providerConfig.getInterfaceId(), providerConfig.getAlias());
    }

    public void stop() {
        logger.info("Close the server: port {}", transportConfig.getPort());
        try {
            serverHandler.shutdown();
            if (serverTransport != null) {
                serverTransport.stop();
            }
        } catch (Exception e) {
            logger.error("Error when shutdown the server, cause by: " + e.getMessage(), e);
        }
    }

    public ServerTransportConfig getTransportConfig() {
        return this.transportConfig;
    }

}