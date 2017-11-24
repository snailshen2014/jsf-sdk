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

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.filter.mock.MockDataFactroy;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: Mock过滤器<br>
 * <p/>
 * Description: 服务端和客户端通用，就是如果设置mock=true就走本地mock实现<br>
 * <p/>
 */
public class MockFilter extends AbstractFilter {

    /**
     * 配置信息
     */
    private final AbstractInterfaceConfig config;

    /**
     * 构造函数
     *
     * @param config
     *         ProviderConfig
     */
    public MockFilter(AbstractInterfaceConfig config) {
        this.config = config;
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String interfaceId = invocation.getClazzName();
        String methodName = invocation.getMethodName();
        String alias = invocation.getAlias();
        // 如果在注册中心中配置了
        Object mockResult = MockDataFactroy.getResultFromCache(interfaceId, methodName, alias);
        if (mockResult != null) {
            // mock 调用
            ResponseMessage responseMessage = MessageBuilder.buildResponse(request);
            responseMessage.setResponse(mockResult);
            return responseMessage;
        } else {
            // 远程调用
            ResponseMessage responseMessage = getNext().invoke(request);
            return responseMessage;
        }
    }
}