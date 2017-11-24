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

import com.ipd.jsf.gd.codec.AdapterDecoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Title: 初始化服务端的ChannelPipeline<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@ChannelHandler.Sharable
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ServerChannelHandler serverChannelHandler;

    private ConnectionChannelHandler connectionChannelHandler;

    private ServerTransportConfig transportConfig;

    public ServerChannelInitializer(ServerTransportConfig transportConfig){
        this.transportConfig = transportConfig;
        this.serverChannelHandler = new ServerChannelHandler(transportConfig);
        this.connectionChannelHandler = new ConnectionChannelHandler(transportConfig);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 只保留一个 根据第一次请求识别协议，构建后面的ChannelHandler
        ch.pipeline().addLast(connectionChannelHandler)
                .addLast(new AdapterDecoder(serverChannelHandler,
                        transportConfig.getPayload(), transportConfig.isTelnet()));
    }
}