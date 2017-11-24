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
 * Title: 客户端收集的服务端状态数据<br>
 * <p/>
 * Description: 包含服务端地址，异常等信息<br>
 * <p/>
 */
public class ProviderStat implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -8720281203254574400L;

    /**
     * 服务端地址
     */
    private String providerIp;

    /**
     * 服务端端口
     */
    private int port;

    /**
     * 客户端地址
     */
    private String consumerIp;

    /**
     * 异常类型
     */
    private String exception;

    /**
     * 异常数量
     */
    private int num;

    /**
     * Instantiates a new Provider stat.
     */
    public ProviderStat() {

    }

    /**
     * Instantiates a new Provider stat.
     *
     * @param providerIp
     *         the provider ip
     * @param port
     *         the port
     * @param consumerIp
     *         the consumer ip
     */
    public ProviderStat(String providerIp, int port, String consumerIp) {
        this.providerIp = providerIp;
        this.port = port;
        this.consumerIp = consumerIp;
    }

    /**
     * Instantiates a new Provider stat.
     *
     * @param providerIp
     *         the provider ip
     * @param port
     *         the port
     * @param consumerIp
     *         the consumer ip
     * @param exception
     *         the exception
     */
    public ProviderStat(String providerIp, int port, String consumerIp, String exception) {
        this.providerIp = providerIp;
        this.port = port;
        this.consumerIp = consumerIp;
        this.exception = exception;
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
     * @param consumerIp
     *         the consumer ip
     */
    public void setConsumerIp(String consumerIp) {
        this.consumerIp = consumerIp;
    }

    /**
     * Gets exception.
     *
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets exception.
     *
     * @param exception
     *         the exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Gets num.
     *
     * @return the num
     */
    public int getNum() {
        return num;
    }

    /**
     * Sets num.
     *
     * @param num
     *         the num
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Equals boolean.
     *
     * @param o
     *         the o
     * @return the boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderStat)) return false;

        ProviderStat that = (ProviderStat) o;

        if (port != that.port) return false;
        if (consumerIp != null ? !consumerIp.equals(that.consumerIp) : that.consumerIp != null) return false;
        if (exception != null ? !exception.equals(that.exception) : that.exception != null) return false;
        if (providerIp != null ? !providerIp.equals(that.providerIp) : that.providerIp != null) return false;

        return true;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = providerIp != null ? providerIp.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (consumerIp != null ? consumerIp.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        return result;
    }
}