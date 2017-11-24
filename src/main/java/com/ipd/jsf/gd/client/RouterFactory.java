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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class RouterFactory {

    /**
     * 根据一串json规则构造列表
     *
     * @param routerRuleJson
     *         多条规则
     * @return List<Router>
     */
    public static List<Router> buildRouters(String routerRuleJson) {
        Map<String, String> map = JsonUtils.parseObject(routerRuleJson, Map.class);
        if (CommonUtils.isNotEmpty(map)) {
            List<Router> routers = new ArrayList<Router>(map.size());
            for (Map.Entry<String, String> rule : map.entrySet()) {
                // key是 method==sayHello value是 10.12.113.111
                Router router = buildRouter(rule.getKey(), rule.getValue());
                routers.add(router);
            }
            return routers;
        }
        return null;
    }

    /**
     * 构造路由器
     *
     * @param routerKey
     *         关键字：method==sayHello
     * @param routerRule
     *         路由地址：10.12.113.11*
     * @return Router
     */
    public static Router buildRouter(String routerKey, String routerRule) {
        Router router;
        if (routerKey.contains(".arg")) { // 方法参数路由
            router = new MethodParameterRouter(routerKey, routerRule);
        } else if (routerKey.contains(".ip")) { // 方法ip路由
            router = new MethodIpRouter(routerKey, routerRule);
        } else if (routerKey.startsWith("method")) { // 方法名路由
            router = new MethodNameRouter(routerKey, routerRule);
        } else {
            throw new InitErrorException("[JSF-21600]Illegal route rule [" + routerKey + " : " + routerRule + "]");
        }
        return router;
    }
}