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
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import com.ipd.jsf.gd.util.DateUtils;
import io.netty.channel.Channel;

/**
 * 取得当前服务进程使用内存的状态
 *
 */
public class JVMStatusTelnetHandler implements TelnetHandler {

	@Override
	public String getCommand() {
		return "jvm";
	}

	@Override
	public String getDescription() {
		return "Display current JVM's status. ";
	}

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
		StringBuilder sb = new StringBuilder();
		//内存使用情况
        MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
        long max = mmxb.getHeapMemoryUsage().getMax();
        long used = mmxb.getHeapMemoryUsage().getUsed();
        long init = mmxb.getHeapMemoryUsage().getInit();
        long commit = mmxb.getHeapMemoryUsage().getCommitted();
        sb.append("********Memory status******************").append(line);
        sb.append("Max JVM Heap Memory:").append(max / 1024 / 1024).append("M").append(line)
                .append("Used Heap Memory:").append(used / 1024 / 1024).append("M").append(line)
                .append("Init Heap Memory:").append(init / 1024 / 1024).append("M").append(line)
                .append("Commited Heap Memory:").append(commit/1024/1024).append("M").append(line);
        
        sb.append("********Thread status********************").append(line);
        //线程数
        ThreadMXBean txmb = ManagementFactory.getThreadMXBean();
        sb.append("Peak thread count:").append(txmb.getPeakThreadCount()).append("").append(line)
                .append("Thread count:").append(txmb.getThreadCount()).append("").append(line);

        sb.append("********Runtime status******************").append(line);
        //启动入口参数
        RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
        sb.append("InputArguments:[");
        for(String ia : rmxb.getInputArguments()){
        	sb.append(ia).append(",");
        }
        sb.deleteCharAt(sb.length()-1).append("]").append(line);
        sb.append("JVM start time:").append(DateUtils.dateToStr(new Date(rmxb.getStartTime()))).append(line);
        
        return sb.toString();
	}

}