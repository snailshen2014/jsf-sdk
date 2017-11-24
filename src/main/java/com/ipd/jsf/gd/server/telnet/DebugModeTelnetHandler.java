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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.logger.JSFLogger;
import io.netty.channel.Channel;

/**
 * Title: 调试模式日志输出<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class DebugModeTelnetHandler implements TelnetHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DebugModeTelnetHandler.class);

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Switch of debug mode (default is false)." + line +
                "Usage:\tdebug on\t\tOpen debug mode" + line +
                "\tdebug off\t\tClose debug mode" + line;
    }

    @Override
    public String telnet(Channel channel, String message) {
        if (message == null || message.length() == 0) {
            return getDescription();
        }
        if (message.equalsIgnoreCase("on")) {
            if (JSFLogger.print) {
                return "Debug mode already opened";
            } else {
                LOGGER.info("Debug mode is opened by telnet");
                JSFLogger.print = true;
                return "Debug mode change to open";
            }
        } else if (message.equalsIgnoreCase("off")) {
            if (JSFLogger.print) {
                JSFLogger.print = false;
                LOGGER.info("Debug mode is closed by telnet");
                return "Debug mode change to close";
            } else {
                return "Debug mode already closed";
            }
        } else {
            return getDescription();
        }
    }
}