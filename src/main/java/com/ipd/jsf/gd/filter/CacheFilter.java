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
package com.ipd.jsf.gd.filter;

import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.filter.cache.Cache;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 结果缓存过滤器<br>
 * <p/>
 * Description: 服务端和客户端均可使用<br>
 * <p/>
 */
public class CacheFilter extends AbstractFilter {

    /**
     * 配置信息
     */
    private final AbstractInterfaceConfig config;

    /**
     * 构造函数
     *
     * @param config
     *         ProviderConfig
     */
    public CacheFilter(AbstractInterfaceConfig config) {
        this.config = config;
        if (config.getCacheref() == null) {
            throw new IllegalConfigureException(21205, "cacheref", null, "Must assign cache when cache=\"true\"");
        }
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        // 命中缓存，直接就不走rpc调用
        Invocation invocation = request.getInvocationBody();
        String methodName = invocation.getMethodName();
        boolean iscache = getBooleanMethodParam(methodName, Constants.CONFIG_KEY_CACHE, config.isCache());
        // 该方法对应有结果缓存
        if (iscache) {
            Cache cache = config.getCacheref();
            String interfaceId = invocation.getClazzName();
            Object key = cache.buildKey(interfaceId, methodName, invocation.getArgs());
            if (key != null) { // 有key
                Object result = cache.get(key);
                if (result != null) {
                    // 命中缓存，直接就不走rpc调用
                    ResponseMessage response = MessageBuilder.buildResponse(request);
                    response.setResponse(result);
                    return response;
                } else {
                    // 未命中发起远程调用
                    ResponseMessage response = getNext().invoke(request);
                    if (!response.isError()) {
                        // 调用成功 保存结果
                        cache.put(key, response.getResponse());
                    }
                    return response;
                }
            }
        }
        // 该方法未开启缓存
        return getNext().invoke(request);
    }
}