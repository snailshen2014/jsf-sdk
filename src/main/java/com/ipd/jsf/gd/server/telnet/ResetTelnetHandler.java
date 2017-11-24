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

import com.ipd.jsf.gd.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * Title: 重置接口<br>
 * <p/>
 * Description: 重置定时任务等<br>
 * <p/>
 */
public class ResetTelnetHandler implements TelnetHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ResetTelnetHandler.class);

    @Override
    public String getCommand() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Reset some status" + line +
                "Usage:\treset scheduled\t\tRest scheduled service" + line;
    }

    @Override
    public String telnet(Channel channel, String message) {
        if (message == null || message.length() == 0) {
            return getDescription();
        }
        if (message.equalsIgnoreCase("scheduled")) {
            ScheduledService.reset();
            return "Reset scheduled operation has been send to server.";
        } else {
            return getDescription();
        }
    }
}