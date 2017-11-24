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
package com.ipd.jsf.gd.server.telnet;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.TelnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;



/**
 * 取得指定接口服务的信息
 *
 */
public class ServiceInfoTelnetHandler implements TelnetHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(ServiceInfoTelnetHandler.class);

	/* (non-Javadoc)
	 * @see TelnetHandler#getCommand()
	 */
	@Override
	public String getCommand() {
		return "info";
	}

	/* (non-Javadoc)
	 * @see TelnetHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Get the interface-service info.Excemple: info xxx.xxx [methodName]";
	}

	/* (non-Javadoc)
	 * @see TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
	 */
	@Override
	public String telnet(Channel channel, String message) {
		String params[] = message.replaceAll("\\s+", " ").split("\\s");
		String serviceName = null;
		List<ProviderConfig> providerList = JSFContext.getProviderConfigs();
		for(ProviderConfig config : providerList){
			String ref = config.getInterfaceId().trim();
			if(params[0].equals(ref)){
				serviceName = ref;
				break;
			}
		}

		if(serviceName == null){
			logger.info("The service [" + params[0] + " ] is not exists!");
			return "{\"error\":\"This service is not exists!\"}";
		}
		Class clazz = null;
		try {
			clazz = Class.forName(params[0]);
		} catch(Exception e){
			logger.error("", e);
			return "{\"error\":\"This service's class init error!\"}";
		}

		if(params.length > 1){
			return this.getMethodInfo(clazz, params[1]);
		}

		StringBuilder result = new StringBuilder();
		result.append("{");
		result.append("\"serviceName\":\"").append(clazz.getCanonicalName()).append("\",");
		Method methods[] = clazz.getDeclaredMethods();
		result.append("\"methods\":[");
		for(Method method : methods){
			result.append("{\"methodName\":").append("\"").append(method.getName()).append("\",");
			Class exceptions[] = method.getExceptionTypes();
			if(exceptions.length > 0){
				result.append("\"exceptions\":").append("\"");
				for(Class exception : exceptions){
					result.append(exception.getCanonicalName()).append(",");
				}
				result.delete(result.length() - 1, result.length());
				result.append("\",");
			}
			result.append("\"returnType\":");
			//返回值类型
			Type returnType = method.getGenericReturnType();
			if(returnType instanceof Class){
				Class returnClass = (Class)returnType;
				result.append("\"").append(returnClass.getCanonicalName()).append("\"");
				if(returnClass.isArray()){
					Class comClass = returnClass.getComponentType();
					while(comClass.isArray()){
						comClass = comClass.getComponentType();
					}
					TelnetUtils.scanParameter(comClass, new HashSet(), result);
				} else {
					TelnetUtils.scanParameter(returnClass, new HashSet(), result);
				}
			} else if(returnType instanceof GenericArrayType){
				String typeStr = returnType.toString();
				result.append(typeStr).append("\"");
				GenericArrayType compsType = (GenericArrayType)returnType;
				Type realType = compsType.getGenericComponentType();
				//直到不是数组类型时
				if(realType instanceof GenericArrayType){
					while(realType instanceof GenericArrayType){
						realType = ((GenericArrayType)realType).getGenericComponentType();
					}
				}
				if(realType instanceof Class){
					Class realClass = (Class)realType;
					TelnetUtils.scanParameter(realClass, new HashSet(), result);
				} else if(realType instanceof ParameterizedType){
					String realStr = realType.toString();
					TelnetUtils.getGenericField(realStr, result, new HashSet());
				}
			} else {
				String typeStr = returnType.toString();
				result.append("\"").append(typeStr).append("\"");
				TelnetUtils.getGenericField(typeStr, result, new HashSet());
			}
			
			result.append(",\"parameters\":[");
			Type paramsType[] = method.getGenericParameterTypes();
			
			int i = 1;
			for(Type param : paramsType){
				result.append("{\"param").append(i).append("\":\"");
				if(param instanceof Class){
					Class paramClass = (Class)param;
					result.append(paramClass.getCanonicalName()).append("\"");
					if(paramClass.isArray()){
						Class comClass = paramClass.getComponentType();
						while(comClass.isArray()){
							comClass = comClass.getComponentType();
						}
						TelnetUtils.scanParameter(comClass, new HashSet(), result);
					} else {
						TelnetUtils.scanParameter(paramClass, new HashSet(), result);
					}
				} else if(param instanceof GenericArrayType) {
					String typeStr = param.toString();
					result.append(typeStr).append("\"");
					GenericArrayType compsType = (GenericArrayType) param;
					Type realType = compsType.getGenericComponentType();
					//直到不是数组类型时
					if (realType instanceof GenericArrayType) {
						while (realType instanceof GenericArrayType) {
							realType = ((GenericArrayType) realType).getGenericComponentType();
						}
					}
					if (realType instanceof Class) {
						Class realClass = (Class) realType;
						TelnetUtils.scanParameter(realClass, new HashSet(), result);
					} else if (realType instanceof ParameterizedType) {
						String realStr = realType.toString();
						TelnetUtils.getGenericField(realStr, result, new HashSet());
					}
				} else {
					String typeStr = param.toString();
					result.append(typeStr).append("\"");
					TelnetUtils.getGenericField(typeStr, result, new HashSet());
				}
				result.append("},");
				i++;
			}
			if(i > 1){
				result.delete(result.length() - 1, result.length());
			}
			result.append("]},");
		}
		result.delete(result.length() - 1, result.length());
		result.append("]}");
		return result.toString();
	}


	private String getMethodInfo(Class clazz, String methodName){
		Method methods[] = clazz.getDeclaredMethods();
		Method method = null;
		for(Method m : methods){
			if(m.getName().equals(methodName)){
				method = m;
			}
		}
		if(method == null){
			return "{\"error\":\"The method ["+methodName+"] is not exists!\"}";
		}
		StringBuilder result = new StringBuilder();
		result.append("{\"methodName\":\"").append(methodName).append("\",");
		Class exceptions[] = method.getExceptionTypes();
		if(exceptions.length > 0){
			result.append("\"exceptions\":").append("\"");
			for(Class exception : exceptions){
				result.append(exception.getCanonicalName()).append(",");
			}
			result.delete(result.length() - 1, result.length());
			result.append("\",");
		}
		result.append("\"returnType\":");
		//返回值类型
		Type returnType = method.getGenericReturnType();
		if(returnType instanceof Class){
			Class returnClass = (Class)returnType;
			result.append("\"").append(returnClass.getCanonicalName()).append("\"");
			if(returnClass.isArray()){
				Class comClass = returnClass.getComponentType();
				while(comClass.isArray()){
					comClass = comClass.getComponentType();
				}
				TelnetUtils.scanParameter(comClass, new HashSet(), result);
			} else {
				TelnetUtils.scanParameter(returnClass, new HashSet(), result);
			}
		} else if(returnType instanceof GenericArrayType){
			String typeStr = returnType.toString();
			result.append(typeStr).append("\"");
			GenericArrayType compsType = (GenericArrayType)returnType;
			Type realType = compsType.getGenericComponentType();
			//直到不是数组类型时
			if(realType instanceof GenericArrayType){
				while(realType instanceof GenericArrayType){
					realType = ((GenericArrayType)realType).getGenericComponentType();
				}
			}
			if(realType instanceof Class){
				Class realClass = (Class)realType;
				TelnetUtils.scanParameter(realClass, new HashSet(), result);
			} else if(realType instanceof ParameterizedType){
				String realStr = realType.toString();
				TelnetUtils.getGenericField(realStr, result, new HashSet());
			}
		} else {
			String typeStr = returnType.toString();
			result.append("\"").append(typeStr).append("\"");
			TelnetUtils.getGenericField(typeStr, result, new HashSet());
		}

		result.append(",\"parameters\":[");
		Type paramsType[] = method.getGenericParameterTypes();

		int i = 1;
		for(Type param : paramsType){
			result.append("{\"param").append(i).append("\":\"");
			if(param instanceof Class){
				Class paramClass = (Class)param;
				result.append(paramClass.getCanonicalName()).append("\"");
				if(paramClass.isArray()){
					Class comClass = paramClass.getComponentType();
					while(comClass.isArray()){
						comClass = comClass.getComponentType();
					}
					TelnetUtils.scanParameter(comClass, new HashSet(), result);
				}else {
					TelnetUtils.scanParameter(paramClass, new HashSet(), result);
				}
			} else if(param instanceof GenericArrayType) {
				String typeStr = param.toString();
				result.append(typeStr).append("\"");
				GenericArrayType compsType = (GenericArrayType) param;
				Type realType = compsType.getGenericComponentType();
				//直到不是数组类型时
				if (realType instanceof GenericArrayType) {
					while (realType instanceof GenericArrayType) {
						realType = ((GenericArrayType) realType).getGenericComponentType();
					}
				}
				if (realType instanceof Class) {
					Class realClass = (Class) realType;
					TelnetUtils.scanParameter(realClass, new HashSet(), result);
				} else if (realType instanceof ParameterizedType) {
					String realStr = realType.toString();
					TelnetUtils.getGenericField(realStr, result, new HashSet());
				}
			} else {
				String typeStr = param.toString();
				result.append(typeStr).append("\"");
				TelnetUtils.getGenericField(typeStr, result, new HashSet());
			}
			result.append("},");
			i++;
		}
		if(i > 1){
			result.delete(result.length() - 1, result.length());
		}
		result.append("]}");
		return result.toString();

	}
}