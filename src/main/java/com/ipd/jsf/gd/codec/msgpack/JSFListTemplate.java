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
package com.ipd.jsf.gd.codec.msgpack;

import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Title: JSF自定义list序列化模板<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFListTemplate<T> extends JSFAbstractTemplate<List<T>> {

    private static final Logger logger = LoggerFactory.getLogger(JSFListTemplate.class);

    @Override
    public void write(Packer pk, List<T> target, boolean required)
            throws IOException {
        if (target == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        boolean allSameTypeFlag = isAllSameType(target);
        if (allSameTypeFlag) { // 所有元素类型一致
            // 格式为： 类型一致标识 + 类名（可能是valueIndex也可能是类名） + （元素值） + （元素值） + ...
            pk.writeArrayBegin(target.size() + 2); // 总长度 + 2（标识+类名）
            pk.write(allSameTypeFlag); // 类型一致 true
            boolean hasBeenWriteClass = false;
            Template template = null;
            for (Object value : target) {
                if (!hasBeenWriteClass) {
                    Class<?> valueClass = value.getClass();
                    String valueIndex = TypeEnum.getIndex(valueClass);
                    valueIndex = valueIndex == null ? ClassTypeUtils.getTypeStr(valueClass) : valueIndex; //非基本类型,直接写类名
                    pk.write(valueIndex);
                    try {
                        template = registry.lookup(valueClass);
                    } catch (MessageTypeException e) {
                        CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
                        template = registry.lookup(valueClass);
                    }
                    hasBeenWriteClass = true;
                }
                template.write(pk, value);
            }
        } else { // 所有元素类型，其中有不一致
            // 格式为： 类型一致标识 + （元素名字 + 元素值） + （元素名字 + 元素值）+ ...
            pk.writeArrayBegin(target.size() + 1);
            pk.write(allSameTypeFlag);  // 类型不一致 false
            for (T t : target) {
                writeValue(pk, t);
            }
        }
        pk.writeArrayEnd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> read(Unpacker u, List<T> to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        int n = u.readArrayBegin();
        if (to == null) {
            to = new ArrayList<T>(n);
        } else {
            to.clear();
        }

        boolean allSameTypeFlag = u.readBoolean();
        if (allSameTypeFlag) { // 所有元素类型一致
            Template template = null;
            Class<?> valueClass = null;
            String valueIndex = u.readString();
            if (valueIndex != null) {
                valueClass = TypeEnum.getType(valueIndex);
                if (valueClass == null) {
                    try {
                        valueClass = ClassTypeUtils.getClass(valueIndex);
                    } catch (Exception e) {
                        logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
                        throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                    }
                }
            } else {
                throw new MessageTypeException("[JSF-24005]Deserialize data error occurred. The data's format is not right.");
            }
            try {
                template = registry.lookup(valueClass);
            } catch (MessageTypeException e) {
                CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
                template = registry.lookup(valueClass);
            }
            for (int i = 0; i < n - 2; i++) {
                try {
                    to.add((T) template.read(u, getItemInstance(valueClass)));
                } catch (Exception e) {
                    logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
                    throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                }
            }
        } else { // 所有元素类型，其中有不一致
            for (int i = 0; i < n - 1; i++) {
                // 依据序列化格式反序列化value值
                T val = (T) readValue(u);
                to.add(val);
            }
        }
        u.readArrayEnd();
        return to;
    }

    private <T> T getItemInstance(Class<T> valueClass){
        try {
            return ClassLoaderUtils.newInstance(valueClass);
        }catch (Exception e){
            logger.warn("JSFListTemplate itme can be not get instance, error: {}", e.getMessage());
        }
        return null;
    }

    private static JSFListTemplate instance = new JSFListTemplate();

    private JSFListTemplate() {
    }

    public static JSFListTemplate getInstance() {
        return instance;
    }
}