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

import java.util.LinkedHashMap;
import java.util.Map;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * Title: 获取配置<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ConfigTelnetHandler implements TelnetHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigTelnetHandler.class);

    @Override
    public String getCommand() {
        return "conf";
    }

    @Override
    public String getDescription() {
        return "Show config details." + line +
                "Usage:\tconf -p\tShow provider configs" + line +
                "\tconf -c\t\tShow consumer configs" + line +
                "\tconf -r\t\tShow jsf context" + line +
                "\tconf -s\t\tShow all interface settings" + line +
                "\tconf -g\t\tShow global settings" + line +
                "\tconf -i\t\tShow interface id mapping" + line +
                "\tconf -a\t\tShow all above" + line;
    }

    @Override
    public String telnet(Channel channel, String message) {
        if (message == null || message.length() == 0) {
            return getDescription();
        }
        if (message.equals("-p")) {
            return JsonUtils.toJSONString(JSFContext.getProviderConfigs());
        } else if (message.equals("-c")) {
            return JsonUtils.toJSONString(JSFContext.getConsumerConfigs());
        } else if (message.equals("-g")) {
            return JsonUtils.toJSONString(JSFContext.getConfigMap(Constants.GLOBAL_SETTING));
        } else if (message.equals("-r")) {
            return JsonUtils.toJSONString(JSFContext.getContext());
        } else if (message.equals("-s")) {
            return JsonUtils.toJSONString(JSFContext.getConfigMaps());
        } else if (message.equals("-i")) {
            return JsonUtils.toJSONString(JSFContext.getClassNameIfaceIdMap());
        } else if (message.equals("-a")) {
            Map<String, Object> tmpmap = new LinkedHashMap<String, Object>();
            tmpmap.put("context", JSFContext.getContext());
            tmpmap.put("configs", JSFContext.getConfigMaps());
            tmpmap.put("providers", JSFContext.getProviderConfigs());
            tmpmap.put("consumers", JSFContext.getConsumerConfigs());
            tmpmap.put("ifaceIds", JSFContext.getClassNameIfaceIdMap());
            return JsonUtils.toJSONString(tmpmap);
        } else {
            return getDescription();
        }
    }
}