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
import java.util.ArrayList;
import java.util.List;

/**
 * Title: 监控到的异常数据<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFExceptionData implements MetricData, Serializable {


    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -7537490091539290524L;
    /**
     * The Interface id.
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
     * The Port.
     */
    private int port;

    /**
     * The Collect time.
     */
    private long collectTime;

    /**
     * The error times
     */
    private int times;


    /**
     * The Exception Detail list.
     */
    private List<ExceptionDetail> detailList = new ArrayList<ExceptionDetail>();

    /**
     * Instantiates a new Exception data.
     */
    public JSFExceptionData() {

    }

    /**
     * Instantiates a new Exception data.
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
    public JSFExceptionData(String interfaceId, String method, String providerIp, int port) {
        this.interfaceId = interfaceId;
        this.method = method;
        this.providerIp = providerIp;
        this.port = port;
    }

    /**
     * Add detail.
     *
     * @param detail
     *         the detail
     */
    public void addDetail(ExceptionDetail detail) {
        this.detailList.add(detail);
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
     * Sets interface iD.
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
     * Gets times.
     *
     * @return the times
     */
    public int getTimes() {
        return times;
    }

    /**
     * Sets times.
     *
     * @param times
     *         the times
     */
    public void setTimes(int times) {
        this.times = times;
    }

    /**
     * Gets detail list.
     *
     * @return the detail list
     */
    public List<ExceptionDetail> getDetailList() {
        return detailList;
    }

    /**
     * Sets detail list.
     *
     * @param detailList
     *         the detail list
     */
    public void setDetailList(List<ExceptionDetail> detailList) {
        this.detailList = detailList;
    }

    @Override
    public String toString() {
        return "ExceptionData{" +
                "interfaceId='" + interfaceId + '\'' +
                ", method='" + method + '\'' +
                ", providerIp='" + providerIp + '\'' +
                ", collectTime=" + collectTime +
                ", port=" + port +
                ", times=" + times +
                '}';
    }
}