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

import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.transport.ClientTransport;

/**
 * Title: 代表一个连接，封装了Provider和Transport<br>
 * <p/>
 * Description: 由于长连接复用，transport里的provider不一定是真正的Provider <br>
 * <p/>
 */
public class Connection {

    private final Provider provider;

    private final ClientTransport transport;

    public Connection(Provider provider, ClientTransport transport) {
        this.provider = provider;
        this.transport = transport;
    }

    public Provider getProvider() {
        return provider;
    }

    public ClientTransport getTransport() {
        return transport;
    }
}