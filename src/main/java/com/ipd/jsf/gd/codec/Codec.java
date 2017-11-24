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
 * Title: Coderc接口<br>
 * <p/>
 * Description: 包含Encoder和Decoder<br>
 * <p/>
 */
public interface Codec extends Encoder, Decoder{

    /**
     * 空的Object数组，无参方法
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 空的Class数组，无参方法
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

}