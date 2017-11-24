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
import com.ipd.jsf.gd.filter.limiter.LimiterFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.filter.limiter.ProviderInvokerLimiter;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: <br>
 *     服务端限制流量filter
 * <p>
 * Description: <br>
 *     具体配置通过注册中心下发,粒度控制到接口+方法+alias.
 * </p>
 *
 * @since 2016/04/27 11:20
 */
public class ProviderInvokeLimitFilter extends AbstractFilter{

    private static final Logger logger = LoggerFactory.getLogger(ProviderInvokeLimitFilter.class);
    @Override
    public ResponseMessage invoke(RequestMessage request) {
        //全局的开关是否是开启状态
        if (LimiterFactory.isGlobalProviderLimitOpen()){
            Invocation invocation = request.getInvocationBody();
            String interfaceId = invocation.getClazzName();
            String methodName = invocation.getMethodName();
            String alias = invocation.getAlias();
            //获取调用方的appId
            String appId = (String) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_APPID);
            if (StringUtils.isEmpty(appId)){
                appId = "";
            }
            ProviderInvokerLimiter limiter = LimiterFactory.getProviderLimiter(interfaceId,methodName,alias,appId);
            if ( limiter != null && limiter.isOverLimit(interfaceId,methodName,alias,appId)){
                ResponseMessage responseMessage = MessageBuilder.buildResponse(request);
                String message = "[JSF-22211]Invocation of " + interfaceId + "." + methodName + " of app:" + appId
                        + " is over invoke limit:["+limiter.getLimit()+"], please wait next period or add upper limit.";
                responseMessage.setException(new RpcException(message));
                return responseMessage;
            }
        }
        return getNext().invoke(request);
    }
}