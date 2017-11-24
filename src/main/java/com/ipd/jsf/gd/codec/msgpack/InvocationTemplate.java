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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.StringTemplate;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * Created on 14-4-22.
 */
public class InvocationTemplate extends JSFAbstractTemplate<Invocation> {

    private final static Logger logger = LoggerFactory.getLogger(InvocationTemplate.class);

    private JSFMapTemplate mapTemplate = JSFMapTemplate.getInstance();


    @SuppressWarnings("unchecked")
	@Override
    public void write(Packer pk, Invocation v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                throw new NullPointerException();
            }
            pk.writeNil();
            return;
        }
        // 有异常先报
        if((v.getArgsType() == null && v.getArgs() != null) || (v.getArgsType() != null && v.getArgs() == null) ||
        		(v.getArgsType() != null && v.getArgs() != null && v.getArgsType().length != v.getArgs().length)){
        	logger.error("Invocation data error for argsType.length != argsObj.length");
        	throw new RuntimeException("Invocation data error for argsType.length != argsObj.length");
        }

        pk.writeArrayBegin(7);
        {
        	// 参数类型和实际值类型不匹配的记录
        	String argTypes[] = v.getArgsType();
        	Object args[] = v.getArgs();
        	Map<String, String> noMatchMap = this.checkArgMatch(v, args);
        	mapTemplate.write(pk, noMatchMap);

			String ifaceId = v.getIfaceId();
			if(ifaceId != null) {
				pk.write(ifaceId);
			} else {
				pk.write(v.getClazzName());
			}
            pk.write(v.getAlias());
            pk.write(v.getMethodName());
            // 参数类型
            if(argTypes == null || ifaceId != null){
            	pk.writeNil();
            } else {
	            Integer strArrSize = argTypes.length;
	            pk.writeArrayBegin(strArrSize);
                for(String type : argTypes){
	                  pk.write(type);
	            }
	            pk.writeArrayEnd();
			}
            // 参数实例
            if(v.getArgs() == null){
            	pk.writeNil();
            } else {
            	int strArrSize = args.length;
            	pk.writeArrayBegin(strArrSize);
                for(int i=0; i < strArrSize; i++){
                	String realType = noMatchMap.get(String.valueOf(i));
                	Object obj = args[i];
                	if(realType != null){
                		Class<?> realClass = obj.getClass();
                		if(!JSFMsgPack.checkRegistry(realClass)){
                			CodecUtils.checkAndRegistryClass(realClass, new HashSet<Class<?>>());
                		}
                	}
                    pk.write(obj);
                }
                pk.writeArrayEnd();
            }
            Map<String,Object> attachMap = v.getAttachments();
            mapTemplate.write(pk,attachMap);
		}
        pk.writeArrayEnd();
	}

    @SuppressWarnings("unchecked")
	@Override
    public Invocation read(Unpacker u, Invocation to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        if (to == null) {
            to = new Invocation();
        }
        u.readArrayBegin();
        {
        	Map<String, String> noMatchMap = mapTemplate.read(u, null, false);

			String classNameOrIfaceId = StringTemplate.getInstance().read(u, null);
			String className = JSFContext.getClassNameByIfaceId(classNameOrIfaceId);
			if (className != null) {
				to.setIfaceId(classNameOrIfaceId);
				to.setClazzName(className); // 实际类名
			} else {
				className = classNameOrIfaceId;
				to.setClazzName(classNameOrIfaceId);
			}

            String alias = StringTemplate.getInstance().read(u, null);
            String methodName = StringTemplate.getInstance().read(u, null);
            to.setAlias(alias);
            to.setMethodName(methodName);
            try {
                // 参数类型
                Class[] argsClass= null;
                String[] argType = null;
                if(u.trySkipNil()){
                    // 从缓存直接取
                    if (StringUtils.isNotEmpty(className) && StringUtils.isNotEmpty(methodName)) {
                        argsClass = ReflectUtils.getMethodArgsType(className, methodName);
                        if (argsClass == null) {
                            throw new JSFCodecException("Can not found argTypes of " + className + "."
                                    + methodName + ", may be no such method in provider.");
                        }
                        argType = ClassTypeUtils.getTypeStrs(argsClass);
                    }
                } else {
                    int arrSize1 = u.readArrayBegin();
                    argType = new String[arrSize1];
                    for(int i=0;i<arrSize1;i++){
                        argType[i] = u.readString();
                    }
                    u.readArrayEnd();
                }

                // 参数实例
                Object[] objArr;
                if(u.trySkipNil()){
                    objArr = null;
                } else {
                    int arrSize2 = u.readArrayBegin();
                    objArr = new Object[arrSize2];
                    for(int j = 0; j<arrSize2; j++){
                        String realType = noMatchMap.get(String.valueOf(j));
                        if(realType == null){
                            realType = argType[j];
                        }
                        Class<?> clazz = ClassTypeUtils.getClass(realType);
                        if(!JSFMsgPack.checkRegistry(clazz)){
                            CodecUtils.checkAndRegistryClass(clazz, new HashSet<Class<?>>());
                        }
                        if(TypeEnum.getIndex(clazz) == null){
                            try{
                                objArr[j] = u.read(ClassLoaderUtils.newInstance(clazz));
                            } catch(Exception e){
                                if(!(e instanceof JSFCodecException)) {
                                    objArr[j] = u.read(clazz);
                                } else {
                                    throw (JSFCodecException) e;
                                }
                            }
                            continue;
                        }
                        objArr[j] = u.read(clazz);
                    }
                    u.readArrayEnd();
                }
                Map<String,Object> map = (Map<String,Object>)u.read(mapTemplate);
                to.setArgsType(argType);
                to.setArgs(objArr);
                to.addAttachments(map);
            } catch (JSFCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new JSFCodecException("Failed to decode invocation of " + className + "." + methodName
                        + "/" + alias + ", cause by : " + e.getMessage(), e);
            }
        }

        u.readArrayEnd();
        return to;
    }

    private Map<String,String> checkArgMatch(Invocation v, Object[] args){
    	Map<String, String> result = new HashMap<String, String>();
    	String argsType[] = v.getArgsType();
    	if(argsType == null || argsType.length == 0){
    		return result;
    	}
    	Class<?> argClasses[] = v.getArgClasses();
    	for(int i = 0; i < argsType.length; i++){
    		if(args[i] == null || (argClasses != null && (argClasses[i].isPrimitive() || Modifier.isFinal(argClasses[i].getModifiers())))){
    			continue;
    		}
    		Class<?> realClass = args[i].getClass();
    		if(!argClasses[i].equals(realClass)){
        		String argType = ClassTypeUtils.getTypeStr(realClass);
    			result.put(String.valueOf(i), argType);
    		}
    	}
    	return result;
    }

    private InvocationTemplate(){}

    private static InvocationTemplate instance = new InvocationTemplate();

    public static InvocationTemplate getInstance(){
    	return instance;
    }
}