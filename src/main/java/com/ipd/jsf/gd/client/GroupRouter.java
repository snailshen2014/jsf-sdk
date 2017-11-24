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
package com.ipd.jsf.gd.client;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.config.ConsumerGroupConfig;

/**
 * Title: 分组路由<br>
 *
 * Description: <br>
 */
public interface GroupRouter {

    /**
     * 分组路由
     *
     * @param invocation
     *         当前请求，可以拿到接口类，方法类，方法参数等
     * @param groupConfig
     *         当前分组配置，可以拿到当前分组列表，一些配置参数
     * @return 指定分组
     */
    public String router(Invocation invocation, ConsumerGroupConfig groupConfig);
}