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

import java.lang.reflect.Method;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 异常过滤器<br>
 * <p/>
 * Description: 1.如果抛出的异常方法上有声明则返回<br>
 * 2.如果是一些已知异常，则返回<br>
 * 3.未知异常，保证为RuntimeException返回<br>
 * <p/>
 */
public class ExceptionFilter extends AbstractFilter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionFilter.class);


    @Override
    public ResponseMessage invoke(RequestMessage request) throws RpcException {

        Invocation invocation = request.getInvocationBody();
        ResponseMessage response = null;

        // 先调用
        try {
            // 调用成功，或者调用返回已经封装的response
            response = getNext().invoke(request);
        } catch (Exception e) {
            // 此时的异常，是由于过滤器没有捕获导致的
            //LOGGER.warn("Catch unchecked and undeclared exception " + e.getClass().getCanonicalName()
            //                +" when invoke" + invocation.getClazzName() + "." + invocation.getMethodName(), e);
            response = MessageBuilder.buildResponse(request);
            response.setException(e);
        }

        // 解析exception
        try {

            if (!response.isError() // 没有错误
                    || GenericService.class.getCanonicalName().equals(invocation.getClazzName())) {
                return response;
            } else {
                try {
                    Throwable exception = response.getException();

                    // 跨语言 特殊处理。
                    if (response.getMsgHeader().removeByKey(Constants.HeadKey.srcLanguage) != null) {
                        response.getMsgHeader().addHeadKey(Constants.HeadKey.responseCode, (byte) 1); // 标记结果为错误
                        String json = JsonUtils.toJSONString(response.getException()); // 转为字符串
                        response.setResponse(json);
                        response.setException(null);
                        return response;
                    }

                    // 如果是checked异常，直接抛出
                    if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                        return response;
                    }
                    // 在方法签名上有声明，直接抛出
                    try {
                        Method method = ReflectUtils.getMethod(invocation.getClazzName(),
                                invocation.getMethodName(), invocation.getArgsType());
                        Class<?>[] exceptionClassses = method.getExceptionTypes();
                        for (Class<?> exceptionClass : exceptionClassses) {
                            if (exception.getClass().equals(exceptionClass)) {
                                return response;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return response;
                    }

                    // 未在方法签名上定义的异常，在服务器端打印ERROR日志
//                    LOGGER.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHostName()
//                            + ". service: " + invocation.getClazzName() + ", method: " + invocation.getMethodName()
//                            + ", exception: " + exception.getClass().getName() + ": " + exception.getMessage(), exception);

                    // 异常类和接口类在同一jar包里，直接抛出
                    String serviceFile = ReflectUtils.getCodeBase(ClassLoaderUtils.forName(invocation.getClazzName()));
                    String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                    if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)){
                        return response;
                    }
                    // 是JDK自带的异常，直接抛出
                    String className = exception.getClass().getName();
                    if (className.startsWith("java.") || className.startsWith("javax.")) {
                        return response;
                    }
                    // 是JSF本身的异常，直接抛出
                    if (exception instanceof RpcException || exception instanceof InitErrorException) {
                        return response;
                    }

                    // 否则，包装成RuntimeException抛给客户端
                    response.setException(new RuntimeException(ExceptionUtils.toString(exception)));
                } catch (Throwable e) {
//                    LOGGER.warn("Fail to ExceptionFilter when called by " + RpcContext.getContext().getRemoteHost()
//                            + ". service: " + invocation.getClazzName() + ", method: " + invocation.getMethodName()
//                            + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
                    return response;
                }
            }

            // 没有错误
            return response;
        } catch (RuntimeException e) {
//            LOGGER.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHost()
//                    + ". service: " + invocation.getClazzName() + ", method: " + invocation.getMethodName()
//                    + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }

    }

}