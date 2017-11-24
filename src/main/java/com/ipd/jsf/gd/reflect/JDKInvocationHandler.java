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
package com.ipd.jsf.gd.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.server.Invoker;

/**
 * Title: JDK代理处理器，拦截请求变为invocation进行调用<br>
 * <p/>
 * Description: 支持hessian序列化<br>
 * <p/>
 */
public class JDKInvocationHandler implements InvocationHandler {

    private Invoker proxyInvoker;

    public JDKInvocationHandler(Invoker proxyInvoker) {
        this.proxyInvoker = proxyInvoker;
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] paramValues)
            throws Throwable {
        String methodName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && paramTypes.length == 0) {
            return proxyInvoker.toString();
        } else if ("hashCode".equals(methodName) && paramTypes.length == 0) {
            return proxyInvoker.hashCode();
        } else if ("equals".equals(methodName) && paramTypes.length == 1) {
            return proxyInvoker.equals(paramValues[0]);
        }
        RequestMessage requestMessage = MessageBuilder.buildRequest(method.getDeclaringClass(),
                methodName, paramTypes, paramValues);
        ResponseMessage responseMessage = proxyInvoker.invoke(requestMessage);
        if(responseMessage.isError()){
            throw responseMessage.getException();
        }
        return responseMessage.getResponse();
    }
}