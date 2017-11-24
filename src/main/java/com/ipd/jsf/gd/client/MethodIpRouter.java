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
package com.ipd.jsf.gd.client;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.JSFContext;

/**
 * Title: 方法级的IP路由<br>
 * <p/>
 * Description: 格式参考："echoStr.ip==10.12.113.111":"192.168.209.*"<br>
 * <p/>
 */
public class MethodIpRouter extends ParameterizedRouter {

    private final String methodName;

    public MethodIpRouter(String routerKey, String routerRule) {
        super(routerKey, routerRule);
        String param = rule.getLeft();
        int idx = param.indexOf(".ip");
        methodName = param.substring(0, idx);
    }

    @Override
    public boolean matchRule(ParameterizedRule rule, Invocation invocation) {
        if (!invocation.getMethodName().equals(methodName)) {
            return false; // 不是本方法的干掉
        }
        return matchString(rule, JSFContext.getLocalHost());
    }
}