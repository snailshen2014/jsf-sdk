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

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseListener;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: 调用端的泛化调用过滤器<br>
 * 
 * Description: 将泛化调用拼成普通调用，注意有可能参数值和参数类型不匹配，要服务端处理<br>
 *
 * @see GenericService
 */
public class ConsumerGenericFilter extends AbstractFilter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerGenericFilter.class);

	/**
	 * @see AbstractFilter#invoke(RequestMessage)
	 */
	@Override
    public ResponseMessage invoke(RequestMessage request) {

        Invocation invocation = request.getInvocationBody();
        String methodName = invocation.getMethodName();

        Object[] args = invocation.getArgs();
        // generic 调用 consumer不处理，服务端做转换
        //if (GenericService.class.getCanonicalName().equals(invocation.getClazzName())) {
        if ("$invoke".equals(methodName) && args.length == 3) {
            invocation.setMethodName(StringUtils.toString(args[0]));
            invocation.addAttachment(Constants.CONFIG_KEY_GENERIC, true);
        } else if ("$asyncInvoke".equals(methodName) && args.length == 4) {
            invocation.setMethodName(StringUtils.toString(args[0]));
            invocation.addAttachment(Constants.CONFIG_KEY_GENERIC, true);
            invocation.addAttachment(Constants.INTERNAL_KEY_ASYNC, true);

            ResponseListener responseListener = (ResponseListener) args[3];
            // 干掉最后一个参数
            String[] newArgTypes = new String[3];
            Object[] newArgs = new Object[3];
            System.arraycopy(invocation.getArgsType(), 0, newArgTypes, 0, 3);
            System.arraycopy(args, 0, newArgs, 0, 3);
            invocation.setArgsType(newArgTypes);
            invocation.setArgs(newArgs);
            // 删掉最后一个参数
            if (responseListener != null) {
                invocation.addAttachment(Constants.INTERNAL_KEY_ONRETURN, responseListener);
            }
        }

        ResponseMessage response = getNext().invoke(request);
        // 返回的如果是自定义对象 则返回的是Map
        return response;
    }
}