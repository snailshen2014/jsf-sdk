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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ipd.jsf.gd.error.JSFCodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.template.StringTemplate;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.template.TemplateRegistry;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * JSF自定义序列化模板抽象类
 *
 * @param <T>
 */
public abstract class JSFAbstractTemplate<T> extends AbstractTemplate<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(JSFAbstractTemplate.class);
	
	protected StringTemplate stringTemplate = StringTemplate.getInstance();
	
	protected TemplateRegistry registry = JSFMsgPack.getTemplateRegistry();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void writeValue(Packer pk, Object v) throws IOException {
		if(v == null){
			pk.writeArrayBegin(1);
			pk.writeNil();
			pk.writeArrayEnd();
			return;
		}
		pk.writeArrayBegin(2);
		Class<?> valueClass = v.getClass();
        String valueIndex = TypeEnum.getIndex(valueClass);
        //非基本类型
        if(valueIndex == null){
        	String valueClassName = ClassTypeUtils.getTypeStr(valueClass);
        	pk.write(valueClassName);
        	Template template;
            try {
    			template = registry.lookup(valueClass);
    		} catch(MessageTypeException e){
    			CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
    			template = registry.lookup(valueClass);
    		}
            template.write(pk, v);
        } else {
        	pk.write(valueIndex);
        	pk.write(v);
        }
        pk.writeArrayEnd();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object readValue(Unpacker u) throws IOException {
		Class<?> valueClass = null;
    	Template valTemplate = null;
    	int len = u.readArrayBegin();
    	if(len == 1){
    		u.readArrayEnd();
    		return null;
    	}
    	Object value = null;
		String valueIndex = u.readString();
		if(valueIndex != null){
			valueClass = TypeEnum.getType(valueIndex);
			if(valueClass == null) {
				try {
					valueClass = ClassTypeUtils.getClass(valueIndex);
				} catch (Exception e) {
					logger.error("[JSF-24004]Deserialize data error occurred. Value of class can be not found.", e);
					throw new MessageTypeException("[JSF-24004]Deserialize data error occurred. Value of class can be not found.");
				}
			}
	    	try {
	    		valTemplate = registry.lookup(valueClass);
			} catch (MessageTypeException e){
				CodecUtils.checkAndRegistryClass(valueClass, new HashSet<Class<?>>());
				valTemplate = registry.lookup(valueClass);
			} finally {
				if(valTemplate != null){
					if(TypeEnum.getIndex(valueClass) != null){
						value = valTemplate.read(u, null);
					} else {
						try {
							value = valTemplate.read(u, ClassLoaderUtils.newInstance(valueClass));
						}catch(Exception e){
							if(e instanceof JSFCodecException){
								throw (JSFCodecException)e;
							} else {
								value = valTemplate.read(u, null);
							}
						}
					}
				} else {
					logger.error("[JSF-24003]Deserialize data error occurred. Could not find the template:" + valueClass.getName());
					throw new MessageTypeException("[JSF-24003]Deserialize data error occurred. Could not find the template:" + valueClass.getName());
				}
			}
		}
    	u.readArrayEnd();
    	return value;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	protected Object[] toArray(Collection coll){
		if(coll.isEmpty()){
			return null;
		}
		Iterator it = coll.iterator();
		Class elementClass = null;
		while (it.hasNext()) {
			Object value = it.next();
			if (value == null) {
				return null;
			}
			if (elementClass == null) {
				elementClass = value.getClass();
			} else if (elementClass != value.getClass()) {
				// 存在不一致的情况
				return null;
			}
		}
		Object[] targetArray=(Object[])Array.newInstance(elementClass, coll.size());
		try{
			Object[] myArray = coll.toArray(targetArray);
			return myArray;
		} catch(Exception e){
			return null;
		}
	}

	protected boolean isAllSameType(Collection coll){
		if(coll.isEmpty()){
			return false;
		}
		Iterator it = coll.iterator();
		Class elementClass = null;
		while (it.hasNext()) {
			Object value = it.next();
			if (value == null) {
				return false;
			}
			if (elementClass == null) {
				elementClass = value.getClass();
			} else if (elementClass != value.getClass()) {
				// 存在不一致的情况
				return false;
			}
		}
		return true;
	}

}