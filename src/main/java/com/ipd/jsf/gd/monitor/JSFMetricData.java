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
 * Title: 监控到的指标数据<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFMetricData implements MetricData, Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 4239734823509041593L;

    /**
     * The Interface iD.
     */
    private String interfaceId;

    /**
     * The Method.
     */
    private String method;

    /**
     * The Provider ip.
     */
    private String providerIp;

    /**
     * The Collect time.
     */
    private long collectTime;

    /**
     * The Port.
     */
    private int port;

    /**
     * The Remotes. 同一个服务端对应的全部调用者统计信息
     */
    private List<RemoteStat> remotes = new CopyOnWriteArrayList<RemoteStat>();

    /**
     * Is empty.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return remotes.isEmpty();
    }

    /**
     * Add remote stat.
     *
     * @param remoteStat
     *         the remote stat
     */
    public void addRemoteStat(RemoteStat remoteStat) {
        remotes.add(remoteStat);
    }

    /**
     * Instantiates a new Metric data.
     */
    public JSFMetricData() {

    }

    /**
     * Instantiates a new Metric data.
     *
     * @param interfaceId
     *         the interface id
     * @param method
     *         the method
     * @param providerIp
     *         the provider ip
     * @param port
     *         the port
     */
    public JSFMetricData(String interfaceId, String method, String providerIp, int port) {
        this.interfaceId = interfaceId;
        this.method = method;
        this.providerIp = providerIp;
        this.port = port;
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
     * @param interfaceId
     *         the interface id
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }


    /**
     * Gets method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets method.
     *
     * @param method
     *         the method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets provider ip.
     *
     * @return the provider ip
     */
    public String getProviderIp() {
        return providerIp;
    }

    /**
     * Sets provider ip.
     *
     * @param providerIp
     *         the provider ip
     */
    public void setProviderIp(String providerIp) {
        this.providerIp = providerIp;
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
     * @param collectTime
     *         the collect time
     */
    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets port.
     *
     * @param port
     *         the port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Gets remotes.
     *
     * @return the remotes
     */
    public List<RemoteStat> getRemotes() {
        return remotes;
    }

    /**
     * Sets remotes.
     *
     * @param remotes
     *         the remotes
     */
    public void setRemotes(List<RemoteStat> remotes) {
        this.remotes = remotes;
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "interfaceId='" + interfaceId + '\'' +
                ", method='" + method + '\'' +
                ", providerIp='" + providerIp + '\'' +
                ", collectTime=" + collectTime +
                ", port=" + port +
                '}';
    }
}