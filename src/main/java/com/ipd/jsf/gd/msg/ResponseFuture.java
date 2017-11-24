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
package com.ipd.jsf.gd.msg;

import java.util.concurrent.TimeUnit;

import com.ipd.jsf.gd.client.MsgFuture;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 异步调用的响应结果<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ResponseFuture<T> {

    private final MsgFuture msgFuture;

    /**
     * 构造函数
     *
     * @param msgFuture
     */
    public ResponseFuture(MsgFuture msgFuture) {
        this.msgFuture = msgFuture;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return msgFuture.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return msgFuture.isCancelled();
    }

    public boolean isDone() {
        return msgFuture.isDone();
    }

    /**
     * 查看future耗时
     * @return
     */
    public long getUseTime() {
        return msgFuture.getDoneTime() - msgFuture.getGenTime();
    }

    /**
     * 获取数据，会抛出真正的异常
     *
     * @return 返回值
     * @throws Throwable
     *         业务异常或其他异常
     */
    public T get() throws Throwable {
        ResponseMessage response = (ResponseMessage) msgFuture.get();
        return (T) getResultFromResponse(response);
    }

    /**
     * 获取数据，会抛出真正的异常
     *
     * @param timeout
     *         超时时间
     * @param unit
     *         单位
     * @return 返回值
     * @throws Throwable
     *         业务异常或其他异常
     */
    public T get(long timeout, TimeUnit unit) throws Throwable {
        ResponseMessage response = (ResponseMessage) msgFuture.get(timeout, unit);
        return (T) getResultFromResponse(response);
    }

    private T getResultFromResponse(ResponseMessage response) throws Throwable {
        if (response.isError()) {
            throw response.getException();
        }
        return (T) response.getResponse();
    }

    /**
     * 函数式构造方式
     *
     * @param asyncCall
     *         异步调用对象
     * @return ResponseFuture
     */
    public static ResponseFuture build(AsyncCall asyncCall) {
        try {
            asyncCall.invoke();
        } catch (Throwable e) {
            throw new RpcException("Failed to do async call", e);
        }
        return RpcContext.getContext().getFuture();
    }

    /**
     * Title: 异步调用抽象接口<br>
     * <p/>
     * Description: <br>
     * <p/>
     */
    public interface AsyncCall {

        /**
         * 需要实现的远程调用方法，类似于<code>xxxService.doSomething(param1, param2)</code>
         *
         * @throws Throwable
         *         出现异常
         */
        public void invoke() throws Throwable;
    }
}