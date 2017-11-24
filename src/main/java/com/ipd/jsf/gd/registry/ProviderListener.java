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

/**
 * Title: 服务列表变化Listener<br>
 * <p/>
 * Description: 实现新增，删除，覆盖等方法。清空等于覆盖为空的<br>
 * <p/>
 */
public interface ProviderListener {

    /**
     * 增加服务节点
     *
     * @param providers 待新增的服务列表（部分）
     */
    void addProvider(List<Provider> providers);

    /**
     * 删除服务节点
     *
     * @param providers 待删除的服务列表(部分)
     */
    void removeProvider(List<Provider> providers);

    /**
     * 更新服务节点
     *
     * @param providers 新的服务列表(全)
     */
    void updateProvider(List<Provider> providers);

}