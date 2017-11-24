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


import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessagePack;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.template.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * JSF自定义封装msgpack序列化工具类
 *
 */
public class JSFMsgPack extends MessagePack {

	private static final Logger logger = LoggerFactory.getLogger(JSFMsgPack.class);

	public static final byte REF_CTRL = (byte)0xd4;
	
	private static Set<Class<?>> registrySet = Collections.synchronizedSet(new HashSet<Class<?>>());
	
	public static TemplateRegistry registry;
	
	static {
		registry = new TemplateRegistry(null); 
		registry.register(HashMap.class, JSFMapTemplate.getInstance());
		registrySet.add(HashMap.class);
		registry.register(HashSet.class, JSFSetTemplate.getInstance());
		registrySet.add(HashSet.class);
		registry.register(Object.class, JSFObjectTemplate.getInstance());
		registrySet.add(Object.class);
		registry.register(ArrayList.class, JSFListTemplate.getInstance());
		registrySet.add(ArrayList.class);
		registry.register(char[].class, CharArrayTemplate.getInstance());
		registrySet.add(char[].class);
		registry.register(Character[].class, CharacterArrayTemplate.getInstance());
		registrySet.add(Character[].class);
		registry.register(Throwable.class, JSFExceptionTemplate.getInstance());
		registrySet.add(Throwable.class);
		registry.register(StackTraceElement.class, JSFStackTraceElementTemplate.getInstance());
		registrySet.add(StackTraceElement.class);
		registry.register(StackTraceElement[].class, JSFStackTraceElementArrayTemplate.getInstance());
		registrySet.add(StackTraceElement[].class);
		registry.register(Invocation.class, InvocationTemplate.getInstance());
		registrySet.add(Invocation.class);
		
		registry.register(java.sql.Date.class, JSFSqlDateTemplate.getInstance());
		registrySet.add(java.sql.Date.class);
		registry.register(Time.class, JSFTimeTemplate.getInstance());
		registrySet.add(Time.class);
		registry.register(Timestamp.class, JSFTimestampTemplate.getInstance());
		registrySet.add(Timestamp.class);

		registry.register(File.class, JSFFileTemplate.getInstance());
		registrySet.add(File.class);
		registry.register(URL.class, JSFURLTemplate.getInstance());
		registrySet.add(URL.class);
		registry.register(URI.class, JSFURITemplate.getInstance());
		registrySet.add(URI.class);

		registry.register(Charset.class, JSFCharsetTemplate.getInstance());
		registrySet.add(Charset.class);
		registry.register(Class.class, JSFClassTemplate.getInstance());
		registrySet.add(Class.class);
		registry.register(Calendar.class, JSFCalendarTemplate.getInstance());
		registrySet.add(Calendar.class);
		registry.register(Locale.class, JSFLocaleTemplate.getInstance());
		registrySet.add(Locale.class);
		registry.register(TimeZone.class, JSFTimeZoneTemplate.getInstance());
		registrySet.add(TimeZone.class);
		registry.register(UUID.class, JSFUUIDTemplate.getInstance());
		registrySet.add(UUID.class);
	}
	
	public static TemplateRegistry getTemplateRegistry(){
		return registry;
	}
	
	public JSFMsgPack(){
		super(registry);
		
	}
	
	public <T> void register(Class<T> type, Template<T> template) {
		registrySet.add(type);
        registry.register(type, template);
    }
	
	public static void registry(Class<?> c){
		if(c == null){
			return;
		}
		if(TypeEnum.getIndex(c) != null || registrySet.contains(c)){
			return;
		}
		try{
			CodecUtils.checkAndRegistryClass(c, new HashSet<Class<?>>());
		} catch(Exception e){
			//logger.error("", e);
			throw new JSFCodecException(e.getMessage(), e.getCause());
		}
	}
	
	@Override
	public <T> byte[] write(T v) throws IOException {
		if(v != null){
			registry(v.getClass());
		}
		return super.write(v);
	}
	
	@Override
	public <T> void write(OutputStream out, T v) throws IOException {
		if(v != null){
			registry(v.getClass());
		}
		super.write(out, v);
	}	

	/**
	 * 重写此方法是为了解决反序列化Map，List的所有子类使用JSFMapTemplate，JSFListTemplate时，
	 * 返回的实例和要求的实例类型不一致的问题
	 */
	@Override
	public <T> T read(byte[] bytes, Class<T> c) throws IOException {
		if(c == null){
			logger.error("The serializered type is null.");
			throw new NullPointerException("The serializered type must be not null.");
		}
		registry(c);
		T v = null;
		try {
            v = c.newInstance();
			return super.read(bytes, v);
       	} catch (InstantiationException e) {
            v = null;
        } catch (IllegalAccessException e) {
			v = null;
		} catch (MessageTypeException e){
			v = null;
			throw new JSFCodecException("The " + c.getCanonicalName() + " can not be deserialized." +
					" Please check definition of your class", e);
		}
		return super.read(bytes, c);
	}
	
	@Override
	public <T> T read(byte[] bytes, T v) throws IOException {
		if(v == null){
			logger.error("The serializered type is null.");
			throw new NullPointerException("The serializered type must be not null.");
		}
		registry(v.getClass());
		try {
			return super.read(bytes, v);
		} catch (MessageTypeException e) {
			throw new JSFCodecException("The " + v.getClass().getCanonicalName() + " can not be deserialized." +
					" Please check definition of your class", e);
		}
	}
	
	/**
	 * 检查并注册模板
	 * @param v
	 */
	@SuppressWarnings("unchecked")
	public static <T> void checkAndRegistry(T v){
		if(v == null){
			return;
		}
		Class<?> clazz = null;
		Object obj = null;
		//先实例化
		if(v instanceof Class<?>){
			clazz = (Class<?>)v;
		} else {
			clazz = v.getClass();
			obj = v;
		}
		if(TypeEnum.getIndex(clazz) == null && !registrySet.contains(clazz)){
			if(!clazz.isEnum()){
				try {
					obj = ClassLoaderUtils.newInstance(clazz);
	//				obj = clazz.newInstance();
				} catch (Exception e) {
					//忽略异常
					logger.warn("newInstance:[" + clazz.getCanonicalName() + "] can not be instance!");
				}
			}
			if((obj != null && obj instanceof Map) || (obj == null && Map.class.isAssignableFrom(clazz))){
				registry.register(clazz, JSFMapTemplate.getInstance());
			} else if((obj != null && obj instanceof List) || (obj == null && List.class.isAssignableFrom(clazz))){
				registry.register(clazz, JSFListTemplate.getInstance());
			} else if((obj != null && obj instanceof Set) || (obj == null && Set.class.isAssignableFrom(clazz))){
				registry.register(clazz, JSFSetTemplate.getInstance());
			} else if((obj != null && (obj instanceof Exception || v  instanceof Error)) ||
					(obj == null && (Exception.class.isAssignableFrom(clazz) || Error.class.isAssignableFrom(clazz)))){
				registry.register(clazz, JSFExceptionTemplate.getInstance());
			} else {
//				try{
				registry.register(clazz);
//				} catch(MessageTypeException e){
//					logger.warn("checkAndRegistry:{}", e.getMessage());
//				}
			}
			registrySet.add(clazz);
		}
	}
	
	public static boolean checkRegistry(Class<?> clazz){
		if(TypeEnum.getIndex(clazz) != null || registrySet.contains(clazz)){
			return true;
		}
		return false;
	}
}