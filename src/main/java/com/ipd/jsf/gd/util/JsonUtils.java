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
package com.ipd.jsf.gd.util;

import java.lang.reflect.Type;

import com.ipd.fastjson.JSON;
import com.ipd.fastjson.parser.ParserConfig;
import com.ipd.fastjson.parser.deserializer.ObjectDeserializer;
import com.ipd.fastjson.serializer.SerializeConfig;
import com.ipd.jsf.gd.codec.json.InvocationDeserializer;
import com.ipd.jsf.gd.codec.json.ResponseSerializer;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.codec.json.InvocationSerializer;
import com.ipd.jsf.gd.codec.json.ResponseDeserializer;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: 包装了JSON的基本行为，隐藏后面的实现 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JsonUtils {

    /**
     * 注册json模板
     */
    public static void registerTemplate() {
        //for extend 便于扩展，定制invocation 序列化/反序化模板
        Object serializerObj = SerializeConfig.getGlobalInstance().get(Invocation.class);
        if (serializerObj == null || !(serializerObj instanceof InvocationSerializer)){
            SerializeConfig.getGlobalInstance().put(Invocation.class, InvocationSerializer.instance);
        }
        ObjectDeserializer deserializerObj = ParserConfig.getGlobalInstance().getDeserializer(Invocation.class);
        if ( deserializerObj == null || !(deserializerObj instanceof InvocationDeserializer)){
            ParserConfig.getGlobalInstance().putDeserializer(Invocation.class, InvocationDeserializer.instance);
        }
        //for extend 便于扩展，定制ResponseMessage 序列化/反序化模板
        Object responseSerializer = SerializeConfig.getGlobalInstance().get(ResponseMessage.class);
        if ( responseSerializer == null || !(responseSerializer instanceof ResponseSerializer)){
            SerializeConfig.getGlobalInstance().put(ResponseMessage.class, ResponseSerializer.instance);
        }
        ObjectDeserializer responseDeserializerObj = ParserConfig.getGlobalInstance().getDeserializer(ResponseMessage.class);
        if ( responseDeserializerObj == null || !(responseDeserializerObj instanceof ResponseDeserializer)){
            ParserConfig.getGlobalInstance().putDeserializer(ResponseMessage.class, ResponseDeserializer.instance);
        }
    }

    /**
     * 对象转为json字符串
     *
     * @param object
     *         对象
     * @return json字符串
     */
    public static final String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * 解析为指定对象
     *
     * @param text
     *         json字符串
     * @param clazz
     *         指定类
     * @param <T>
     *         指定对象
     * @return 指定对象
     */
    public static final <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 解析为指定对象
     *
     * @param text
     *         json字符串
     * @param clazz
     *         指定类
     * @param <T>
     *         指定对象
     * @return 指定对象
     */
    public static final <T> T parseObjectByType(String text, Type clazz) {
        return JSON.parseObject(text, clazz);
    }
}