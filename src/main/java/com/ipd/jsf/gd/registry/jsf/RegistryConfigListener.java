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
package com.ipd.jsf.gd.registry.jsf;

import java.util.HashMap;
import java.util.Map;

import com.ipd.jsf.gd.client.GroupRouterFactory;
import com.ipd.jsf.gd.compress.CompressUtil;
import com.ipd.jsf.gd.filter.limiter.LimiterFactory;
import com.ipd.jsf.gd.filter.mock.MockDataFactroy;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.registry.ConfigListener;
import com.ipd.jsf.gd.server.ServerAuthHelper;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 注册中心配置监听器<br>
 * <p/>
 * Description: 解析注册中心的配置变化<br>
 * <p/>
 */
public class RegistryConfigListener implements ConfigListener {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistryConfigListener.class);

    @Override
    public void configChanged(Map newAttrs) {
        if (newAttrs == null) {
            return;
        }
        Map<String, String> attrs = new HashMap<String, String>(newAttrs);
        String interfaceId = attrs.remove(Constants.CONFIG_KEY_INTERFACE);
        LOGGER.trace("{} config changed, new attrs is {}", interfaceId, attrs);

        // 全局变量
        if (interfaceId == null || Constants.GLOBAL_SETTING.equals(interfaceId)) {
            changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_REGISTRY_HEARTBEAT_INTERVAL, "15000");
            changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_REGISTRY_CHECK_INTERVAL, "300000");

            changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_MONITOR_SEND_INTERVAL, "60000");
            if (changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_MONITOR_GLOBAL_OPEN, "false")) {
                MonitorFactory.invalidateCache(Constants.GLOBAL_SETTING);
            }

            //changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_SERVER_SUDO_PASSWD);
            //changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_SERVER_SUDO_WHITELIST);

            if (changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_INVOKE_CP_OPEN, "true")) {
                CompressUtil.compressOpen = CommonUtils.isTrue(
                        JSFContext.getGlobalVal(Constants.SETTING_INVOKE_CP_OPEN, "true"));
            }
            if (changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_INVOKE_CP_SIZE, "2048")) {
                CompressUtil.compressThresholdSize = CommonUtils.parseInt(
                        JSFContext.getGlobalVal(Constants.SETTING_INVOKE_CP_SIZE, null), 2048);
            }
            //changeVal(Constants.GLOBAL_SETTING, attrs, Constants.SETTING_REGISTRY_BACKUP_DIR);

            // 其它属性（不需要判断变化，也不要默认值的属性）
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                JSFContext.putInterfaceVal(Constants.GLOBAL_SETTING, entry.getKey(), entry.getValue());
            }

            // 备份到文件
            JSFRegistryHelper.writeGlobalConfigToFile();
        } else { // 接口级变量
            boolean mntrb1 = changeVal(interfaceId, attrs, Constants.SETTING_MONITOR_OPEN);
            boolean mntrb2 = changeVal(interfaceId, attrs, Constants.SETTING_MONITOR_WHITELIST);
            boolean mntrb3 = changeVal(interfaceId, attrs, Constants.SETTING_MONITOR_SLICE_INTERVAL);
            if (mntrb1 || mntrb2 || mntrb3) {
                MonitorFactory.invalidateCache(interfaceId); // 监控是否开启缓存失效
            }
            if (changeVal(interfaceId, attrs, Constants.SETTING_MONITOR_ELASPED_METRIC)) {
                MonitorFactory.invalidateMonitor(interfaceId, MonitorFactory.MONITOR_CONSUMER_ELAPSED); // 监控重建
            }

            boolean ivkb1 = changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_WB_OPEN, "true");
            boolean ivkb2 = changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_BLACKLIST, "");
            boolean ivkb3 = changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_WHITELIST, "{\"*\":\"*\"}");
            if (ivkb1 || ivkb2 || ivkb3) {
                ServerAuthHelper.invalidateCache(interfaceId); // 服务黑白名单缓存失效
            }

            if (changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_MOCKRESULT, null)) {
                MockDataFactroy.updateCache(interfaceId); // 注册中心下发的服务模拟调用结果更新
            }

            if (changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_APPLIMIT, null)) {
                LimiterFactory.updateCache(interfaceId); // 注册中心下发的app限制更新
            }
            if (changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_PROVIDER_LIMIT, null)) {
                LimiterFactory.updateProviderLimitCache(interfaceId); // 注册中心下发的provider限制更新
            }
            if (changeVal(interfaceId, attrs, Constants.SETTING_MAP_PARAM_ALIAS)) {
                GroupRouterFactory.updateCache(interfaceId);
            }

            //changeVal(interfaceId, attrs, Constants.SETTING_INVOKE_TOKEN);

            //changeVal(interfaceId, attrs, Constants.SETTING_ROUTER_OPEN);
            //changeVal(interfaceId, attrs, Constants.SETTING_ROUTER_RULE);

            // 其它属性（不需要判断变化）
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                JSFContext.putInterfaceVal(interfaceId, entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void providerAttrUpdated(Map newValue) {

    }

    @Override
    public void consumerAttrUpdated(Map newValue) {

    }

    private boolean changeVal(String interfaceId, Map<String, String> map, String key) {
        return changeVal(interfaceId, map, key, null);
    }

    private boolean changeVal(String interfaceId, Map<String, String> map, String key, String defaultVal) {
        try {
            if (!map.containsKey(key)) {
                return false;
            }
            String newval = map.get(key);
            if (newval == null) {
                newval = defaultVal;
            }
            // 比较变化
            String oldval = JSFContext.getInterfaceVal(interfaceId, key, null);
            if (oldval != null) { // 旧的有值
                if (!oldval.equals(newval)) { // 新旧不通
                    JSFContext.putInterfaceVal(interfaceId, key, newval);
                    return true;
                } else {
                    // 相同不管
                }
            } else { // 旧的没值
                if (newval != null) { // 新的有值
                    JSFContext.putInterfaceVal(interfaceId, key, newval);
                    return true;
                } else {
                    // 新旧都是null不管
                }
            }
            return false;
        } finally {
            map.remove(key);
        }

    }
}