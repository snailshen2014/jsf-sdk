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

import com.ipd.jsf.gd.client.MsgFuture;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;

import java.net.InetSocketAddress;

/**
 * Title: 客户端服务端连接<br>
 * <p/>
 * Description: 处理建立/断开连接，收到数据等事件<br>
 * <p/>
 */
public interface ClientTransport {

    /**
     * 重连
     */
    void reconnect();

    /**
     * 关闭
     */
    void shutdown();

    /**
     * 得到配置
     *
     * @return ClientTransportConfig
     */
    ClientTransportConfig getConfig();

    /**
     * 异步调用
     *
     * @param msg
     *         the msg 消息
     * @param timeout
     *         the timeout 超时时间
     * @return 异步Future
     */
    MsgFuture sendAsyn(BaseMessage msg, int timeout);

    /**
     * 同步调用
     *
     * @param msg
     *         the msg 消息
     * @param timeout
     *         the timeout 超时时间
     * @return ResponseMessage
     */
    ResponseMessage send(BaseMessage msg, int timeout);

    /**
     * 是否开启
     *
     * @return the boolean
     */
    boolean isOpen();

    /**
     * 得到连接的远端地址
     *
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 得到连接的本地地址（如果是短连接，可能不准）
     *
     * @return the local address
     */
    InetSocketAddress getLocalAddress();

    /**
     * 当前请求数
     *
     * @return 当前请求数
     */
    int currentRequests();
}