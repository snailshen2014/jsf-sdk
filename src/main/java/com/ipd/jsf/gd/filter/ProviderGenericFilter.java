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
import java.net.InetSocketAddress;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.PojoUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.bk.saf.SafBaseException;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 服务端的泛化调用过滤器<br>
 * <p/>
 * Description: 如果是generic请求，那么可能传递的参数值和参数类型不匹配 需要转换<br>
 * <p/>
 */
public class ProviderGenericFilter extends AbstractFilter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderGenericFilter.class);

    /**
     * @see AbstractFilter#invoke(RequestMessage)
     */
    @Override
    public ResponseMessage invoke(RequestMessage request) {

        Invocation invocation = request.getInvocationBody();
        Boolean generic = (Boolean) invocation.getAttachment(Constants.CONFIG_KEY_GENERIC);
        // 如果是generic请求，
        if (CommonUtils.isTrue(generic)) {
            // 转换为正常请求
            genericToNormal(invocation);
            try {
                Method method = ReflectUtils.getMethod(invocation.getClazzName(),
                        invocation.getMethodName(), invocation.getArgsType());

                Class[] paramTypes = invocation.getArgClasses();
                Object[] paramValues = invocation.getArgs();
                paramTypes = paramTypes == null ? Codec.EMPTY_CLASS_ARRAY : paramTypes; // 如果客户端写的是null
                paramValues = paramValues == null ? Codec.EMPTY_OBJECT_ARRAY : paramValues; // 如果客户端写的是null
                // 参数值类型 和 参数类型不匹配 解析数据
                Object[] newParamValues = PojoUtils.realize(paramValues, paramTypes, method.getGenericParameterTypes());
                invocation.setArgs(newParamValues);

                /*for (int i = 0; i < paramTypes.length; i++) {
                    Class paramType  = paramTypes[i];
                    Object paramValue = paramValues[i];
                    if (paramType.isAssignableFrom(paramValue.getClass())) {
                        // 可以转换
                    } else {
                        // 参数值类型 和 参数类型不匹配
                        if(paramValue instanceof Map){
                            Map paramMap = (Map) paramValue;
                            // map转对象
                            LOGGER.debug("convert pojo map to pojo");
                            Object newParamValue = PojoUtils.realize(paramMap, paramType, paramType);
                            paramValues[i] = newParamValue; // 修改参数值
                        } else {
                            Exception e = new RpcException("can't parse generic param : "
                                    + paramValue.getClass().getName() + " which need " + paramType.getName());
                            ResponseMessage response = MessageBuilder.buildResponse(request);
                            response.setException(e);
                        }
                    }
                }*/
            } catch (Exception e) {
                LOGGER.error("[JSF-22202]Failed to realize generic invocation of " + invocation.getClazzName()
                        + "." + invocation.getMethodName() + " from " + NetUtils.toAddressString((InetSocketAddress)
                        invocation.getAttachment(Constants.INTERNAL_KEY_REMOTE)) + ".", e);
                ResponseMessage response = MessageBuilder.buildResponse(request);
                response.setException(e);
                return response;
            }

            // 解析完毕后，将invocation从generic换成正常invocatio，往下调用
            ResponseMessage response = getNext().invoke(request);

            if(response.isError()){ // 有异常
                Throwable exception = response.getException();
                if (exception instanceof SafBaseException
                        || exception instanceof RpcException) {
                    return response; // SAF定义的异常或者rpc异常 直接返回
                }
                else { // 业务异常（可能业务异常类客户端没有，返回文本形式）
                    response.setException(new RpcException(ExceptionUtils.toString(exception)));
                }
            } else { // 无异常
                Object result = response.getResponse();
                result = PojoUtils.generalize(result);
                response.setResponse(result);
            }
            return response;
        } else {
            // 正常请求
            return getNext().invoke(request);
        }
    }

    private Invocation genericToNormal(Invocation invocation){
        Object[] genericArgs = invocation.getArgs();
        // 转为正常的请求，发给服务端
        invocation.setArgs((Object[]) genericArgs[2]);
        invocation.setArgsType((String[]) genericArgs[1]);
        invocation.setMethodName((String) genericArgs[0]);

        return invocation;
    }
}