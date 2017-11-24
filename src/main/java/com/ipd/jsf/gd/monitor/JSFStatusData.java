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
package com.ipd.jsf.gd.monitor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Title: 状态数据<br>
 * <p/>
 * Description: 主要是客户端收集调用的所有provider的数据<br>
 * <p/>
 */
public class JSFStatusData implements MetricData, Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -9031818316986485489L;

    /**
     * 接口
     */
    private String interfaceId;

    /**
     * 时间
     */
    private long collectTime;

    /**
     * The Remotes. 同一个服务端对应的全部调用者统计信息
     */
    private List<ProviderStat> providers = new CopyOnWriteArrayList<ProviderStat>();

    /**
     * Instantiates a new JSF status data.
     */
    public JSFStatusData() {

    }

    /**
     * Instantiates a new JSF status data.
     *
     * @param interfaceId          the interface id
     */
    public JSFStatusData(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets interface id.
     *
     * @return the interface id
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Sets interface id.
     *
     * @param interfaceId          the interface id
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets collect time.
     *
     * @return the collect time
     */
    public long getCollectTime() {
        return collectTime;
    }

    /**
     * Sets collect time.
     *
     * @param collectTime          the collect time
     */
    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    /**
     * Gets providers.
     *
     * @return the providers
     */
    public List<ProviderStat> getProviders() {
        return providers;
    }

    /**
     * Sets providers.
     *
     * @param providers  the providers
     */
    public void setProviders(List<ProviderStat> providers) {
        this.providers = providers;
    }

    /**
     * 是否有数据
     *
     * @return 有没有状态数据
     */
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Equals boolean.
     *
     * @param o          the o
     * @return the boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JSFStatusData)) return false;

        JSFStatusData that = (JSFStatusData) o;

        if (interfaceId != null ? !interfaceId.equals(that.interfaceId) : that.interfaceId != null) return false;

        return true;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = interfaceId != null ? interfaceId.hashCode() : 0;
        return result;
    }
}