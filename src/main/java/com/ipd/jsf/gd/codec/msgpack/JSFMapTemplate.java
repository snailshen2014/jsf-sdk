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

import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Title: JSF自定义map序列化模板类,用于Map参数的序列化和反序列化<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFMapTemplate<K, V> extends JSFAbstractTemplate<Map<K, V>> {

    private static final Logger logger = LoggerFactory.getLogger(JSFMapTemplate.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void write(Packer pk, Map<K, V> target, boolean required) throws IOException {
        if (target == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        Template keyTemplate = null;
        /*
         * 把map作为一个bean输出。
         */
        int len = 1;
        boolean allSameTypeFlag = isAllSameType(target.values());
        if (allSameTypeFlag) {
            // 所有元素类型一致
            // 格式为： 类型一致标识 + key类名（可能是valueIndex也可能是类名） + value类名（同前）
            //          + （key元素值 + value 元素值） + （key元素值 + value 元素值） + ...
            if (target.size() != 0) {
                len = target.size() * 2 + 3; // 总长度*2 + 3（标识+key类型+value类型）
            }
            pk.writeArrayBegin(len);
            pk.write(allSameTypeFlag);
            boolean hasBeenWriteClass = false;
            Template valueTemplate = null;
            for (Map.Entry<K, V> pair : target.entrySet()) {
                K k = pair.getKey();
                V value = pair.getValue();
                if (!hasBeenWriteClass) {
                    /*
	        		 * key和value的class是不变的，在第一条信息中写上key和value的class名称（全路径）
	        		 * 如果修改规范，value的class不变的话，可以放在头部
	        		 */
                    Class<?> keyClass = k.getClass();
                    String keyIndex = TypeEnum.getIndex(keyClass);
                    keyIndex = keyIndex == null ? ClassTypeUtils.getTypeStr(keyClass) : keyIndex;
                    pk.write(keyIndex);
                    try {
                        keyTemplate = this.registry.lookup(keyClass);
                    } catch (MessageTypeException e) {
                        CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
                        keyTemplate = this.registry.lookup(keyClass);
                    }

                    Class<?> valueClass = value.getClass();
                    String valueIndex = TypeEnum.getIndex(valueClass);
                    valueIndex = valueIndex == null ? ClassTypeUtils.getTypeStr(valueClass) : valueIndex;
                    pk.write(valueIndex);
                    try {
                        valueTemplate = this.registry.lookup(valueClass);
                    } catch (MessageTypeException e) {
                        CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
                        valueTemplate = this.registry.lookup(valueClass);
                    }
                    hasBeenWriteClass = true;
                }
                keyTemplate.write(pk, k);
                valueTemplate.write(pk, value);
            }
        } else {
            // 所有元素类型有不一致
            // 格式为： 类型一致标识 + key类名（可能是valueIndex也可能是类名）
            //          + （key元素值 + value元素类型 + value元素值） + （key元素值 + value元素类型 + value元素值） + ...
            if (target.size() != 0) {
                len = target.size() * 2 + 2;
            }
            pk.writeArrayBegin(len);
            pk.write(allSameTypeFlag);
            boolean hasBeenWriteClass = false;
            for (Map.Entry<K, V> pair : target.entrySet()) {
                K k = pair.getKey();
                if (!hasBeenWriteClass) {
                    /*
	        		 * key的class是不变的，在第一条信息中写上key的class名称（全路径）
	        		 * 如果修改规范，value的class不变的话，可以放在头部
	        		 */
                    Class<?> keyClass = k.getClass();
                    String keyIndex = TypeEnum.getIndex(keyClass);
                    keyIndex = keyIndex == null ? ClassTypeUtils.getTypeStr(keyClass) : keyIndex;
                    //	        	stringTemplate.write(pk, keyIndex);
                    pk.write(keyIndex);
                    try {
                        keyTemplate = this.registry.lookup(keyClass);
                    } catch (MessageTypeException e) {
                        CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
                        keyTemplate = this.registry.lookup(keyClass);
                    }
                    hasBeenWriteClass = true;
                }
                keyTemplate.write(pk, k);
	        	
	    		/*
	    		 * value格式为：class全名称，值
	    		 * 先写入class全名称，然后再写入值
	    		 */
                V value = pair.getValue();
                writeValue(pk, value);
            }
        }
        pk.writeArrayEnd();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map<K, V> read(Unpacker u, Map<K, V> to, boolean required)
            throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
		/*
		 * 作为Bean读入，然后把读取的值放入map
		 */
        int len = u.readArrayBegin();
        Map<K, V> map;
        boolean flag = u.readBoolean();
        if (to != null) {
            map = (Map<K, V>) to;
            map.clear();
        } else {
            map = new HashMap<K, V>(len / 2);
        }
        if (len > 1) {
            int n = len / 2 - 1;
            if (flag) {
                String keyIndex = u.readString();
                String valueIndex = u.readString();
                Template keyTemplate = null;
                Template valueTemplate = null;
                if (keyIndex == null) {
                    logger.error("[JSF-24004]Deserialize data error occurred. Key of class can be not found.");
                    throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Key of class can be not found.");
                }
				/*
				 * 取得key的class全名称
				 * 然后取得序列化模板
				 */
                Class<?> keyClass = null;
                if (keyIndex.length() < 3) {
                    keyClass = TypeEnum.getType(keyIndex);
                } else {
                    try {
                        keyClass = ClassTypeUtils.getClass(keyIndex);
                    } catch (Exception e) {
                        logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
                        throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                    }
                }
                try {
                    keyTemplate = this.registry.lookup(keyClass);
                } catch (MessageTypeException e) {
                    CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
                    keyTemplate = this.registry.lookup(keyClass);
                }
				/*
				 * 取得value的class全名称
				 * 然后取得序列化模板
				 */

                if (valueIndex == null) {
                    logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                    throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                }
                Class<?> valueClass = null;
                if (valueIndex.length() < 3) {
                    valueClass = TypeEnum.getType(valueIndex);
                } else {
                    try {
                        valueClass = ClassTypeUtils.getClass(valueIndex);
                    } catch (Exception e) {
                        logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
                        throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                    }
                }
                try {
                    valueTemplate = this.registry.lookup(valueClass);
                } catch (MessageTypeException e) {
                    CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
                    valueTemplate = this.registry.lookup(valueClass);
                }
                for (int i = 0; i < n; i++) {
                    map.put((K) keyTemplate.read(u, null), (V) valueTemplate.read(u, null));
                }
            } else {
                String keyIndex = u.readString();
                Template keyTemplate = null;
                if (keyIndex == null) {
                    logger.error("[JSF-24004]Deserialize data error occurred. Key of class can be not found.");
                    throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Key of class can be not found.");
                }
				/*
				 * 取得key的class全名称
				 * 然后取得序列化模板
				 */
                Class<?> keyClass = null;
                if (keyIndex.length() < 3) {
                    keyClass = TypeEnum.getType(keyIndex);
                } else {
                    try {
                        keyClass = ClassTypeUtils.getClass(keyIndex);
                    } catch (Exception e) {
                        logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
                        throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
                    }
                }
                try {
                    keyTemplate = this.registry.lookup(keyClass);
                } catch (MessageTypeException e) {
                    CodecUtils.checkAndRegistryClass(keyClass, new HashSet<Class<?>>());
                    keyTemplate = this.registry.lookup(keyClass);
                }

                for (int i = 0; i < n; i++) {
		        	/*
		        	 * 依据序列化格式反序列化value值
		        	 */
                    K key = (K) keyTemplate.read(u, null);
                    V val = (V) readValue(u);
                    map.put(key, val);
                }
            }
        }
        u.readArrayEnd();
        return map;
    }

    private static JSFMapTemplate instance = new JSFMapTemplate();

    private JSFMapTemplate() {
    }

    public static JSFMapTemplate getInstance() {
        return instance;
    }
}