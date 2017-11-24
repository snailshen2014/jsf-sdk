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

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFElapsedData implements Serializable, MetricData {
    private static final long serialVersionUID = -4142440857328218488L;

    private String interfaceId;

    private String methodName;

    private long collectTime;

    private String providerIp;

    private String consumerIp;

    private int elapsedMetricsId;

    private int[] distribution;

    /**
     * Instantiates a new JSF elapsed data.
     */
    public JSFElapsedData() {

    }

    /**
     * Instantiates a new JSF elapsed data.
     *
     * @param interfaceId the interface id
     * @param methodName the method name
     * @param providerIp the provider ip
     * @param consumerIp the consumer ip
     */
    public JSFElapsedData(String interfaceId, String methodName, String providerIp, String consumerIp) {
        this.interfaceId = interfaceId;
        this.methodName = methodName;
        this.providerIp = providerIp;
        this.consumerIp = consumerIp;
        this.collectTime = System.currentTimeMillis();
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
     * @param interfaceId the interface id
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
     * @param collectTime the collect time
     */
    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
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
     * @param providerIp the provider ip
     */
    public void setProviderIp(String providerIp) {
        this.providerIp = providerIp;
    }

    /**
     * Gets consumer ip.
     *
     * @return the consumer ip
     */
    public String getConsumerIp() {
        return consumerIp;
    }

    /**
     * Sets consumer ip.
     *
     * @param consumerIp the consumer ip
     */
    public void setConsumerIp(String consumerIp) {
        this.consumerIp = consumerIp;
    }

    /**
     * Gets elapsed metric id.
     *
     * @return the elapsed metric id
     */
    public int getElapsedMetricsId() {
        return elapsedMetricsId;
    }

    /**
     * Sets elapsed metric id.
     *
     * @param elapsedMetricsId the elapsed metric id
     */
    public void setElapsedMetricsId(int elapsedMetricsId) {
        this.elapsedMetricsId = elapsedMetricsId;
    }

    /**
     * Get distribution.
     *
     * @return the int [ ]
     */
    public int[] getDistribution() {
        return distribution;
    }

    /**
     * Sets distribution.
     *
     * @param distribution the distribution
     */
    public void setDistribution(int[] distribution) {
        this.distribution = distribution;
    }
}