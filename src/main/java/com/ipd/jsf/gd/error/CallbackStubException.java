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
 * Title: callback已经失效的异常<br>
 *
 * Description: Remove the stub object when get this Exception<br>
 */
public class CallbackStubException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5669745421147050696L;

    protected CallbackStubException() {
    }

    private String errorMsg;

    public CallbackStubException(String errorMsg){
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public CallbackStubException(String errorMsg,Throwable throwable){
        super(errorMsg,throwable);
        this.errorMsg = errorMsg;

    }
}