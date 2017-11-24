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
package com.ipd.jsf.gd.codec.json;

import com.ipd.fastjson.serializer.JSONSerializer;
import com.ipd.fastjson.serializer.ObjectSerializer;
import com.ipd.fastjson.serializer.SerializeWriter;
import com.ipd.fastjson.serializer.SerializerFeature;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.CommonUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import static com.ipd.jsf.gd.util.JSFLogicSwitch.JSON_SERIALIZER_FEATURES;

/**
 * Title: Invocation fastjson 序列化<br>
 * <p/>
 * Description: <br>
 *     保证序列化字段按如下顺序：<br>
 *     1、class name 即接口名称<br>
 *     2、alias<br>
 *     3、method name<br>
 *     4、argsType callback 调用才会写
 *     5、args 参数value<br>
 *     6、attachments (值不为空则序列化)<br>
 * <p/>
 *
 * @since 2015/01/12 13:06
 */
public class InvocationSerializer implements ObjectSerializer,InvocationSerializable {

    public static final InvocationSerializer instance = new InvocationSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (CommonUtils.isNotEmpty(JSON_SERIALIZER_FEATURES)) {
            for (SerializerFeature feature : JSON_SERIALIZER_FEATURES) {
                serializer.config(feature, true);
            }
        }
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            out.writeNull();
            return;
        }
        out.append("{");
        Invocation invocation = (Invocation) object;
        //1、class name
        out.writeFieldName(CLAZZ_NAME);
        out.writeString(invocation.getClazzName());
        out.append(",");
        //2、alias
        out.writeFieldName(ALIAS);
        out.writeString(invocation.getAlias());
        out.append(",");
        //3、method name
        out.writeFieldName(METHOD_NAME);
        out.writeString(invocation.getMethodName());
        out.append(",");
        //4.argsType
        if ( CALLBACK_CLZ.equals(invocation.getClazzName())){
            ObjectSerializer argsTypeSerializer = serializer.getObjectWriter(String[].class);
            out.writeFieldName(ARGS_TYPE);
            argsTypeSerializer.write(serializer,invocation.getArgsType(),ARGS_TYPE,null, features);
            out.append(",");
        }
        //5、args
        out.writeFieldName(ARGS);
        ObjectSerializer argsSerializer = serializer.getObjectWriter(Object[].class);
        if ( invocation.getArgs() == null || invocation.getArgsType() != null && invocation.getArgsType().length == 0){
            out.writeNull();
        } else {
            argsSerializer.write(serializer,invocation.getArgs(),ARGS,null, features);
        }
        //6、attachments
        if ( invocation.getAttachments() != null && invocation.getAttachments().size() > 0 ){
            out.append(",");
            out.writeFieldName(ATTACHMENTS);
            ObjectSerializer mapSerializer = serializer.getObjectWriter(Map.class);
            mapSerializer.write(serializer,invocation.getAttachments(),ATTACHMENTS,null, features);
        }
        out.append("}");
    }
}