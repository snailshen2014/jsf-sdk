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

import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.ExceptionUtils;
import io.netty.channel.Channel;

/**
 * Title: 各种检查命令<br>
 *
 * Description: <br>
 */
public class CheckTelnetHandler implements TelnetHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CheckTelnetHandler.class);

    @Override
    public String getCommand() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check the specified funcation. " + line +
                "Usage:\tcheck iface com.xxx.XxxService.xxxMethod 12345" + line;
    }

    @Override
    public String telnet(Channel channel, String message) {
        try {
            if (StringUtils.isEmpty(message)) {
                return getDescription();
            }
            int id = message.indexOf(" ");
            if (id >= 0) {
                String method = message.substring(0, id);
                if ("iface".equals(method)) {
                    String[] nameAndId = message.substring(id + 1).trim().replaceAll("\\s+", " ").split("\\s");
                    if (nameAndId.length == 2 && nameAndId[0] != null && nameAndId[1] != null) {
                        boolean m = nameAndId[1].equals(JSFContext.getIfaceIdByClassName(nameAndId[0]));
                        return m ? "1" : "0";
                    }
                }
            }
            return getDescription();
        } catch (Exception e) {
            return ExceptionUtils.toString(e);
        }
    }
}