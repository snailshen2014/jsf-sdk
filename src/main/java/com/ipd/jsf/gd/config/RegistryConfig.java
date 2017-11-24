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
package com.ipd.jsf.gd.config;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 注册中心配置<br>
 * <p/>
 * Description: 对应标签jsf:registry，对应一组注册中心<br>
 * <p/>
 */
public class RegistryConfig extends AbstractIdConfig implements Serializable {


    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -2508213661321690225L;

    /**
     * 协议
     */
    private String protocol = Constants.REGISTRY_PROTOCOL_JSF;

    /**
     * 地址, 和index必须填一个
     */
    private String address;

    /**
     * index服务地址, 和address必须填一个
     * 用于获取注册中心地址
     * 后期需要实现
     */
    private String index;

    /**
     * 是否注册，如果是false只订阅不注册
     */
    private boolean register = true;

    /**
     * 是否订阅服务
     */
    private boolean subscribe = true;

    /**
     * 调用注册中心超时时间
     */
    private int timeout = Constants.DEFAULT_CLIENT_INVOKE_TIMEOUT;

    /**
     * 连接注册中心超时时间
     */
    private int connectTimeout = Constants.DEFAULT_REGISTRY_CONNECT_TIMEOUT;

    /**
     * 保存到本地文件的位置，默认$HOME下
     */
    private String file;

    /**
     * 定时批量检查时的条目数
     */
    private int batchCheck = 10;

    /**
     * 自定义注册中心
     */
    private String implClass;

    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String> parameters;

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets address.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Is register.
     *
     * @return the boolean
     */
    public boolean isRegister() {
        return register;
    }

    /**
     * Sets register.
     *
     * @param register the register
     */
    public void setRegister(boolean register) {
        this.register = register;
    }

    /**
     * Is subscribe.
     *
     * @return the boolean
     */
    public boolean isSubscribe() {
        return subscribe;
    }

    /**
     * Sets subscribe.
     *
     * @param subscribe the subscribe
     */
    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Is file.
     *
     * @return the boolean
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets file.
     *
     * @param file the file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets index.
     *
     * @param index the index
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Gets batch check.
     *
     * @return the batch check
     */
    public int getBatchCheck() {
        return batchCheck;
    }

    /**
     * Sets batch check.
     *
     * @param batchCheck the batch check
     */
    public void setBatchCheck(int batchCheck) {
        this.batchCheck = batchCheck;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets parameter.
     *
     * @param key
     *         the key
     * @param value
     *         the value
     */
    public void setParameter(String key, String value) {
        if (parameters == null) {
            parameters = new ConcurrentHashMap<String, String>();
        }
        parameters.put(key, value);
    }

    /**
     * Gets parameter.
     *
     * @param key
     *         the key
     * @return the value
     */
    public String getParameter(String key) {
        return parameters == null ? null : parameters.get(key);
    }

    @Override
    public String toString() {
        return "RegistryConfig{" +
                "protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", index='" + index + '\'' +
                ", register=" + register +
                ", subscribe=" + subscribe +
                ", timeout=" + timeout +
                ", connectTimeout=" + connectTimeout +
                ", file='" + file + '\'' +
                ", batchCheck=" + batchCheck +
                ", implClass=" + implClass +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistryConfig)) return false;

        RegistryConfig config = (RegistryConfig) o;

        if (register != config.register) return false;
        if (subscribe != config.subscribe) return false;
        if (timeout != config.timeout) return false;
        if (connectTimeout != config.connectTimeout) return false;
        if (batchCheck != config.batchCheck) return false;
        if (protocol != null ? !protocol.equals(config.protocol) : config.protocol != null) return false;
        if (address != null ? !address.equals(config.address) : config.address != null) return false;
        if (index != null ? !index.equals(config.index) : config.index != null) return false;
        if (file != null ? !file.equals(config.file) : config.file != null) return false;
        if (implClass != null ? !implClass.equals(config.implClass) : config.implClass != null) return false;
        if (parameters != null ? !parameters.equals(config.parameters) : config.parameters != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = protocol != null ? protocol.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (index != null ? index.hashCode() : 0);
        result = 31 * result + (register ? 1 : 0);
        result = 31 * result + (subscribe ? 1 : 0);
        result = 31 * result + timeout;
        result = 31 * result + connectTimeout;
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (implClass != null ? implClass.hashCode() : 0);
        result = 31 * result + batchCheck;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}