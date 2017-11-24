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
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.RpcStatus;

/**
 * Title: 调用端并发限制器<br>
 * <p/>
 * Description: 按接口和方法进行限制<br>
 * <p/>
 */
public class ConsumerConcurrentsFilter extends AbstractFilter {

    /**
     * 配置信息
     */
    private final ConsumerConfig config;

    /**
     * 构造函数
     *
     * @param config
     *         ConsumerConfig
     */
    public ConsumerConcurrentsFilter(ConsumerConfig config) {
        this.config = config;
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String interfaceId = invocation.getClazzName();
        String methodName = invocation.getMethodName();
        int concurrents = getIntMethodParam(methodName, Constants.CONFIG_KEY_CONCURRENTS, config.getConcurrents());
        RpcStatus count = RpcStatus.getMethodStatus(config, methodName);
        if (concurrents > 0) { // 存在并发限制
            long timeout = getIntMethodParam(methodName, Constants.CONFIG_KEY_TIMEOUT, 5000);
            long start = JSFContext.systemClock.now();
            long remain = timeout;
            int active = count.getActive();
            if (active >= concurrents) {
                synchronized (count) {
                    while ((active = count.getActive()) >= concurrents) {
                        try {
                            count.wait(remain); // 等待执行
                        } catch (InterruptedException e) {
                        }
                        long elapsed = JSFContext.systemClock.now() - start;
                        remain = timeout - elapsed;
                        if (remain <= 0) {
                            throw new RpcException("[JSF-22207]Waiting concurrent timeout in client-side when invoke"
                                    + interfaceId + "." + methodName + ", elapsed: " + elapsed
                                    + ", timeout: " + timeout + ". concurrent invokes: " + active
                                    + ". max concurrents: " + concurrents + ". You can change it by "
                                    + "<jsf:consumer concurrents=\"\"/> or <jsf:method concurrents=\"\"/>");
                        }
                    }
                }
            }
        }
        try {
            boolean isException = false;
            long begin = JSFContext.systemClock.now();
            RpcStatus.beginCount(config, methodName);
            try {
                ResponseMessage response = getNext().invoke(request);
                isException = response.isError();
                return response;
            } catch (Throwable t) {
                isException = true;
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RpcException("unexpected exception", t);
                }
            } finally {
                RpcStatus.endCount(config, methodName, JSFContext.systemClock.now() - begin, !isException);
            }
        } finally {
            if (concurrents > 0) {
                synchronized (count) {
                    count.notify(); // 调用结束 通知等待的人
                }
            }
        }
    }

}