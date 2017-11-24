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
package com.ipd.org.msgpack.template.builder;

import com.ipd.jsf.gd.codec.msgpack.JSFMsgPack;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Title: <br>
 * <p>
 * Description: <br>
 * <p>
 * bean 字段的序列化与反序列化，在动态生成模板中使用
 * 避免在动态生成模板产生大量的bean 的代码段
 * <p>
 * </p>
 *
 * @since 2016/03/30 18:18
 */
public class BuildContextUtil {


    /**
     * checkMap 操作
     *
     * @param jsf_checkMap
     * @param bean
     * @param fieldList
     */
    public static void checkMap(Map jsf_checkMap, Object bean, FieldBean[] fieldList) {
        for (int i = 0; i < fieldList.length; i++) {
            FieldBean fb = fieldList[i];
            Class fbType = fb.getType();
            if (Modifier.isFinal(fbType.getModifiers())) {
                continue;
            }
            Object obj = fb.get(bean);
            if (obj == null) {
                continue;
            }
            if (!fbType.equals(obj.getClass())) {
                jsf_checkMap.put(String.valueOf(i), obj.getClass().getName());
            }
        }
    }


    /**
     * bean 字段的序列化
     *
     * @param _templates
     * @param index
     * @param packer
     * @param bean
     * @param required
     * @param isFieldPublic
     * @param isFieldPrimitive
     * @param isNotNullable
     * @param fieldBean
     * @param jsfVersion
     * @param jsfCheckMap
     * @throws IOException
     */
    public static void doFieldWrite(
            Template[] _templates,
            int index,
            Packer packer,
            Object bean,
            boolean required,
            boolean isFieldPublic,
            boolean isFieldPrimitive,
            boolean isNotNullable,
            FieldBean fieldBean,
            Short jsfVersion,
            Map jsfCheckMap) throws IOException {
        if (isFieldPrimitive) { // primitive types
            if (!isFieldPublic) {
                _templates[index].write(packer, fieldBean.get(bean));
            }
        } else { // reference types
            Object field = fieldBean.get(bean);
            if (field == null) {
                if (isNotNullable) {
                    throw new MessageTypeException(String.format("%s cannot be null by @NotNullable", field.getClass().getName()));
                } else {
                    packer.writeNil();
                }
            } else {
                Class clz = field.getClass();
                short jsfVer = jsfVersion == null ? 0 : jsfVersion.shortValue();
                Template temp = adaptTemlateByVersion(clz, jsfVer, jsfCheckMap, _templates, index, fieldBean);
                temp.write(packer, field);
            }
        }
    }

    /**
     * 根据版本自动找序列化模板
     */
    private static Template adaptTemlateByVersion(Class clz, short jsfVer, Map jsfCheckMap, Template[] _templates,
                                                  int index, FieldBean fieldBean) {
        Template temp = null;
        try {
            // 内部的List Set Map
            if ((clz.isMemberClass() || clz.isAnonymousClass()) &&
                    (Collection.class.isAssignableFrom(clz) || Map.class.isAssignableFrom(clz))) {
                temp = JSFMsgPack.registry.lookup(clz.getSuperclass());
            }
            // 匿名的枚举对象
            else if (clz.isAnonymousClass() && clz.getSuperclass().isEnum()) {
                temp = JSFMsgPack.registry.lookup(clz.getSuperclass());
            } else {
                temp = JSFMsgPack.registry.lookup(clz);
            }
        } catch (MessageTypeException e) {
            CodecUtils.checkAndRegistryClass(clz, new HashSet<Class<?>>());
            temp = JSFMsgPack.registry.lookup(clz);
        }
        if (temp == null) {
            temp = _templates[index];
        }
        return temp;
    }


    /**
     * public 的基本类型
     *
     * @param packer
     * @param fieldValue
     * @throws IOException
     */
    public static void doPPFieldWrite(Packer packer, int fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, long fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, short fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, double fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, float fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, boolean fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, char fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    public static void doPPFieldWrite(Packer packer, byte fieldValue) throws IOException {
        packer.write(fieldValue);
    }


    /**
     * bean 字段的反序列化
     *
     * @param _templates
     * @param index
     * @param fieldLen         属性个数
     * @param unpacker
     * @param bean
     * @param isFieldPrimitive
     * @param isOptional
     * @param fieldBean
     * @param jsfVersion
     * @param jsfCheckMap
     * @throws Exception
     */
    public static void doFieldRead(
            Template[] _templates,
            int index,
            int fieldLen,
            Unpacker unpacker,
            Object bean,
            boolean isFieldPrimitive,
            boolean isOptional,
            FieldBean fieldBean,
            Short jsfVersion,
            Map jsfCheckMap) throws Exception {

        FieldBean curField = null;
        if (index < fieldLen) {
            curField = fieldBean;
        }
        if (isOptional) {
            if (unpacker.trySkipNil()) {
                // if Optional and nil, then keep default value
                if (curField!=null && curField.get(bean) != null){
                    curField.set(bean, null);
                }
            } else if (index < fieldLen) {
                doRead(_templates, index, fieldLen, unpacker, bean, isFieldPrimitive, jsfVersion, jsfCheckMap, curField);
            }
        } else {
            doRead(_templates, index, fieldLen, unpacker, bean, isFieldPrimitive, jsfVersion, jsfCheckMap, curField);
        }
    }

    private static void doRead(Template[] _templates,
                               int index,
                               int fieldLen,
                               Unpacker unpacker,
                               Object bean,
                               boolean isFieldPrimitive,
                               Short jsfVersion,
                               Map jsfCheckMap,
                               FieldBean curField) throws Exception {
        if (isFieldPrimitive) {
            if (index < fieldLen) {
                curField.set(bean, _templates[index].read(unpacker, null));
            }
        } else {
            //进行父子类检查
            String childClassName = (String) jsfCheckMap.get(String.valueOf(index));
            if (childClassName == null) {
                try {
                    curField.set(bean, _templates[index].read(unpacker, ClassLoaderUtils.newInstance(curField.getType())));
                } catch (InstantiationException e) {
                    curField.set(bean, _templates[index].read(unpacker, null));
                } catch (NoSuchMethodException e) {
                    curField.set(bean, _templates[index].read(unpacker, null));
                } catch (SecurityException e) {
                    curField.set(bean, _templates[index].read(unpacker, null));
                }
            } else {
                Class childClass = ClassTypeUtils.getClass(childClassName);
                short jsfVer = jsfVersion == null ? 0 : jsfVersion.shortValue();
                Template temp = adaptTemlateByVersion(childClass, jsfVer, jsfCheckMap, _templates, index, curField);
                try {
                    curField.set(bean, temp.read(unpacker, ClassLoaderUtils.newInstance(childClass)));
                } catch (InstantiationException e) {
                    curField.set(bean, temp.read(unpacker, null));
                } catch (NoSuchMethodException e) {
                    curField.set(bean, temp.read(unpacker, null));
                } catch (SecurityException e) {
                    curField.set(bean, temp.read(unpacker, null));
                }
            }
        }
    }
}

