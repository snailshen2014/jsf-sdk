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

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.util.TwoLevelMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: 组路由，需要实现方法，选择一个组<br>
 *
 * Description: <br>
 */
public class GroupRouterFactory {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(GroupRouterFactory.class);

    /**
     * 配置信息 interface：{method: {param:alias}}
     */
    private static final TwoLevelMap<String, Map> paramAliasMap
            = new TwoLevelMap<String, Map>();

    /**
     * 更新接口下的 机房-分组映射关系缓存
     *
     * @param interfaceId
     *         接口名称
     */
    public static synchronized void updateCache(String interfaceId) {
        String ss = JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_MAP_PARAM_ALIAS, null);
        try {
            if (StringUtils.isNotEmpty(ss)) { // 有配置
                Map<String, String> config = JsonUtils.parseObject(ss, Map.class);
                Map<String, Map> methodCfg = new HashMap<String, Map>();
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    String methodAndParam = entry.getKey();
                    int split = methodAndParam.indexOf(":");
                    if (split > 0) {
                        String method = methodAndParam.substring(0, split);
                        String param = methodAndParam.substring(split + 1);
                        Map tmp = methodCfg.get(method);
                        if (tmp == null) {
                            tmp = new HashMap();
                            methodCfg.put(method, tmp);
                        }
                        tmp.put(param, entry.getValue());
                    }
                }
                paramAliasMap.put(interfaceId, methodCfg); // 覆盖更新
                LOGGER.info("Add param alias map of {} from cache : {}", interfaceId,
                        paramAliasMap.get(interfaceId));
            } else { // 配置为空
                if (paramAliasMap.remove(interfaceId) != null) {
                    LOGGER.info("Remove param alias map of {} from cache", interfaceId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update group router of " + interfaceId + ", the json is " + ss);
        }
    }

    /**
     * 根据接口 + 目标参数 获取实际的分组列表
     *
     * @param interfaceId
     *         接口
     * @param methodName
     *         方法
     * @param dstParamValue
     *         目标参数值
     * @return 实际的分组列表
     */
    protected static String getAliasesByParam(String interfaceId, String methodName, String dstParamValue) {
        Map map = paramAliasMap.getOrDefault(interfaceId, methodName, "*");
        return map != null ? StringUtils.toString(map.get(dstParamValue)) : null;
    }
}