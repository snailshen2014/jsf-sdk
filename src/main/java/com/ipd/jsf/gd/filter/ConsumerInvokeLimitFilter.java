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
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;

/**
 * Title: 如果本app超过了调用次数限制，则不允许发起调用<br>
 * <p/>
 * Description: 限制是从注册中心以下发配置开关的方式发来的，根据接口+方法+app来打开或者关闭开关<br>
 * <p/>
 */
public class ConsumerInvokeLimitFilter extends AbstractFilter {

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        if (LimiterFactory.isFunctionOpen()) { // 开启了这个功能，
            Invocation invocation = request.getInvocationBody();
            String interfaceId = invocation.getClazzName();
            String methodName = invocation.getMethodName();
            String alias = invocation.getAlias();
            String appId = (String) JSFContext.get(JSFContext.KEY_APPID);
            if (LimiterFactory.isOverLimit(interfaceId, methodName, alias, appId)) {
                ResponseMessage responseMessage = MessageBuilder.buildResponse(request);
                String message = "[JSF-22206]Invocation of " + interfaceId + "." + methodName + " of app:" + appId
                        + " is over invoke limit, please wait next period or add upper limit.";
                responseMessage.setException(new RpcException(message));
                return responseMessage;
            }
        }
        return getNext().invoke(request);
    }
}