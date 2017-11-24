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
package com.ipd.jsf.gd.filter.limiter.bucket;

import com.ipd.jsf.gd.error.RpcException;

/**
 * Title: <br>
 * <p>
 * Description: <br>
 *     没有可用token,抛出此异常
 * </p>
 *
 * @since 2016/04/26 22:40
 */
public class LimitedException extends RpcException {

    public LimitedException() {
    }

    public LimitedException(String message) {
        super(message);
    }

    public LimitedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LimitedException(Throwable cause) {
        super(cause);
    }

}