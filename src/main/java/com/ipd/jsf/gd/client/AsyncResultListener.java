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
package com.ipd.jsf.gd.client;

import java.util.ArrayList;
import java.util.List;

import com.ipd.jsf.gd.msg.ResponseListener;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.transport.CallbackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 异步调用的结果Listener<br>
 * <p/>
 * Description: 将用户层的ResponseListener包装为transport层的ResultListener<br>
 */
public class AsyncResultListener implements ResultListener {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncResultListener.class);

    /**
     * The Listeners.
     */
    private List<ResponseListener> listeners;

    /**
     * Operation complete.
     *
     * @param future
     *         the future
     * @return the boolean
     * @see ClientProxyInvoker#notifyResponseListener(String, ResponseMessage)
     */
    @Override
    public boolean operationComplete(MsgFuture future) {
        if (listeners == null || listeners.isEmpty()) {
            return false;
        }
        final ResponseMessage response = (ResponseMessage) future.getNow();
        CallbackUtil.getCallbackThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("call async listener");
                }
                if (response.isError()) {
                    Throwable responseException = response.getException();
                    for (ResponseListener responseListener : listeners) {
                        try {
                            responseListener.catchException(responseException);
                        } catch (Exception e) {
                            LOGGER.warn("notify response listener error", e);
                        }
                    }
                } else {
                    Object result = response.getResponse();
                    for (ResponseListener responseListener : listeners) {
                        try {
                            responseListener.handleResult(result);
                        } catch (Exception e) {
                            LOGGER.warn("notify response listener error", e);
                        }
                    }
                }
            }
        });
        return true;
    }

    /**
     * Add response listener.
     *
     * @param responseListener
     *         the response listener
     */
    public void addResponseListener(ResponseListener responseListener) {
        if (responseListener == null) {
            return;
        }
        if (listeners == null) {
            listeners = new ArrayList<ResponseListener>();
        }
        listeners.add(responseListener);
    }

    /**
     * Add response listeners.
     *
     * @param responseListeners
     *         the response listeners
     */
    public void addResponseListeners(List<ResponseListener> responseListeners) {
        if (responseListeners == null) {
            return;
        }
        if (listeners == null) {
            listeners = new ArrayList<ResponseListener>();
        }
        listeners.addAll(responseListeners);
    }

}