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
package com.ipd.jsf;

/**
 * Title: 计数器服务接口<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface Counter {

    /**
     * 调用计数器
     *
     * @param interfaceName
     *         接口名
     * @param alias
     *         服务别名
     * @param methodName
     *         方法名
     * @param batch
     *         批量次数
     * @param appId
     *         appId
     * @return -1 超出调用次数
     *          0  有配置，且可以调用
     *          1  没有配置
     *          101  设置过期时间错误，但可以调用
     *          102 create jimdb key error
     */
    public int count(String interfaceName, String alias, String methodName, int batch, String appId);

    /**
     * http网关定制
     * @param key
     * @param batch
     * @param interval
     * 入参判断由client端实现
     * @return  -1  设置过期时间错误，但可以调用
     *          -2 create jimdb key error
     */
    public long gateCount(String key, int batch, int interval);
}