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
package com.ipd.jsf.gd.server.telnet;

import java.net.InetSocketAddress;
import java.util.List;

import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.channel.Channel;

/**
 * 显示当前进程的协议和端口列表
 *
 */
public class PortTelnetHandler implements TelnetHandler {

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getCommand()
	 */
	@Override
	public String getCommand() {
		return "ps";
	}

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Show server ports." + line +
                "Usage:\tps \t\t\tshow listening port." + line +
                //"\tps -l\t\t\tshow listening port detail." + line +
                "\tps [port]\t\tshow established connect with port." + line;
                //"\tps -l [port]\t\tshow established connection detail with port." + line;
	}

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
        if (StringUtils.isBlank(message)) {
            StringBuilder sb = new StringBuilder();
            List<Channel> activeChannels = BaseServerHandler.getAllChannel();
            sb.append("count:").append(activeChannels.size()).append(line);
            for (Channel cn : activeChannels) {
                sb.append(NetUtils.channelToString(cn.remoteAddress(), cn.localAddress())).append(line);
            }
            return sb.toString();
        } else if ("?".equals(message) || "/?".equals(message)) {
            return getDescription();
        } else {
            boolean detail = true;
            if (message.startsWith("-c")) {
                detail = false;
                message = message.substring(2).trim();
            }
            try {
                int port = Integer.parseInt(message);
                StringBuilder sb = new StringBuilder();
                List<Channel> activeChannels = BaseServerHandler.getAllChannel();
                int count = 0;
                for (Channel cn : activeChannels) {
                    InetSocketAddress address = (InetSocketAddress) cn.localAddress();
                    if (NetUtils.toAddressString(address).endsWith(":" + port)) {
                        if (detail) {
                            sb.append(NetUtils.channelToString(cn.remoteAddress(), cn.localAddress())).append(line);
                        }
                        count++;
                    }
                }
                sb.insert(0, "count:" + count + line);
                return sb.toString();

            } catch (Exception e) {
                return "Invalid port : " + message;
            }
        }
    }
}