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
import com.ipd.jsf.gd.filter.validation.Validator;
import com.ipd.jsf.gd.filter.validation.ValidatorFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 参数校验过滤器<br>
 * <p/>
 * Description: 支持接口级或者方法级配置，服务端和客户端都可以配置，需要引入第三方jar包<br>
 * <p/>
 */
public class ValidationFilter extends AbstractFilter {

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        String methodName = request.getMethodName();
        // 该方法开启校验
        if (getBooleanMethodParam(methodName, Constants.CONFIG_KEY_VALIDATION, false)) {
            // 自定义参数<jsf:param>是否有自定义jsr303实现
            String customImpl = getStringMethodParam(methodName, Constants.HIDE_KEY_PREFIX + "customImpl",null);
            String className = request.getClassName();
            Validator validator = ValidatorFactory.getValidator(className, customImpl);
            try {
                Invocation invocation = request.getInvocationBody();
                validator.validate(methodName, invocation.getArgsType(), invocation.getArgs());
            } catch (Exception e) { // 校验出现异常
                ResponseMessage response = MessageBuilder.buildResponse(request);
                RpcException re;
                if (RpcContext.getContext().isProviderSide()) { // 无法直接序列化异常，只能转为字符串然后包装为RpcException
                    re = new RpcException("[JSF-22209]validate is not passed, cause by: " + ExceptionUtils.toString(e));
                } else {
                    re = new RpcException("[JSF-22210]validate is not passed, cause by: " + e.getMessage(), e);
                }
                response.setException(re);
                return response;
            }
        }
        return getNext().invoke(request);
    }
}