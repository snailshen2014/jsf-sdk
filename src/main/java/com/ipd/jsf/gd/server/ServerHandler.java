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
package com.ipd.jsf.gd.server;

import io.netty.channel.Channel;

/**
 * Title: 服务端的Handler<br>
 * <p/>
 * Description: 提供注册/反注册服务，处理请求<br>
 * <p/>
 */
public interface ServerHandler {

    /**
     * 注册服务，将Invoker注册到端口
     *
     * @param instanceName
     *         服务实例关键字
     * @param invoker
     */
    void registerProcessor(String instanceName, Invoker invoker);

    /**
     * 反注册服务，从端口上删掉Invoker
     *
     * @param instanceName
     *         服务实例关键字
     */
    void unregisterProcessor(String instanceName);

    /**
     * 处理请求（可以实时或者丢到线程池）
     *
     * @param channel
     *         连接（结果可以写入channel）
     * @param requestMsg
     *         请求
     */
    void handlerRequest(Channel channel, Object requestMsg);

    /**
     * 关闭服务
     */
    void shutdown();

}