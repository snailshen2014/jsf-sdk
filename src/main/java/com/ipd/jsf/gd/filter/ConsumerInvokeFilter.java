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
package com.ipd.jsf.gd.filter;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.client.Client;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.server.Invoker;

/**
 * Title: 调用过滤器<br>
 * <p/>
 * Description: 执行真正的调用过程，使用client发送数据给server<br>
 * <p/>
 */
public class ConsumerInvokeFilter implements Filter {

    /**
     * slf4j logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerInvokeFilter.class);

    /**
     * The Consumer config.
     */
    private ConsumerConfig<?> consumerConfig;

    /**
     * The Client.
     */
    private Client client;

    /**
     * Instantiates a new Consumer invoke filter.
     *
     * @param consumerConfig
     *         the consumer config
     * @param client
     *         the client
     */
    public ConsumerInvokeFilter(ConsumerConfig<?> consumerConfig, Client client) {
        this.consumerConfig = consumerConfig;
        this.client = client;
    }

    /**
     * Invoke response message.
     *
     * @param requestMessage
     *         the request message
     * @return the response message
     * @see Filter#invoke(RequestMessage)
     */
    @Override
    public ResponseMessage invoke(RequestMessage requestMessage) {
        // 优先本地调用，本地没有或者已经unexport，调用远程
        if (consumerConfig.isInjvm()) {
            Invoker injvmProviderInvoker = BaseServerHandler.getInvoker(consumerConfig.getInterfaceId(),
                    consumerConfig.getAlias());
            if (injvmProviderInvoker != null) {
                return injvmProviderInvoker.invoke(requestMessage);
            }
        }
        // 目前只是通过client发送给服务端
        try {
            return client.sendMsg(requestMessage);
        } catch (RpcException e) {
            ResponseMessage response = MessageBuilder.buildResponse(requestMessage);
            response.setException(e);
            return response;
        }
    }

}