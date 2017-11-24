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
 * Title: 序列化Encoder接口<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface Encoder {

    /**
     * 直接序列化
     *
     * @param obj
     *         要序列化的对象
     * @return byte[]
     */
    byte[] encode(Object obj);

    /**
     * 按指定类型的序列化
     *
     * @param obj
     *         byte[]
     * @param classTypeName
     *         类型
     * @return byte[]
     */
    byte[] encode(Object obj, String classTypeName);

}