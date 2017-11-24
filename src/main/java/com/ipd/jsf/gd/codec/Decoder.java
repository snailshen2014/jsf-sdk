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
package com.ipd.jsf.gd.codec;

/**
 * Title: 序列化Decoder接口<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface Decoder {

    /**
     * 反序列化，按指定类
     *
     * @param datas
     *         byte[]数据
     * @param clazz
     *         指定class类
     * @return 实际对象
     */
    Object decode(byte[] datas, Class clazz);

    /**
     * 反序列化，按指定类名
     *
     * @param datas
     *         byte[]数据
     * @param className
     *         指定class类名
     * @return 实际对象
     */
    Object decode(byte[] datas, String className);

}