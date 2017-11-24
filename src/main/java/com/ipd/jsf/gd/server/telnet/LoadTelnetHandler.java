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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

import io.netty.channel.Channel;

/**
 * 取得服务负载情况
 *
 */
public class LoadTelnetHandler implements TelnetHandler {

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getCommand()
	 */
	@Override
	public String getCommand() {
		return "loadstatus";
	}

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Show CPU system load average. Excemple: loadstatus";
	}

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    	double load;
    	try {
    	    Method method = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage", new Class<?>[0]);
    	    load = (Double)method.invoke(operatingSystemMXBean, new Object[0]);
    	} catch (Throwable e) {
    	    load = -1;
    	}
    	int cpu = operatingSystemMXBean.getAvailableProcessors();
		return "load:[" + load + "] cpu:[" + cpu + "]";
	}

}