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
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 方法调用验证器<br>
 * <p/>
 * Description: 检查服务端接口发布了哪些方法，放在此处是为了generic解析后再判断<br>
 * <p/>
 */
public class ProviderMethodCheckFilter extends AbstractFilter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderMethodCheckFilter.class);

    /**
     * The Provider config.
     */
    private final ProviderConfig providerConfig;

    /**
     * Instantiates a new Provider method check filter.
     *
     * @param providerConfig the provider config
     */
    public ProviderMethodCheckFilter(ProviderConfig providerConfig){
        this.providerConfig = providerConfig;
    }

    /**
     * Invoke response message.
     *
     * @param request the request
     * @return the response message
     */
    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String methodName = invocation.getMethodName();

        // 判断服务下方法的黑白名单
        Boolean include = (Boolean) providerConfig.getMethodsLimit().get(methodName);
        ResponseMessage response;
        if (include == null || !include) { // 服务端未暴露这个方法
            response = MessageBuilder.buildResponse(request);
            RpcException rpcException;
            if ("$invoke".equals(methodName) && invocation.getArgs().length == 3) {
                // 可能是dubbo2.3.2发来的请求，没有带上"generic"标记
                rpcException = new RpcException("[JSF-22203]Provider of " + invocation.getClazzName()
                        + " didn't export method named \"" + methodName + "\", maybe you are using"
                        + " SAF(<1.0.9) for generic invoke to JSF, please upgrade to JSF or SAF(>=1.0.9).");
            } else {
                rpcException = new RpcException("[JSF-22203]Provider of " + invocation.getClazzName()
                        + " didn't export method named \"" + methodName + "\", maybe provider"
                        + " has been exclude it, or don't have this method!");
            }
            response.setException(rpcException);
        } else {
            // 调用
            response = getNext().invoke(request);
        }
        // 得到结果
        return response;
    }

}