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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.transport.ServerTransportConfig;

/**
 * Created on 14-5-7.
 */
public class ServerHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandlerFactory.class);

    public static ServerHandler getServerHandler(ServerTransportConfig config) {
        ServerHandler handler = null;
        switch (config.getProtocolType()) {
            case jsf:
                handler = BaseServerHandler.getInstance(config);
                break;
            case rest:
                break;
            case dubbo:
                handler = BaseServerHandler.getInstance(config);
                break;
            case webservice:
                break;
            case jaxws:
                break;
            default:
                throw new InitErrorException("[JSF-23001]Unsupported protocol type of server handler:" + config.getProtocolType());
        }
        return handler;
    }

}