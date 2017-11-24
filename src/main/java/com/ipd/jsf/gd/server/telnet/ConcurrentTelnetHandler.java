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

import com.ipd.jsf.gd.server.BusinessPool;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.transport.CallbackUtil;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Title: 取得状态统计<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ConcurrentTelnetHandler implements TelnetHandler {

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#getCommand()
     */
    @Override
    public String getCommand() {
        return "tp";
    }

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#getDescription()
     */
    @Override
    public String getDescription() {
        return "Display the threadpool statistics: "+ line +
        "Usage:\ttp        \t\t Show context and callback threadpool" + line +
                "\ttp [port]\t\t Show threadpool of port [port]" + line +
                "\ttp [port] [interval] [count]\t Show threadpool of port [port] time after time";
    }

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
        if (StringUtils.isBlank(message)) {
            StringBuilder sb = new StringBuilder();
            // 打印注册中心 等信息
            ConcurrentHashMap map = JSFContext.getContext();
            ThreadPoolExecutor pool = CallbackUtil.getCallbackThreadPool(false);
            if (pool != null) {
                HashMap poolmap = new HashMap();
                poolmap.put("min", pool.getCorePoolSize());
                poolmap.put("max", pool.getMaximumPoolSize());
                poolmap.put("current", pool.getPoolSize());
                poolmap.put("active", pool.getActiveCount());
                poolmap.put("queue", pool.getQueue().size());
                map.put("callbackpool", poolmap);
            }
            sb.append(JsonUtils.toJSONString(map));
            return sb.toString();
        } else if ("?".equals(message) || "/?".equals(message)) {
            return getDescription();
        } else {
            try {
                int interval = 1000;
                int count = 1;
                int port = -1;
                message = message.trim();
                String[] ss = message.split("\\s+");
                if (ss.length == 1) {
                    port = Integer.parseInt(ss[0]);
                } else if (ss.length == 2) {
                    port = Integer.parseInt(ss[0]);
                    interval = Integer.parseInt(ss[1]);
                } else if (ss.length > 2) {
                    port = Integer.parseInt(ss[0]);
                    interval = Integer.parseInt(ss[1]);
                    count = Integer.parseInt(ss[2]);
                }
                if (interval < 100 || interval > 5000) {
                    return "ERROR:interval must between 100 and 5000";
                }
                if (count < 1 || count > 60) {
                    return "ERROR:count must between 1 and 60";
                }

                ThreadPoolExecutor pool = BusinessPool.getBusinessPool(port);
                if (pool != null) {
                    HashMap map = new HashMap();
                    for (int i = count; i > 0; i--) {
                        HashMap poolmap = new HashMap();
                        poolmap.put("port", port);
                        poolmap.put("min", pool.getCorePoolSize());
                        poolmap.put("max", pool.getMaximumPoolSize());
                        poolmap.put("current", pool.getPoolSize());
                        poolmap.put("active", pool.getActiveCount());
                        poolmap.put("queue", pool.getQueue().size());
                        map.put("serverpool", poolmap);

                        if (i != 1) {
                            channel.writeAndFlush(JsonUtils.toJSONString(map));
                            channel.writeAndFlush(line);
                            try {
                                Thread.sleep(interval);
                            } catch (Exception e) {
                            }
                            continue;
                        } else {
                            break;
                        }
                    }
                    return JsonUtils.toJSONString(map);
                } else {
                    return "Not found listening port:" + port;
                }
            } catch (Exception e) {
                return "ERROR:" + ExceptionUtils.toShortString(e, 2);
            }
        }
    }
}