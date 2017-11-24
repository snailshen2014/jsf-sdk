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
package com.ipd.jsf.service;

import java.util.List;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.vo.Heartbeat;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.vo.HbResult;
import com.ipd.jsf.vo.SubscribeUrl;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface RegistryService {

    /**
     * provider/consumer注册
     *
     * @param jsfUrl
     *         the jsf url
     * @return jsf url
     * @throws RpcException
     *         the rpc exception
     */
    public JsfUrl doRegister(JsfUrl jsfUrl) throws RpcException;

    /**
     * provider/consumer批量注册
     *
     * @param jsfUrlList
     *         the jsf url list
     * @return list
     * @throws RpcException
     *         the rpc exception
     */
    public List<JsfUrl> doRegisterList(List<JsfUrl> jsfUrlList) throws RpcException;

    /**
     * 检查注册列表的方法
     *
     * @param jsfUrl
     *         the jsf url
     * @return boolean
     * @throws RpcException
     *         the rpc exception
     */
    public boolean doCheckRegister(JsfUrl jsfUrl) throws RpcException;

    /**
     * provider/consumer取消注册
     *
     * @param jsfUrl
     *         the jsf url
     * @return boolean
     * @throws RpcException
     *         the rpc exception
     */
    public boolean doUnRegister(JsfUrl jsfUrl) throws RpcException;

    /**
     * 检查反注册列表的方法
     *
     * @param jsfUrl
     *         the jsf url
     * @return boolean
     * @throws RpcException
     *         the rpc exception
     */
    public boolean doCheckUnRegister(JsfUrl jsfUrl) throws RpcException;

    /**
     * provider/consumer批量取消注册
     *
     * @param jsfUrlList
     *         the jsf url list
     * @return boolean
     * @throws RpcException
     *         the rpc exception
     */
    public boolean doUnRegisterList(List<JsfUrl> jsfUrlList) throws RpcException;

    /**
     * consumer订阅服务provider
     *
     * @param jsfUrl
     *         the jsf url
     * @param subscribeData
     *         the subscribe data
     * @return subscribe url
     * @throws RpcException
     *         the rpc exception
     */
    public SubscribeUrl doSubscribe(JsfUrl jsfUrl, Callback<SubscribeUrl, String> subscribeData) throws RpcException;

    /**
     * 取消订阅，删除callback
     *
     * @param jsfUrl
     *         the jsf url
     * @return boolean
     * @throws RpcException
     *         the rpc exception
     */
    public boolean doUnSubscribe(JsfUrl jsfUrl) throws RpcException;

    /**
     * 读取provider信息
     *
     * @param jsfUrl
     *         the jsf url
     * @return subscribe url
     * @throws RpcException
     *         the rpc exception
     */
    public SubscribeUrl lookup(JsfUrl jsfUrl) throws RpcException;

    /**
     * 批量读取provider信息
     *
     * @param list
     *         the list
     * @return list
     * @throws RpcException
     *         the rpc exception
     */
    public List<SubscribeUrl> lookupList(List<JsfUrl> list) throws RpcException;

    /**
     * 实例心跳
     *
     * @param heartbeat
     *         the heartbeat
     * @return hb result
     * @throws RpcException
     *         the rpc exception
     */
    public HbResult doHeartbeat(Heartbeat heartbeat) throws RpcException;

    /**
     * 订阅配置信息
     *
     * @param jsfUrl
     *         the jsf url
     * @param subscribeData
     *         the subscribe data
     * @return jsf url
     * @throws RpcException
     *         the rpc exception
     */
    public JsfUrl subscribeConfig(JsfUrl jsfUrl, Callback<SubscribeUrl, String> subscribeData) throws RpcException;

    /**
     * 获取配置信息
     *
     * @param jsfUrl
     *         the jsf url
     * @return config
     * @throws RpcException
     *         the rpc exception
     */
    public JsfUrl getConfig(JsfUrl jsfUrl) throws RpcException;

    /**
     * 获取配置信息列表
     *
     * @param list
     *         the list
     * @return config list
     * @throws RpcException
     *         the rpc exception
     */
    public List<JsfUrl> getConfigList(List<JsfUrl> list) throws RpcException;
}