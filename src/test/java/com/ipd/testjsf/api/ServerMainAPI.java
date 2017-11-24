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
package com.ipd.testjsf.api;

import java.io.UnsupportedEncodingException;

import com.ipd.testjsf.HelloService;
import com.ipd.testjsf.HelloServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.config.ServerConfig;

/**
 * Title: <br>
 *
 * Description: <br>
 */
public class ServerMainAPI {

    private final static Logger logger = LoggerFactory.getLogger(ServerMainAPI.class);

    /**
     * Method Name main
     *
     * @param args
     *            Return Type void
     * @throws java.io.UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException {

        HelloService helloService = new HelloServiceImpl();


        // 服务提供者协议配置（必须）
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setProtocol("jsf");
        logger.info("实例ServerConfig");

        // 服务提供者连接注册中心，设置属性
        ProviderConfig<HelloService> providerConfig = new ProviderConfig<HelloService>();
        providerConfig.setInterfaceId("com.ipd.testjsf.HelloService");
        providerConfig.setRef(helloService);
        providerConfig.setAlias("JSF:0.0.1");
        providerConfig.setServer(serverConfig); // 多个server用list
        logger.info("实例ProviderConfig");

        // 暴露及注册服务
        providerConfig.export();
        logger.info("发布服务完成");

//		providerConfig.unexport();
//		logger.info("反发布服务完成");

        // 启动本地服务，然后hold住本地服务
        synchronized (ServerMainAPI.class) {
            while (true) {
                try {
                    ServerMainAPI.class.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        // JSFContext.destroy();
    }
}