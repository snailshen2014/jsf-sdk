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

import java.util.Collection;

/**
 * Title: 远程调用时，没有可用的服务端 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class NoAliveProviderException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 399321029655641392L;

    /**
     * Instantiates a new No alive provider exception.
     *
     * @param key
     *         the key
     * @param serverIp
     *         the server ip
     */
    public NoAliveProviderException(String key, String serverIp) {
        super("[JSF-22011]No alive provider of pinpoint address : [" + serverIp + "]! The key is " + key);
    }

    /**
     * Instantiates a new No alive provider exception.
     *
     * @param key
     *         the key
     * @param providers
     *         the providers
     */
    public NoAliveProviderException(String key, Collection providers) {
        super("[JSF-22010]No alive provider! The key is " + key + ", current providers is " + providers);
    }

    /**
     * Instantiates a new No alive provider exception.
     */
    protected NoAliveProviderException() {

    }
}