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
import com.ipd.jsf.gd.filter.FilterChain;
import com.ipd.jsf.gd.filter.ProviderInvokeFilter;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.msg.BaseMessage;

/**
 * Title: 服务端收到请求后，调用业务逻辑的Invoker<br>
 * <p/>
 * Description: 执行过滤器链，最后反射调到业务实现类<br>
 * <p/>
 */
public class ProviderProxyInvoker implements Invoker {
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderProxyInvoker.class);

    /**
     * 对应的客户端信息
     */
    private final ProviderConfig providerConfig;

    /**
     * 过滤器执行链
     */
    private final FilterChain filterChain;

    /**
     * 构造执行链
     *
     * @param providerConfig
     *         服务端配置
     */
    public ProviderProxyInvoker(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
        // 最底层是调用过滤器
        this.filterChain = FilterChain.buildProviderChain(providerConfig,
                new ProviderInvokeFilter(providerConfig));
    }

    /**
     * proxy拦截的调用
     *
     * @param requestMessage
     *         请求消息
     * @return 调用结果
     */
    @Override
    public ResponseMessage invoke(BaseMessage requestMessage) {
        RequestMessage request = (RequestMessage) requestMessage;

        ResponseMessage response = filterChain.invoke(request);
        // 得到结果
        return response;
    }

    /**
     * @return the providerConfig
     */
    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }


}