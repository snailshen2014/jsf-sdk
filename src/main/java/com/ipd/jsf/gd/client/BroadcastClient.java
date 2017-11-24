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
package com.ipd.jsf.gd.client;

import java.util.Map;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.registry.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.transport.ClientTransport;

/**
 * Title: 广播请求，配置为broadcast<br>
 * <p/>
 * Description: 一个请求派发给全部存活的provider，不支持异步调用方式<br>
 *     而且结果为最后一台的结果，不是全部结果
 * <p/>
 */
@Deprecated
public class BroadcastClient extends Client {
    
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(BroadcastClient.class);

    /**
     * 构造方法
     *
     * @param consumerConfig
     *         客户端配置
     */
    public BroadcastClient(ConsumerConfig<?> consumerConfig) {
        super(consumerConfig);
    }

    @Override
    protected ResponseMessage doSendMsg(RequestMessage msg) {
        RpcException exception = null;
        ResponseMessage result = null; // TODO 是否检查回参类型，返回List？
        // 循环调用全部连接的服务端
        for (Map.Entry<Provider, ClientTransport> entry : connectionHolder.getAliveConnections().entrySet()) {
            Provider provider = entry.getKey();
            ClientTransport connection = entry.getValue();
            if (connection != null && connection.isOpen()) {
                try {
                    result = super.sendMsg0(new Connection(provider, connection), msg);
                } catch (RpcException e) {
                    exception = e;
                    LOGGER.warn(e.getMessage(), e);
                } catch (Throwable e) {
                    exception = new RpcException(e.getMessage(), e);
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }
}