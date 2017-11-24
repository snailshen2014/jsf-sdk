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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.template.TemplateRegistry;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * 异常序列化模板类
 * 
 * 根据Field列表序列化和反序列化
 *
 * @param <T>
 */
public class JSFExceptionTemplate<T> extends AbstractTemplate<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(JSFExceptionTemplate.class);
	
	@SuppressWarnings("rawtypes")
	private static Map<Class<?>, Template> fieldTemplateCache = new ConcurrentHashMap<Class<?>, Template>();

	private static ConcurrentHashMap<Class, Constructor> constructorCache = new ConcurrentHashMap<Class, Constructor>();
	private static ConcurrentHashMap<Class, Object[]> constructorArgsCache = new ConcurrentHashMap<Class, Object[]>();
	
	private TemplateRegistry registry = JSFMsgPack.getTemplateRegistry();
	
	@SuppressWarnings("rawtypes")
	private static JSFExceptionTemplate instance = new JSFExceptionTemplate();
	
	private JSFExceptionTemplate(){}
	
	@SuppressWarnings("rawtypes")
	public static JSFExceptionTemplate getInstance(){
		return instance;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void write(Packer pk, T v, boolean required)
			throws IOException {
		if (v == null) {
			if (required) {
				logger.error("Attempted to write null");
				throw new MessageTypeException("Attempted to write null");
			}
			pk.writeNil();
			return;
		}
		Class<?> clazz = v.getClass();
		Field fields[] = CodecUtils.getFields(clazz);
		pk.writeArrayBegin(fields.length + 1);
		pk.write(ClassTypeUtils.getTypeStr(clazz));
		try {
			for(Field f : fields){
				Class<?> fclass = f.getType();
				Class<?> declareClass = f.getDeclaringClass();
				Template fieldTemp = null;
				Object value = readField(v, declareClass, f);
				if(fclass.isArray()){
					Object values[] = null;
					if("stackTrace".equals(f.getName())){
						Method method = declareClass.getDeclaredMethod("getStackTrace");
						Object obj = declareClass.cast(v);
						values = (Object[])method.invoke(obj);
					} else {
						values = (Object[])value;
					}
					if(values == null){
						pk.writeNil();
						continue;
					} else {
						pk.writeArrayBegin(values.length);
						for (Object o : values) {
							if (fieldTemp == null) {
								fieldTemp = this.registerAndGetTemplate(o.getClass());
							}
							fieldTemp.write(pk, o);
						}
					}
				} else {
					if(("cause".equals(f.getName()) && v == value) || fclass.isPrimitive() || value == null){
						pk.writeArrayBegin(1);
						if(fclass.isPrimitive()){
							fieldTemp = this.registerAndGetTemplate(fclass);
							fieldTemp.write(pk, value);
						} else {
							pk.writeNil();
						}
					} else {
						Class<?> valueClass = value.getClass();
						if(valueClass.getName().indexOf("$") > 0){
							pk.writeArrayBegin(0);
						} else {
							pk.writeArrayBegin(2);
							fieldTemp = this.registerAndGetTemplate(valueClass);
							pk.write(ClassTypeUtils.getTypeStr(valueClass));
							fieldTemp.write(pk, value);
						}
					}
				}
				pk.writeArrayEnd();
			}
		} catch(Exception e){
			logger.error("An exception occurred while serializing data: ", e);
			throw new MessageTypeException(clazz.getName() + ":", e);
		}
		pk.writeArrayEnd();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public T read(Unpacker u, T to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
			return null;
		}
		
		Class<?> clazz = null;
		int fieldLen = u.readArrayBegin() - 1;
		String className = u.readString();
		try {
			if(to !=null){
				if(!(to instanceof Throwable)){
					logger.error("[JSF-24002]Type mismatch, Target is not a Throwable but " + className);
					throw new MessageTypeException("[JSF-24002]Type mismatch, Target is not a Throwable but " + className);
				}
				if(!to.getClass().getCanonicalName().equals(className)){
					clazz = ClassTypeUtils.getClass(className);
					to = (T)newInstance(clazz);
				} else {
					clazz = to.getClass();
				}
			} else if(to == null){
				clazz = ClassTypeUtils.getClass(className);
				to = (T)newInstance(clazz);
			}

			Field fields[] = CodecUtils.getFields(clazz);
			int minLen = fieldLen > fields.length? fields.length : fieldLen;
			for(int k=0; k < minLen; k++){
				Field f = fields[k];
				Class<?> fclass = f.getType();
				Class<?> declareClass = f.getDeclaringClass();
				Template fieldTemp = null;
				if(fclass.isArray()){
					Object values[] = null;
					if (!u.trySkipNil()){
						Class<?> compentType = fclass.getComponentType();
						fieldTemp = this.registerAndGetTemplate(compentType);
						int len = u.readArrayBegin();
						values = (Object[]) Array.newInstance(compentType, len);
						for (int i = 0; i < len; i++) {
							if (Throwable.class.isAssignableFrom(compentType)) {
								values[i] = fieldTemp.read(u, newInstance(compentType));
							} else {
								try {
									values[i] = fieldTemp.read(u, ClassLoaderUtils.newInstance(compentType));
								} catch (InstantiationException e) {
									values[i] = fieldTemp.read(u, null);
								} catch (NoSuchMethodException e) {
									values[i] = fieldTemp.read(u, null);
								} catch (SecurityException e) {
									values[i] = fieldTemp.read(u, null);
								}
							}
						}
						u.readArrayEnd();
					}
					writePrivateField(to, declareClass, f, values);
				} else {
					int len = u.readArrayBegin();
					if(len == 2){
						String valueClassName = u.readString();
						Class<?> valueClass = ClassTypeUtils.getClass(valueClassName);
						fieldTemp = this.registerAndGetTemplate(valueClass);
						Object value = null;
						if(Throwable.class.isAssignableFrom(valueClass)) {
							value = fieldTemp.read(u, newInstance(valueClass));
						} else {
							try {
								value = fieldTemp.read(u, ClassLoaderUtils.newInstance(valueClass));
							} catch (InstantiationException e) {
								value = fieldTemp.read(u, null);
							}catch (NoSuchMethodException e) {
								value = fieldTemp.read(u, null);
							}catch (SecurityException e) {
								value = fieldTemp.read(u, null);
							}

						}
						writePrivateField(to, declareClass, f, value);
					} else if(len == 1) {
						if(fclass.isPrimitive()){
							fieldTemp = this.registerAndGetTemplate(fclass);
							Object value = fieldTemp.read(u, null);
							writePrivateField(to, declareClass, f, value);
						}
					}
					u.readArrayEnd();
				}
			}
		}catch(Exception e){
			logger.error("An exception occurred while serializing data:", e);
			throw new JSFCodecException(className + ":", e);
		}
		u.readArrayEnd();
		return to;
	}
	
    private void writePrivateField(Object target,
            Class<?> targetClass, Field field, Object value) {
        try {
        	if(!targetClass.equals(target.getClass())){
            	target = targetClass.cast(target);
            }
            field.set(target, value);
        } catch (Exception e) {
        	logger.error("", e);
            throw new MessageTypeException(e);
        }
    }

	
    private Object readField(Object target, Class<?> targetClass, Field field) {

        try {
            if(!targetClass.equals(target.getClass())){
            	target = targetClass.cast(target);
            }
            Object valueReference = field.get(target);
            return valueReference;
        } catch (Exception e) {
        	logger.error("", e);
            throw new MessageTypeException(e);
        }
    }

    /**
     * 取得模板
     * 取不到时注册
     * @param type
     * @return
     */
    @SuppressWarnings("rawtypes")
	private Template registerAndGetTemplate(Class<?> type){
    	Template temp = fieldTemplateCache.get(type);
    	if(temp != null){
    		return temp;
    	}
		try {
			temp = this.registry.lookup(type);
			fieldTemplateCache.put(type, temp);
		} catch(MessageTypeException ex1){
			try{
				type.asSubclass(Throwable.class);
				this.registry.register(type, this);
		   		fieldTemplateCache.put(type, this);
		   		return this;
			} catch(ClassCastException e) {
				JSFMsgPack.registry(type);
				temp = this.registry.lookup(type);
				fieldTemplateCache.put(type, temp);
			}
		}
		return temp;
     }

	private <T> T newInstance(Class<T> clazz) throws Exception{
		Constructor _constructor = constructorCache.get(clazz);
		Object[] _constructorArgs = constructorArgsCache.get(clazz);
		if(_constructor == null) {
			Constructor constructors[] = clazz.getDeclaredConstructors();
			long bestCost = Long.MAX_VALUE;

			for (int i = 0; i < constructors.length; i++) {
				Class param[] = constructors[i].getParameterTypes();
				long cost = 0;

				for (int j = 0; j < param.length; j++) {
					cost = 4 * cost;
					if (Object.class.equals(param[j]))
						cost += 1;
					else if (String.class.equals(param[j]))
						cost += 2;
					else if (int.class.equals(param[j]))
						cost += 3;
					else if (long.class.equals(param[j]))
						cost += 4;
					else if (param[j].isPrimitive())
						cost += 5;
					else
						cost += 6;
				}
				if (cost < 0 || cost > (1 << 48)) {
					cost = 1 << 48;
				}
				cost += (long) param.length << 48;
				if (cost < bestCost) {
					_constructor = constructors[i];
					bestCost = cost;
				}
			}

			if (_constructor != null) {
				_constructor.setAccessible(true);
				Class[] params = _constructor.getParameterTypes();
				_constructorArgs = new Object[params.length];
				for (int i = 0; i < params.length; i++) {
					_constructorArgs[i] = getParamArg(params[i]);
				}
				constructorCache.put(clazz, _constructor);
				constructorArgsCache.put(clazz, _constructorArgs);
			}
		}
		if (_constructor != null) {
			return (T) _constructor.newInstance(_constructorArgs);
		} else {
			return clazz.newInstance();
		}
	}

	private static Object getParamArg(Class cl) {
		if (! cl.isPrimitive())
			return null;
		else if (boolean.class.equals(cl))
			return Boolean.FALSE;
		else if (byte.class.equals(cl))
			return new Byte((byte) 0);
		else if (short.class.equals(cl))
			return new Short((short) 0);
		else if (char.class.equals(cl))
			return new Character((char) 0);
		else if (int.class.equals(cl))
			return Integer.valueOf(0);
		else if (long.class.equals(cl))
			return Long.valueOf(0);
		else if (float.class.equals(cl))
			return Float.valueOf(0);
		else if (double.class.equals(cl))
			return Double.valueOf(0);
		else
			throw new UnsupportedOperationException();
	}
}