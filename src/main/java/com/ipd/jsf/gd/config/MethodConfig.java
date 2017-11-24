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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.msg.ResponseListener;

/**
 * Title: 方法级配置，可出现在provider，consumer下面<br>
 * <p/>
 * Description: 对应jsf:method，用于指定方法下的配置，覆盖接口配置<br>
 * <p/>
 */
public class MethodConfig extends AbstractConfig {

    /*-------------配置项开始----------------*/
    /**
     * 方法名称，无法做到重载方法的配置
     */
    private String name;

    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String> parameters;

    /**
     * The Timeout. 远程调用超时时间(毫秒)
     */
    protected Integer timeout;

    /**
     * The Retries. 失败后重试次数
     */
    protected Integer retries;

    /**
     * The Async. 是否异步
     */
    protected Boolean async;

    /**
     * The Validation. 是否jsr303验证
     */
    protected Boolean validation;

    /**
     * 返回值之前的listener
     */
    protected List<ResponseListener> onreturn;

    /**
     * The concurrents. 最大并发执行（不管服务端还是客户端）
     */
    protected Integer concurrents;

    /**
     * The Cache. 客户端缓存
     */
    protected Boolean cache;

    /**
     * 是否启动压缩
     */
    protected String compress;

    /**
     * 目标参数（机房/分组）索引，第一个参数从0开始
     */
    protected Integer dstParam;

    /*-------------配置项结束----------------*/

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
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
     * Gets timeout.
     *
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets retries.
     *
     * @return the retries
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     */
    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    /**
     * Sets async.
     *
     * @param async the async
     */
    public void setAsync(Boolean async) {
        this.async = async;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public Integer getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     */
    public void setConcurrents(Integer concurrents) {
        this.concurrents = concurrents;
    }

    /**
     * Gets cache.
     *
     * @return the cache
     */
    public Boolean getCache() {
        return cache;
    }

    /**
     * Sets cache.
     *
     * @param cache the cache
     */
    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    /**
     * Sets validation.
     *
     * @param validation the validation
     */
    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    /**
     * Gets async.
     *
     * @return the async
     */
    public Boolean getAsync() {
        return async;
    }

    /**
     * Gets validation.
     *
     * @return the validation
     */
    public Boolean getValidation() {
        return validation;
    }

    /**
     * Gets onreturn.
     *
     * @return the onreturn
     */
    public List<ResponseListener> getOnreturn() {
        return onreturn;
    }

    /**
     * Sets onreturn.
     *
     * @param onreturn the onreturn
     */
    public void setOnreturn(List<ResponseListener> onreturn) {
        this.onreturn = onreturn;
    }

    /**
     * Gets compress.
     *
     * @return the compress
     */
    public String getCompress() {
        return compress;
    }

    /**
     * Sets compress.
     *
     * @param compress the compress
     */
    public void setCompress(String compress) {
        this.compress = compress;
    }

    /**
     * Gets dst param.
     *
     * @return the dst param
     */
    public Integer getDstParam() {
        return dstParam;
    }

    /**
     * Sets dst param.
     *
     * @param dstParam the dst param
     */
    public void setDstParam(Integer dstParam) {
        this.dstParam = dstParam;
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
}