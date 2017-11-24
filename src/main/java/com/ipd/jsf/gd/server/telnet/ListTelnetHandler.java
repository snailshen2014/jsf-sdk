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

import java.lang.reflect.Method;
import java.util.List;

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;

/**
 * 显示当前进程所有服务端接口
 */
public class ListTelnetHandler implements TelnetHandler {

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#getCommand()
     */
    @Override
    public String getCommand() {
        return "ls";
    }

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#getDescription()
     */
    @Override
    public String getDescription() {
        return "Display all service interface and included methods. " + line +
                "Usage:\tls \t\t\tshow interfaces." + line +
                "\tls -l\t\t\tshow interfaces detail." + line +
                "\tls com.ipd.XXX\t\tshow methods." + line +
                "\tls -l com.ipd.XXX\tshow methods detail." + line;
    }

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(message)) {// 普通的显示全部接口+alias
            //for (Map.Entry<String, Invoker> entry : BaseServerHandler.getAllInvoker().entrySet()) {
            //    String key = entry.getKey();
            //   sb.append(key).append(line);
            //}
            List<ProviderConfig> providerConfigs = JSFContext.getProviderConfigs();
            for (ProviderConfig config : providerConfigs) {
                sb.append(config.getInterfaceId()).append(line);
            }
        } else if ("?".equals(message) || "/?".equals(message)) { // 显示帮助
            return getDescription();
        } else if ("-l".equals(message)) { // 显示详细alias等其它参数？
            List<ProviderConfig> providerConfigs = JSFContext.getProviderConfigs();
            for (ProviderConfig config : providerConfigs) {
                List<String> urls = config.buildUrls();
                if (urls != null) {
                    for (String url : urls) {
                        sb.append(config.getInterfaceId()).append(" -> ").append(url).append(line);
                    }
                }
            }
        } else { // 显示接口下信息
            if (message.startsWith("-l")) {
                String interfaceId = message.substring(2).trim();
                try {
                    Class clazz = ClassLoaderUtils.forName(interfaceId);
                    sb.append(clazz.getCanonicalName()).append(line);
                    Method ms[] = clazz.getMethods();
                    for (Method m : ms) {
                        sb.append(m.getReturnType().getCanonicalName()).append(" ").append(m.getName()).append("(");
                        Class[] params = m.getParameterTypes();
                        for (Class p : params) {
                            sb.append(p.getCanonicalName()).append(", ");
                        }
                        if (params.length > 0) {
                            sb.delete(sb.length() - 2, sb.length());
                        }
                        sb.append(")").append(line);
                    }
                } catch (ClassNotFoundException e) {
                    return "interface not found:" + interfaceId;
                }
            } else {
                String interfaceId = message.trim();
                try {
                    Class clazz = ClassLoaderUtils.forName(interfaceId);
                    sb.append(clazz.getCanonicalName()).append(line);
                    Method ms[] = clazz.getMethods();
                    for (Method m : ms) {
                        sb.append(m.getName()).append(line);
                    }
                } catch (ClassNotFoundException e) {
                    return "interface not found:" + interfaceId;
                }
            }
        }
        return sb.toString();
    }

}