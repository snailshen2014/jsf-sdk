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
package com.ipd.jsf.gd.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 客户端连接抽象类<br>
 * <p/>
 * Description: 只是简单封装了地址。配置等信息<br>
 * <p/>
 */
abstract class AbstractClientTransport implements ClientTransport {

    /**
     * slf4j logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractClientTransport.class);

    /**
     * 本地地址
     */
    protected SocketAddress localAddress;

    /**
     * 远程地址
     */
    protected SocketAddress remoteAddress;

    /**
     * 正在发送的调用数量
     */
    protected AtomicInteger currentRequests = new AtomicInteger(0);

    /**
     * 当前的客户端配置
     */
    protected ClientTransportConfig clientTransportConfig;

    /**
     * 构造函数
     *
     * @param clientTransportConfig
     *         客户端配置
     */
    protected AbstractClientTransport(ClientTransportConfig clientTransportConfig) {
        this.clientTransportConfig = clientTransportConfig;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.remoteAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) this.localAddress;
    }

    @Override
    public ClientTransportConfig getConfig() {
        return clientTransportConfig;
    }

    @Override
    public int currentRequests() {
        return currentRequests.get();
    }
}