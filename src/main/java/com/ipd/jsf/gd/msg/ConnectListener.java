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

import io.netty.channel.ChannelHandlerContext;

/**
 * Title: server连接事件<br>
 * <p/>
 * Description: Server端当有客户端建立连接和销毁连接的时候，注意只做一些快速的动作，防止阻塞IO线程<br>
 * <p/>
 */
public interface ConnectListener {

    /**
     * 客户端建立连接的时候，服务端触发的事件
     *
     * @param ctx the ctx
     */
    public void connected(ChannelHandlerContext ctx);

    /**
     * 客户端断开连接的时候，服务端触发的事件
     *
     * @param ctx the ctx
     */
    public void disconnected(ChannelHandlerContext ctx);
}