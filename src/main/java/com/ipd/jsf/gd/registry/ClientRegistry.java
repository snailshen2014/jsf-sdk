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
package com.ipd.jsf.gd.registry;

import java.util.List;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;

/**
 * Title: 客户端注册中心接口<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface ClientRegistry {


    /**
     * 注册服务提供者
     *
     * @param config
     *         Provider配置
     * @param listener
     *         配置监听器
     */
    void register(ProviderConfig config, ConfigListener listener);

    /**
     * 反注册服务提供者
     *
     * @param config
     *         Provider配置
     */
    void unregister(ProviderConfig config);

    /**
     * 订阅服务列表
     *
     * @param config
     *         Consumer配置
     * @param providerListener
     *         配置监听器
     * @param configListener
     *         配置监听器
     * @return 当前Provider列表 list
     */
    List<Provider> subscribe(ConsumerConfig config, ProviderListener providerListener,
                             ConfigListener configListener);

    /**
     * 反订阅服务调用者相关配置
     *
     * @param config
     *         Consumer配置
     */
    void unsubscribe(ConsumerConfig config);


    /**
     * Destroy void.
     */
    void destroy();
}