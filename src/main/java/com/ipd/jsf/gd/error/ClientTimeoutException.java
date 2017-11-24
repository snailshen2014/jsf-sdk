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
package com.ipd.jsf.gd.error;

/**
 * Title: 客户端等待超时的异常 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ClientTimeoutException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -3008927155169307876L;

    /**
     * Instantiates a new Client timeout exception.
     */
    protected ClientTimeoutException() {
    }

    /**
     * Instantiates a new Client timeout exception.
     *
     * @param errorMsg
     *         the error msg
     */
    public ClientTimeoutException(String errorMsg) {
        super(errorMsg);
    }

    /**
     * Instantiates a new Client timeout exception.
     *
     * @param errorMsg
     *         the error msg
     * @param throwable
     *         the throwable
     */
    public ClientTimeoutException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}