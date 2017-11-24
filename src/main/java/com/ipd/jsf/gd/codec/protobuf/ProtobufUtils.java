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
package com.ipd.jsf.gd.codec.protobuf;

import java.lang.reflect.Method;

import com.google.protobuf.MessageLite;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ProtobufUtils {

    public static void checkParameterAndReturnType(Class... classes) {
        if (CommonUtils.isEmpty(classes)) {
            return;
        }
        for (Class clazz : classes) {
            Method[] methods = clazz.getMethods();
            if (CommonUtils.isNotEmpty(methods)) {
                for (Method method : methods) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (Class<?> parameterType : parameterTypes) {
                        checkSupportedType(parameterType);
                    }
                    Class returnType = method.getReturnType();
                    checkSupportedType(returnType);
                }
            }
        }
    }

    private static void checkSupportedType(Class clazz) {
        if (MessageLite.class.isAssignableFrom(clazz)
//                || MessageLite.Builder.class.isAssignableFrom(clazz)
//                || clazz == String.class
//                || clazz == int.class
//                || clazz == boolean.class
//                || clazz == long.class
//                || clazz == byte[].class
//                || clazz == double.class
//                || clazz == byte.class
            ) {
            // 支持
        } else {
            throw new JSFCodecException("Protobuf unsupported class:" + ClassTypeUtils.getTypeStr(clazz) +
                    ", only support String/int/boolean/long/double/byte/byte[]/ProtobufMessage");
        }
    }

}