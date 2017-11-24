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
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CommonUtils;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.ipd.jsf.gd.util.JSFLogicSwitch.JSON_SERIALIZER_FEATURES;

/**
 * Title: ResponseMessage 序列化<br>
 * <p/>
 * Description: <br>
 * 把ResponseMessage 中的response 具体类型写入json中
 * <p/>
 * @since 2015/01/12 17:25
 */
public class ResponseSerializer implements ObjectSerializer ,ResponseSerializable{

    public static final ResponseSerializer instance = new ResponseSerializer();

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
        ResponseMessage resMsg = (ResponseMessage) object;
        //1、class name
        out.writeFieldName(RES_CLASS);
        Class responseCLz = null;
        if ( resMsg.getResponse()!= null ){
            responseCLz = resMsg.getResponse().getClass();
            out.writeString(ClassTypeUtils.getTypeStr(responseCLz));
        } else if (resMsg.getException() != null  ){
            responseCLz = resMsg.getException().getClass();
            out.writeString(ClassTypeUtils.getTypeStr(responseCLz));
        } else {
            out.writeNull();
        }
        //2、response
        if ( responseCLz != null ){
            out.append(",");
            out.writeFieldName(RESPONSE);
            ObjectSerializer responseSerializer = serializer.getObjectWriter(responseCLz);
            responseSerializer.write(serializer,resMsg.getResponse(),RESPONSE,null, features);
            //out.append(",");
        }
        //3、exception
        if ( resMsg.getException() != null ){
            out.append(",");
            out.writeFieldName(EXCEPTION);
            ObjectSerializer throwableSerializer = serializer.getObjectWriter(responseCLz);
            throwableSerializer.write(serializer,resMsg.getException(),EXCEPTION,null, features);
            //out.append(",");
        }
        out.append("}");

    }
}