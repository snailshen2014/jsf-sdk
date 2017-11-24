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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.config.ServerConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.transport.ServerTransportConfig;
import com.ipd.jsf.gd.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ServerFactory {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    /**
     * 全部服务端
     */
    private final static Map<String, Server> SERVERMAP = new ConcurrentHashMap<String, Server>();

    public static Server getServer(ServerConfig serverConfig) {
        ServerTransportConfig serverTransportConfig = convertConfig(serverConfig);
        Server server = SERVERMAP.get(Integer.toString(serverTransportConfig.getPort()));
        if(server == null) server = initServer(serverTransportConfig);
        return server;
    }

    private synchronized static Server initServer(ServerTransportConfig serverTransportConfig){
        String key = Integer.toString(serverTransportConfig.getPort());
        Server server = SERVERMAP.get(key);
        if(server == null){
            switch (serverTransportConfig.getProtocolType()){
                case rest:
                    server = new HttpRestServer(serverTransportConfig);
                    break;
                case webservice:
                    server = new HttpWsServer(serverTransportConfig);
                    break;
                case jaxws:
                    server = new HttpWsServer(serverTransportConfig);
                    break;
                case jsf: // jsf目前支持jsf+dubbo
                    server = new Server(serverTransportConfig);
                    break;
//                case dubbo: // 服务端支持dubbo协议，但功能暂时不开放
//                    server = new Server(serverTransportConfig);
//                    break;
                default:
                    throw new InitErrorException("[JSF-23000]Unsupported protocol type of server:"
                            + serverTransportConfig.getProtocolType().name());
            }
            SERVERMAP.put(key,server);
        }
        return server;
    }

    private static ServerTransportConfig convertConfig(ServerConfig serverConfig){

        ServerTransportConfig serverTransportConfig = new ServerTransportConfig();
        serverTransportConfig.setPort(serverConfig.getPort());
        serverTransportConfig.setProtocolType(Constants.ProtocolType.valueOf(serverConfig.getProtocol()));
        serverTransportConfig.setHost(serverConfig.getBoundHost());
        serverTransportConfig.setPrintMessage(serverConfig.isDebug());
        serverTransportConfig.setContextPath(serverConfig.getContextpath());
        serverTransportConfig.setServerBusinessPoolSize(serverConfig.getThreads());
        serverTransportConfig.setServerBusinessPoolType(serverConfig.getThreadpool());
        serverTransportConfig.setChildNioEventThreads(serverConfig.getIothreads());
        serverTransportConfig.setConnectListeners(serverConfig.getOnconnect());
        serverTransportConfig.setMaxConnection(serverConfig.getAccepts());
        serverTransportConfig.setBuffer(serverConfig.getBuffer());
        serverTransportConfig.setPayload(serverConfig.getPayload());
        serverTransportConfig.setTelnet(serverConfig.isTelnet());
        serverTransportConfig.setUseEpoll(serverConfig.isEpoll());
        serverTransportConfig.setPoolQueueType(serverConfig.getQueuetype());
        serverTransportConfig.setPoolQueueSize(serverConfig.getQueues());
        serverTransportConfig.setDispatcher(serverConfig.getDispatcher());
        serverTransportConfig.setDaemon(serverConfig.isDaemon());
        serverTransportConfig.setParameters(serverConfig.getParameters());
        return serverTransportConfig;

    }

    /**
     * 得到全部服务端
     *
     * @return 全部服务端
     */
    public static List<Server> getServers() {
        return new ArrayList<Server>(SERVERMAP.values());
    }

    /**
     * 关闭全部服务端
     */
    public static void destroyAll() {
        LOGGER.info("Destroy all server");
        for (Map.Entry<String, Server> entry : SERVERMAP.entrySet()){
            String key = entry.getKey();
            Server server = entry.getValue();
            try {
                server.stop();
                SERVERMAP.remove(key);
            } catch(Exception e) {
                LOGGER.error("Error when destroy server with key:" + key, e);
            }
        }
    }
}