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

/**
 * Title: 按方法名进行路由<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class MethodNameRouter extends ParameterizedRouter {

    public MethodNameRouter(String routerKey, String routerRule) {
        super(routerKey, routerRule);
    }

    public boolean matchRule(ParameterizedRule rule, Invocation invocation) {
        // 匹配方法
        return matchString(rule, invocation.getMethodName());
    }
}