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

import io.netty.channel.Channel;

/**
 * 返回用户输入的信息
 * 一般用于测试服务是否正常
 *
 */
public class EchoTelnetHandler implements TelnetHandler {

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getCommand()
	 */
	@Override
	public String getCommand() {
		return "echo";
	}

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "echo the client info.Excemple: echo test";
	}

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
		return message;
	}

}