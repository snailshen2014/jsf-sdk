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

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.codec.dubbo.DubboDecoder;
import com.ipd.jsf.gd.codec.dubbo.DubboEncoder;
import com.ipd.jsf.gd.codec.jsf.JSFDecoder;
import com.ipd.jsf.gd.codec.jsf.JSFEncoder;
import com.ipd.jsf.gd.error.InitErrorException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Title: 初始化客户端的ChannelPipeline<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@ChannelHandler.Sharable
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ClientChannelHandler clientChannelHandler;

    private ClientTransportConfig transportConfig;

    public ClientChannelInitializer(ClientTransportConfig transportConfig) {
        this.transportConfig = transportConfig;
        this.clientChannelHandler = new ClientChannelHandler(transportConfig);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 根据服务端协议，选择解码器
        Constants.ProtocolType type = transportConfig.getProvider().getProtocolType();
        switch (type) {
            case jsf:
                pipeline.addLast(new JSFEncoder());
                pipeline.addLast(new JSFDecoder(transportConfig.getPayload()));
                break;
            case dubbo:
                pipeline.addLast(new DubboEncoder());
                pipeline.addLast(new DubboDecoder(transportConfig.getPayload()));
                break;
            default:
                throw new InitErrorException("Unsupported client protocol type : " + type.name());
        }
        pipeline.addLast(Constants.CLIENT_CHANNELHANDLE_NAME, clientChannelHandler);
    }

    public ClientChannelHandler getClientChannelHandler() {
        return clientChannelHandler;
    }
}