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

import java.util.Arrays;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 服务端用，记录超时用<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ProviderTimeoutFilter extends AbstractFilter{

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderTimeoutFilter.class);

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        long start = JSFContext.systemClock.now();
        Invocation invocation = request.getInvocationBody();
        ResponseMessage response = getNext().invoke(request);
        long elapsed = JSFContext.systemClock.now() - start;
        int providerTimeout = getIntMethodParam(invocation.getMethodName(), Constants.CONFIG_KEY_TIMEOUT, Constants
                .DEFAULT_CLIENT_INVOKE_TIMEOUT);
        if (elapsed > providerTimeout) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("[JSF-22204]Provider invoke method [" + invocation.getClazzName() + "."
                        + invocation.getMethodName() + "] timeout. "
                        + "The arguments is: " + Arrays.toString(invocation.getArgs())
                        + ", timeout is " + providerTimeout + " ms, invoke elapsed " + elapsed + " ms.");
            }
        }
        invocation.addAttachment(Constants.INTERNAL_KEY_ELAPSED,  (int) elapsed);
        return response;
    }
}