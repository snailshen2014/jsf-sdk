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

import java.io.Serializable;
import java.util.Map;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JsfUrl implements Serializable {
    private static final long serialVersionUID = 7213436245556457630L;
    /* 实例ip */
    private String ip;
    /* 实例端口 */
    private int port;
    /* 实例pid */
    private int pid;
    /* 接口描述 */
    private String iface;
    /* 服务别名 */
    private String alias;
    /* protocol为0-consumer,或1-jsf,2-rest,3-dubbo */
    private int protocol;
    /* string dubboVersion ,string safVersion, string language, string appPath, i32 weight */
    private Map<String, String> attrs;
    /* 超时时间 */
    private int timeout;
    /* 随机端口, true-随机端口   false-固定端口*/
    private boolean random;
    /* startTime */
    private long stTime;
    /* 实例key 由注册中心生成 */
    private String insKey;
    /* 版本号，订阅时比对 */
    private long dataVersion;
    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    /**
     * @return the iface
     */
    public String getIface() {
        return iface;
    }
    /**
     * @param iface the iface to set
     */
    public void setIface(String iface) {
        this.iface = iface;
    }
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * @return the protocol
     */
    public int getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }
    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }
    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
    /**
     * @return the attrs
     */
    public Map<String, String> getAttrs() {
        return attrs;
    }
    /**
     * @param attrs the attrs to set
     */
    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }
    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }
    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    /**
     * @return the random
     */
    public boolean isRandom() {
        return random;
    }
    /**
     * @param random the random to set
     */
    public void setRandom(boolean random) {
        this.random = random;
    }
    /**
     * @return the stTime
     */
    public long getStTime() {
        return stTime;
    }
    /**
     * @param stTime the stTime to set
     */
    public void setStTime(long stTime) {
        this.stTime = stTime;
    }
    /**
     * @return the insKey
     */
    public String getInsKey() {
        return insKey;
    }
    /**
     * @param insKey the insKey to set
     */
    public void setInsKey(String insKey) {
        this.insKey = insKey;
    }
    /**
     * @return the dataVersion
     */
    public long getDataVersion() {
        return dataVersion;
    }
    /**
     * @param dataVersion the dataVersion to set
     */
    public void setDataVersion(long dataVersion) {
        this.dataVersion = dataVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JsfUrl(");
        sb.append("ip:").append(this.ip);
        sb.append(",port:").append(this.port);
        sb.append(",pid:").append(this.pid);
        sb.append(",interface:").append(this.iface);
        sb.append(",alias:").append(this.alias);
        sb.append(",protocol:").append(this.protocol);
        sb.append(",timeout:").append(this.timeout);
        sb.append(",random:").append(this.random);
        sb.append(",startTime:").append(this.stTime);
        sb.append(",insKey:").append(this.insKey);
        sb.append(",dataversion:").append(this.dataVersion);
        if (this.attrs != null) {
            sb.append(",attrs:").append(this.attrs.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsfUrl)) return false;

        JsfUrl jsfUrl = (JsfUrl) o;

        if (pid != jsfUrl.pid) return false;
        if (port != jsfUrl.port) return false;
        if (protocol != jsfUrl.protocol) return false;
        if (random != jsfUrl.random) return false;
        if (stTime != jsfUrl.stTime) return false;
        if (timeout != jsfUrl.timeout) return false;
        if (alias != null ? !alias.equals(jsfUrl.alias) : jsfUrl.alias != null) return false;
        if (iface != null ? !iface.equals(jsfUrl.iface) : jsfUrl.iface != null) return false;
        if (insKey != null ? !insKey.equals(jsfUrl.insKey) : jsfUrl.insKey != null) return false;
        if (ip != null ? !ip.equals(jsfUrl.ip) : jsfUrl.ip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + pid;
        result = 31 * result + (iface != null ? iface.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + protocol;
        result = 31 * result + timeout;
        result = 31 * result + (random ? 1 : 0);
        result = 31 * result + (int) (stTime ^ (stTime >>> 32));
        result = 31 * result + (insKey != null ? insKey.hashCode() : 0);
        return result;
    }
}