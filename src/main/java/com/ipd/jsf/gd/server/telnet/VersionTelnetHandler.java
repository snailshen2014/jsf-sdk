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

import com.ipd.jsf.gd.util.Constants;
import io.netty.channel.Channel;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class VersionTelnetHandler implements TelnetHandler {
    @Override
    public String getCommand() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Show version of current JSF";
    }

    @Override
    public String telnet(Channel channel, String message) {
        return "{\"jsfVersion\":\"" + Constants.JSF_VERSION + "\"," +
                "\"buildVersion\":\"" + Constants.JSF_BUILD_VERSION + "\"," +
                "\"safVersion\":\"" + Constants.DEFAULT_SAF_VERSION + "\"}";
    }
}