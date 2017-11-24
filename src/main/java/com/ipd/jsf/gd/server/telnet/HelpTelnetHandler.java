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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class HelpTelnetHandler implements TelnetHandler {

    private Map<String, TelnetHandler> supportedCmds = new ConcurrentHashMap<String, TelnetHandler>();

    public void setSupportedCmds(Map<String, TelnetHandler> supportedCmds) {
        this.supportedCmds = supportedCmds;
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String telnet(Channel channel, String message) {
        StringBuffer result = new StringBuffer();
        if (StringUtils.isNotBlank(message)) {
            TelnetHandler handler = supportedCmds.get(message);
            if (handler != null) {
                result.append(handler.getCommand()).append(line)
                        .append(handler.getDescription()).append(line);
            } else {
                result.append("Not found command : " + message);
            }
        } else {
            result.append("The supported command include:").append(line);
            for (Entry<String, TelnetHandler> entry : supportedCmds.entrySet()) {
                result.append(entry.getKey()).append(" ");
                //result.append(entry.getKey() + "\t : " + entry.getValue().getDescription() + "\r\n");
            }
            result.append(line);
        }
        return result.toString();
    }

    @Override
    public String getDescription() {
        return "show all commands infomation!" + line + "Usage:\thelp" + line + "\thelp [cmd]";
    }

}