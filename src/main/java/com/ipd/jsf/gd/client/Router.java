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

import java.util.List;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.registry.Provider;

/**
 * Title: 路由规则<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface Router {

    /**
     * 筛选Provider
     *
     * @param invocation
     *         本次调用（可以得到类名，方法名，方法参数，参数值等）
     * @param providers
     *         providers（<b>当前可用</b>的服务Provider列表）
     * @return 路由匹配的服务Provider列表
     */
    public List<Provider> route(Invocation invocation, List<Provider> providers);
}