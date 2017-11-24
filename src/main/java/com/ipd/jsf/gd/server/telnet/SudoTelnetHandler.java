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
import com.ipd.jsf.gd.util.CryptUtils;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.server.TelnetChannelHandler;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class SudoTelnetHandler implements TelnetHandler {
    @Override
    public String getCommand() {
        return "sudo";
    }

    @Override
    public String getDescription() {
        return "Open some superuser's function. " + line +
                "Usage:\tsudo PASSWORD." + line;
    }

    @Override
    public String telnet(Channel channel, String message) {
        if (StringUtils.isBlank(message)) {
            return "please input password after sudo";
        } else if ("?".equals(message) || "/?".equals(message)) { // 显示帮助
            return getDescription();
        } else { // 显示接口下信息
            try {
                String globalPasswd = JSFContext.getGlobalVal(Constants.SETTING_SERVER_SUDO_PASSWD, null);
                if (message.equals(CryptUtils.decrypt(globalPasswd))) {
                    TelnetChannelHandler.ALLOW_INVOKE_CHANNELS.add(channel);
                    return "Success, function has been open";
                } else {
                    // 增加密码重试限制？
                    return "Failure, wrong password!";
                }
            } catch (Exception e) {
                return ExceptionUtils.toString(e);
            }
        }
    }
}