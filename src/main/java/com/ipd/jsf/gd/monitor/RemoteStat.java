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
 * 客户端统计数据，一个远程地址一个
 */
public class RemoteStat implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7059916645499773009L;

    /**
     * The Remote ip.
     */
    private String remoteIp;

    /**
     * The Call times.
     */
    private int callTimes;

    /**
     * The Elapse.
     */
    private int elapse;

    /**
     * The In.
     */
    private int in;

    /**
     * The Out.
     */
    private int out;

    /**
     * The Protocol
     */
    private String protocol;

    /**
     * The Alias
     */
    private String alias;

    /**
     * The AppId
     */
    private String appId;

    /**
     * Instantiates a new Remote stat.
     */
    public RemoteStat() {
    }

    /**
     * Instantiates a new Remote stat.
     *
     * @param remoteIp
     *         the remote ip
     * @param protocol
     *         the protocol
     * @param appId
     *         the app id
     * @param alias
     *         the alias
     * @param values
     *         the values
     */
    public RemoteStat(String remoteIp, String protocol, String alias, String appId, Integer[] values) {
        this.remoteIp = remoteIp;
        this.protocol = protocol;
        this.appId = appId;
        this.alias = alias;
        callTimes = values[0];
        elapse = values[1];
        in = values[2];
        out = values[3];
    }

    /**
     * Gets remote ip.
     *
     * @return the remote ip
     */
    public String getRemoteIp() {
        return remoteIp;
    }

    /**
     * Sets remote ip.
     *
     * @param remoteIp
     *         the remote ip
     */
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    /**
     * Gets call times.
     *
     * @return the call times
     */
    public int getCallTimes() {
        return callTimes;
    }

    /**
     * Sets call times.
     *
     * @param callTimes
     *         the call times
     */
    public void setCallTimes(int callTimes) {
        this.callTimes = callTimes;
    }

    /**
     * Gets elapse.
     *
     * @return the elapse
     */
    public int getElapse() {
        return elapse;
    }

    /**
     * Sets elapse.
     *
     * @param elapse
     *         the elapse
     */
    public void setElapse(int elapse) {
        this.elapse = elapse;
    }

    /**
     * Gets in.
     *
     * @return the in
     */
    public int getIn() {
        return in;
    }

    /**
     * Sets in.
     *
     * @param in
     *         the in
     */
    public void setIn(int in) {
        this.in = in;
    }

    /**
     * Gets out.
     *
     * @return the out
     */
    public int getOut() {
        return out;
    }

    /**
     * Sets out.
     *
     * @param out
     *         the out
     */
    public void setOut(int out) {
        this.out = out;
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
     * @param protocol
     *         the protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets alias.
     *
     * @param alias
     *         the alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets app id.
     *
     * @return the app id
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets app id.
     *
     * @param appId
     *         the app id
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }
}