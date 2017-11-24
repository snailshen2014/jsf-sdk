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

import com.ipd.bk.saf.SafBaseException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: http网关调用过滤器<br>
 * <p/>
 * Description: 特殊处理<br>
 * <p/>
 */
public class ProviderHttpGWFilter<T> extends AbstractFilter {

    @Override
    public ResponseMessage invoke(RequestMessage request) {

        Invocation invocation = request.getInvocationBody();
        String fromGateway = StringUtils.toString(invocation.getAttachment("httpgw"));
        // 如果是网关请求
        if (CommonUtils.isTrue(fromGateway)) {
            ResponseMessage response = getNext().invoke(request);

            if (response.isError()) { // 有异常
                Throwable exception = response.getException();
                if (exception instanceof SafBaseException
                        || exception instanceof RpcException) {
                    return response; // SAF定义的异常或者rpc异常 直接返回
                } else { // 业务异常（可能业务异常类客户端没有，返回文本形式）
                    response.setException(new RpcException(exception.getMessage()));
                }
            }
            return response;
        } else {
            return getNext().invoke(request);
        }
    }

}