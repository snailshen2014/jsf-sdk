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
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: token过滤器<br>
 * <p/>
 * Description: token过滤器，服务端和客户端配置一样的token才能完成调用<br>
 * <p/>
 */
public class TokenFilter extends AbstractFilter {

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String methodName = invocation.getMethodName();
        // providerToken在配置中
        String providerToken = super.getStringMethodParam(methodName, Constants.HIDDEN_KEY_TOKEN, null);
        if (providerToken != null) {
            // consumer在每次请求中
            String consumerToken = (String) invocation.getAttachment(Constants.HIDDEN_KEY_TOKEN);
            if (!providerToken.equals(consumerToken)) {
                RpcException exception = new RpcException("[JSF-22205]Invalid token! Invocation of "
                        + invocation.getClazzName() + "." + invocation.getMethodName()
                        + " from consumer " + invocation.getAttachment(Constants.INTERNAL_KEY_REMOTE)
                        + " to provider " + invocation.getAttachment(Constants.INTERNAL_KEY_LOCAL)
                        + " are forbidden by server. ");
                ResponseMessage response = MessageBuilder.buildResponse(request);
                response.setException(exception);
                return response;
            }
        }
        return getNext().invoke(request);
    }

}