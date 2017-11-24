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

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.RpcStatus;

/**
 * Title: 服务端并发限制器<br>
 * <p/>
 * Description: 方法级别的并发限制<br>
 * <p/>
 */
public class ProviderConcurrentsFilter extends AbstractFilter {

    /**
     * 配置信息
     */
    private final ProviderConfig config;

    /**
     * 构造函数
     *
     * @param config
     *         ProviderConfig
     */
    public ProviderConcurrentsFilter(ProviderConfig config) {
        this.config = config;
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String interfaceId = invocation.getClazzName();
        String methodName = invocation.getMethodName();
        int concurrents = getIntMethodParam(methodName, Constants.CONFIG_KEY_CONCURRENTS, config.getConcurrents());
        if (concurrents > 0) {
            // 判断是否超过并发数大小
            RpcStatus count = RpcStatus.getMethodStatus(config, methodName);
            if (count.getActive() >= concurrents) {
                throw new RpcException("[JSF-22208]Failed to invoke method " + interfaceId + "." + methodName
                        + ", The service using threads greater than: " + concurrents + ". Change it by "
                        + "<jsf:provider concurrents=\"\"/> or <jsf:method concurrents=\"\"/> on provider");
            }
        }
        boolean isException = false;
        long begin = JSFContext.systemClock.now();
        RpcStatus.beginCount(config, methodName);
        try {
            ResponseMessage response = getNext().invoke(request);
            isException = response.isError();
            return response;
        } catch (Throwable t) {
            isException = true;
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RpcException("unexpected exception", t);
            }
        } finally {
            RpcStatus.endCount(config, methodName, JSFContext.systemClock.now() - begin, !isException);
        }
    }
}