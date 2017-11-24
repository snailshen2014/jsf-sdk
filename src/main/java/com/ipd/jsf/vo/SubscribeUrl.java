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
package com.ipd.jsf.vo;

import java.util.List;
import java.util.Map;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class SubscribeUrl {

    public static final int CHECK_RUOK = 0; // 检查是否正常的空事件，正常就返回imok

    public static final int PROVIDER_ADD = 1; // 服务列表增加，增量变化
    public static final int PROVIDER_DEL = 2; // 服务列表删除，增量变化（为了安全，删光服务列表的操作将被忽略）
    public static final int PROVIDER_UPDATE_ALL = 3; // 服务列表变化，全量变化（为了安全，删光服务列表的操作将被忽略）
    public static final int PROVIDER_CLEAR = 4; // 黑白名单时使用，清空服务列表
    public static final int PROVIDER_FORCE_DEL = 5; // 服务列表删除，增量变化（不会判断是否被删光）
    public static final int PROVIDER_FORCE_UPDATE_ALL = 6; // 服务列表删除，增量变化（不会判断是否被删光）

//    public static final int CONFIG_ADD = 11;
//    public static final int CONFIG_DEL = 12;
    public static final int CONFIG_UPDATE = 13; // 配置更新 接口级

    public static final int ATTRIBUTE_P_UPDATE = 20; // provider属性更新（例如alias，timeout）
    public static final int ATTRIBUTE_C_UPDATE = 21; // consumer属性更新（例如alias，timeout）
    public static final int ATTRIBUTE_S_UPDATE = 22; // server属性更新（例如coreSize，maxSize）

    public static final int CONSUMER_CONFIG = 31; // 查询订阅者配置
    public static final int CONSUMER_PROVIDERLIST = 32; // 查询订阅者服务列表
    public static final int CONSUMER_CONCURRENT = 35; // 查询调用者的线程并发信息
    public static final int PROVIDER_CONFIG = 33; // 查询发布者配置
    public static final int PROVIDER_CONCURRENT = 34; // 查询发布者的线程并发信息
    public static final int PROVIDER_INTERFACE = 36; // 查询发布者的接口描述
    public static final int SERVER_STATUS = 37; // 查询Server端口的配置

    public static final int INSTANCE_CONFIG = 41; // 查询实例级配置（包括provider和consumer、registry等）
    public static final int INSTANCE_RECOVER = 42; // 强制重新订阅（包括provider和consumer等）
    public static final int INSTANCE_CONCURRENT = 43; // 查询实例的线程池+future并发信息
    public static final int INSTANCE_RECONNECT = 44; // 强制重连其它注册中心并强制重新订阅（包括provider和consumer等）
    public static final int INSTANCE_CONFIG_UPDATE = 45; // 实例级配置更新
    public static final int INSTANCE_RESET_SCHEDULED = 46; // 强制重置定时任务，包括定时心跳，发送监控数据等
    public static final int INSTANCE_NOTIFICATION = 47; // 自定义通知，可设置日志打印级别

    public static final int SWITCH_APP_LIMIT = 61; // 配置开关 app调用限制
    public static final int SWITCH_MOCK_RESULT = 62; // mock设结果设置开关

    /**
     * 事件类型 参考上面的常量
     */
    private int type;

    private JsfUrl sourceUrl;

    private List<JsfUrl> providerList;

    private Map<String, String> config;

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the sourceUrl
     */
    public JsfUrl getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @param sourceUrl the sourceUrl to set
     */
    public void setSourceUrl(JsfUrl sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return the providerList
     */
    public List<JsfUrl> getProviderList() {
        return providerList;
    }

    /**
     * @param providerList the providerList to set
     */
    public void setProviderList(List<JsfUrl> providerList) {
        this.providerList = providerList;
    }

    /**
     * @return the config
     */
    public Map<String, String> getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("subscribeUrl(");
        sb.append("type:").append(this.type);
        if (this.sourceUrl != null) {
            sb.append(",sourceUrl:").append(this.sourceUrl.toString());
        }
        sb.append(",providerList:");
        if (providerList == null) {
            sb.append("null");
        } else {
            sb.append("{");
            for (JsfUrl jsfUrl : providerList) {
                sb.append(jsfUrl.toString());
            }
            sb.append("}");
        }
        if (this.config != null) {
            sb.append(",config:").append(this.config);
        }
        sb.append(")");
        return sb.toString();
    }
}