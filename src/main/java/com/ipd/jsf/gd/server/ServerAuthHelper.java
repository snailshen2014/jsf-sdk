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
package com.ipd.jsf.gd.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public final class ServerAuthHelper {

    /**
     * 接口/IP的二级缓存
     */
    private static Map<String, Map<String, Boolean>> cache = new ConcurrentHashMap<String, Map<String, Boolean>>();

    /**
     * 判断时候合法请求
     *
     * @param interfaceId
     *         接口
     * @param alias
     *         the alias
     * @param remoteIp
     *         远程地址
     * @return boolean
     */
    public static boolean isValid(String interfaceId, String alias, String remoteIp) {
        String key = buildkey(interfaceId, alias);
        Map<String, Boolean> itfcache = cache.get(key);
        if (itfcache == null) {
            itfcache = new ConcurrentHashMap<String, Boolean>();
            cache.put(key, itfcache);
        }
        Boolean bol = itfcache.get(remoteIp); // 缓存中获取
        if (bol == null) {
            String open = JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_INVOKE_WB_OPEN, "true");
            if (CommonUtils.isTrue(open)) {
                String itfwhitelist = JSFContext.getInterfaceVal(interfaceId,
                        Constants.SETTING_INVOKE_WHITELIST, "{\"*\":\"*\"}");
                String whitelist = getAliasList(itfwhitelist, alias, "*"); // 白名单默认为*
                // 白名单为空或者为* 或者在白名单中
                boolean inwhite = "*".equals(whitelist)
                        || StringUtils.isBlank(whitelist)
                        || NetUtils.isMatchIPByPattern(whitelist, remoteIp);
                if (inwhite) {
                    String itfblacklist = JSFContext.getInterfaceVal(interfaceId,
                            Constants.SETTING_INVOKE_BLACKLIST, "");
                    String blacklist = getAliasList(itfblacklist, alias, StringUtils.EMPTY); // 黑名单默认为 空
                    // 在白名单且不在黑名单
                    bol = !NetUtils.isMatchIPByPattern(blacklist, remoteIp);
                } else { // 不在白名单
                    bol = Boolean.FALSE;
                }
            } else { // 未开启黑白名单功能，默认true
                bol = Boolean.TRUE;
            }
            // 放入缓存
            itfcache.put(remoteIp, bol);
        }
        return bol.booleanValue();
    }

    /**
     * 得到接口下组级别的配置
     *
     * @param bwlist
     *         接口的黑名单或者白名单，是json字符串
     * @param alias
     *         服务别名
     * @param defaultVal
     *         the default val
     * @return 服务别名级的配置 alias list
     */
    private static String getAliasList(String bwlist, String alias, String defaultVal) {
        if (StringUtils.isBlank(bwlist)) {
            return defaultVal; // 为空直接返回默认值
        }
        try {
            Map map = JsonUtils.parseObject(bwlist, Map.class);
            String subbwlist = (String) map.get(alias);
            if (subbwlist == null) { // 组没有
                subbwlist = (String) map.get("*");
                if (subbwlist == null) { // 全局没有
                    subbwlist = defaultVal;
                }
            }
            return subbwlist;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /**
     * 增加Interface缓存
     *
     * @param interfaceId
     *         the interface id
     * @param alias
     *         the alias
     * @return the map
     */
    public static Map<String, Boolean> addInterface(String interfaceId, String alias) {
        String key = buildkey(interfaceId, alias);
        Map<String, Boolean> itfcache = cache.get(key);
        if (itfcache == null) {
            itfcache = new ConcurrentHashMap<String, Boolean>();
            cache.put(key, itfcache);
        }
        return itfcache;
    }

    /**
     * 清空缓存
     *
     * @param interfaceId
     *         the interface id
     * @param alias
     *         the alias
     */
    public static void delInterface(String interfaceId, String alias) {
        String key = buildkey(interfaceId, alias);
        cache.remove(key);
    }

    /**
     * Buildkey string.
     *
     * @param interfaceId
     *         the interface id
     * @param alias
     *         the alias
     * @return the string
     */
    private static String buildkey(String interfaceId, String alias) {
        return interfaceId + "::" + alias;
    }

    /**
     * 清空缓存
     *
     * @param interfaceId
     *         the interface id
     */
    public static void invalidateCache(String interfaceId) {
        Map<String, Boolean> itfcache = cache.get(interfaceId);
        if (itfcache == null) {
            itfcache = new ConcurrentHashMap<String, Boolean>();
            cache.put(interfaceId, itfcache);
        } else {
            itfcache.clear();
        }
    }
}