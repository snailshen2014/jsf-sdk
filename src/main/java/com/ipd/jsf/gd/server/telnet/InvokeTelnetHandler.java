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

import com.ipd.fastjson.JSON;
import com.ipd.fastjson.serializer.SerializerFeature;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.server.Invoker;
import com.ipd.jsf.gd.server.TelnetChannelHandler;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.CryptUtils;
import com.ipd.jsf.gd.util.ExceptionUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.PojoUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class InvokeTelnetHandler implements TelnetHandler {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(InvokeTelnetHandler.class);

	@Override
	public String getCommand() {
		return "invoke";
	}

	@Override
	public String getDescription() {
        return "Invoke the specified method. (Before invoke need superuser's role)" + line +
                "Usage:\tinvoke com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})" + line +
                "\tinvoke -p password com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})" + line +
                "\tinvoke -p password -t token com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})" + line;
	}

	@Override
	public String telnet(Channel channel, String message) {
        try {
            if (message == null || message.length() == 0) {
                return getDescription();
            }
            StringBuilder buf = new StringBuilder();

            // 解析密码
            String password = null;
            boolean isGlobal = false;
            String requestStr = message; // 请求数据部分 com.xxx.XxxService.xxxMethod(args)
            String interfaceId;
            String methodName;
            String token = null;

            while (requestStr.startsWith("-")) {
                if (requestStr.startsWith("-g")) {
                    String tmp = requestStr.substring(2).trim();
                    int index = tmp.indexOf(" ");
                    password = tmp.substring(0, index);
                    isGlobal= true;
                    requestStr = tmp.substring(index + 1);
                } else if (requestStr.startsWith("-p")) {
                    String tmp = requestStr.substring(2).trim();
                    int index = tmp.indexOf(" ");
                    password = tmp.substring(0, index);
                    requestStr = tmp.substring(index + 1);
                } else if (requestStr.startsWith("-t")) {
                    String tmp = requestStr.substring(2).trim();
                    int index = tmp.indexOf(" ");
                    token = tmp.substring(0, index);
                    requestStr = tmp.substring(index + 1);
                } else {
                    String tmp = requestStr.substring(2).trim();
                    int index = tmp.indexOf(" ");
                    //token = tmp.substring(0, index);
                    requestStr = tmp.substring(index + 1);
                }
                requestStr = requestStr.trim();
            }

            // 解析接口和方法
            int i = requestStr.indexOf("(");
            if (i < 0 || ! requestStr.endsWith(")")) {
                return "Invalid parameters, format: service.method(args)";
            }
            String serviceAndmethod = requestStr.substring(0, i).trim();
            String args = requestStr.substring(i + 1, requestStr.length() - 1).trim();
            i = serviceAndmethod.lastIndexOf(".");
            if (i >= 0) {
                interfaceId = serviceAndmethod.substring(0, i).trim();
                methodName = serviceAndmethod.substring(i + 1).trim();
            } else {
                return "Invalid parameters, format: service.method(args)";
            }
            //LOGGER.info("{},{},{},{},{}", new Object[]{password, isGlobal, token, interfaceId, methodName});
            String registryAddress = (String) JSFContext.get(JSFContext.KEY_REGISTRY_CONFIG);
            // 判断调用地址黑白名单
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            String remoteIp = NetUtils.toIpString(address);
            if (!NetUtils.isLocalHost(remoteIp) // 本机地址可以直接调用
                    && StringUtils.isNotEmpty(registryAddress)) { // 连上了注册中心
                String whitelist = JSFContext.getGlobalVal(Constants.SETTING_SERVER_SUDO_WHITELIST, null);
                if (!NetUtils.isMatchIPByPattern(whitelist, remoteIp)) {
                    return "Remote ip " + remoteIp + " is not in invoke whitelist";
                }
                // 此处验证密码 设置过sudo passwd可以调用
                boolean canInvoke = TelnetChannelHandler.ALLOW_INVOKE_CHANNELS.contains(channel);
                if (!canInvoke) {
                    if (password == null) {
                        return "Password is null, please set it by \"invoke -p password \"";
                    }
                    String invokepasswd = null; // 注册中心配的密码
                    if (!isGlobal) { // 取接口密码
                        invokepasswd = JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_INVOKE_TOKEN, null);
                    } else { // 取全局密码
                        invokepasswd = JSFContext.getGlobalVal(Constants.SETTING_SERVER_SUDO_PASSWD, null);
                    }
                    if (invokepasswd == null) { // 没设置密码不让调用
                        return "please set password at jsf administrator website.";
                    } else {
                        try {
                            invokepasswd = CryptUtils.decrypt(invokepasswd); // 需要先解密密码
                            if (!password.equals(invokepasswd)) { // 传的密码和密码不匹配
                                return "Wrong password [" + password + "], please check it";
                            }
                        } catch (Exception e) {
                            return ExceptionUtils.toString(e);
                        }
                    }
                }
            }

            Object[] paramArgs; // 参数列表
            try {
                paramArgs = JsonUtils.parseObject("[" + args + "]", Object[].class);
            } catch (Throwable t) {
                return "Invalid json argument, cause: " + t.getMessage();
            }

            Method invokeMethod = null; // 方法
            Invoker invoker = null;  // 找代理
            for (Map.Entry<String,Invoker> entry: BaseServerHandler.getAllInvoker().entrySet()) {
                String key = entry.getKey();
                if (key.equals(interfaceId) || key.startsWith(interfaceId + "/")) {
                    invoker = entry.getValue();
                    break;
                }
            }
            if(invoker == null){
                return "Not found such exported service !";
            } else {
                // 找方法
                Class clazz = ClassTypeUtils.getClass(interfaceId);
                invokeMethod = findMethod(clazz, methodName, paramArgs);

                if (invokeMethod != null) {
                    try {
                        Object[] array = PojoUtils.realize(paramArgs, invokeMethod.getParameterTypes());

                        long start = System.currentTimeMillis();
                        RequestMessage request = MessageBuilder.buildRequest(clazz, methodName,
                                invokeMethod.getParameterTypes(), array);
                        Invocation invocation = request.getInvocationBody();
                        invocation.addAttachment(Constants.INTERNAL_KEY_REMOTE, channel.remoteAddress());
                        invocation.addAttachment(Constants.INTERNAL_KEY_LOCAL, channel.localAddress());
                        invocation.addAttachment(".telnet", true);
                        if (token != null) {
                            invocation.addAttachment(Constants.HIDDEN_KEY_TOKEN, token);
                        }
                        ResponseMessage response = invoker.invoke(request);
                        long end = System.currentTimeMillis();
                        if(response.isError()) {
                            Throwable e = response.getException();
                            LOGGER.error("error when telnet invoke", e);
                            buf.append(ExceptionUtils.toString(e));
                        } else {
                            buf.append(JSON.toJSONString(response.getResponse(), SerializerFeature.WriteDateUseDateFormat));
                        }

                        buf.append("\r\nelapsed: ");
                        buf.append(end - start);
                        buf.append(" ms.");
                    } catch (Throwable t) {
                        LOGGER.error("error when telnet invoke", t);
                        return "Failed to invoke method " + invokeMethod.getName() + ", cause: " + ExceptionUtils.toString(t);
                    }
                } else {
                    buf.append("No such method " + methodName + " in interface " + interfaceId);
                }
            }

            return buf.toString();
        } catch (Exception e) {
            return ExceptionUtils.toString(e);
        }
	}


    private static Method findMethod(Class service, String methodName, Object[] args) {
        Method[] methods = service.getMethods();
        Method invokeMethod = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName) && m.getParameterTypes().length == args.length) {
                if (invokeMethod != null) { // 重载
                    if (isMatch(invokeMethod.getParameterTypes(), args)) {
                        invokeMethod = m;
                        break;
                    }
                } else {
                    invokeMethod = m;
                }
            }
        }
        return invokeMethod;
    }

    private static boolean isMatch(Class<?>[] types, Object[] args) {
        if (types.length != args.length) {
            return false;
        }
        for (int i = 0; i < types.length; i ++) {
            Class<?> type = types[i];
            Object arg = args[i];
            if (ReflectUtils.isPrimitives(arg.getClass())) {
                if (!ReflectUtils.isPrimitives(type)) {
                    return false;
                }
            } else if (arg instanceof Map) {
                String name = (String) ((Map<?, ?>)arg).get("class");
                Class<?> cls = arg.getClass();
                if (name != null && name.length() > 0) {
                    cls = ReflectUtils.forName(name);
                }
                if (! type.isAssignableFrom(cls)) {
                    return false;
                }
            } else if (arg instanceof Collection) {
                if (! type.isArray() && ! type.isAssignableFrom(arg.getClass())) {
                    return false;
                }
            } else {
                if (! type.isAssignableFrom(arg.getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

}