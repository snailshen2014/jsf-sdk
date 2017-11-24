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
package com.ipd.jsf.gd.codec.protobuf;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.LRUHashMap;
import com.ipd.jsf.gd.codec.hessian.UnsafeByteArrayOutputStream;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: Protobuf协议序列化<br>
 * <p/>
 * Description: boolean/string/int/long/double/byte/byte[]/struct（生成的自定义对象）<br>
 * <p/>
 */
public class ProtobufCodec implements Codec {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProtobufCodec.class);

    @Override
    public Object decode(byte[] datas, Class clazz) {
        Object result;
        try {
            CodedInputStream code = CodedInputStream.newInstance(datas);
            if (clazz == Invocation.class) {
                result = decodeInvocation(code);
            } else if (clazz == ResponseMessage.class) {
                result = decodeResponse(code);
            } else {
                throw new JSFCodecException("Unsupported encode class: " + clazz.getName());
            }
            return result;
        } catch (JSFCodecException e) {
            throw e;
        } catch (Exception e) {
            throw new JSFCodecException("Failed to decode bytes of class: " + clazz, e);
        }
    }

    @Override
    public Object decode(byte[] datas, String className) {
        return decode(datas, ClassTypeUtils.getClass(className));
    }

    @Override
    public byte[] encode(Object obj) {
        return encode(obj, ClassTypeUtils.getTypeStr(obj.getClass()));
    }

    @Override
    public byte[] encode(Object obj, String classTypeName) {
        try {
            UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
            CodedOutputStream code = CodedOutputStream.newInstance(out);

            if (obj instanceof Invocation) { // 请求特殊处理
                encodeInvocation((Invocation) obj, code);
            } else if (obj instanceof ResponseMessage) { // 响应特殊处理
                encodeResponse((ResponseMessage) obj, code);
            } else {
                throw new JSFCodecException("Unsupported encode type of class: "
                        + ClassTypeUtils.getTypeStr(obj.getClass()));
            }
            code.flush(); // 要flush下
            return out.toByteArray();
        } catch (JSFCodecException e) {
            throw e;
        } catch (Exception e) {
            throw new JSFCodecException("Failed to encode bytes of class: " + classTypeName, e);
        }
    }

    /**
     * Invocation特殊处理
     *
     * @param invocation 请求对象
     * @param code       输出流
     */
    protected void encodeInvocation(Invocation invocation, CodedOutputStream code) throws Exception {
        // 转为rpc专用
        RpcTemplate.ProtobufInvocation.Builder builder = RpcTemplate.ProtobufInvocation.newBuilder()
                .setService(invocation.getClazzName()) // 接口名
                .setMethod(invocation.getMethodName()) // 方法名
                .setAlias(invocation.getAlias()); // 服务别名

        String[] argTypes = invocation.getArgsType(); // 参数类型
        Object[] args = invocation.getArgs();  // 参数值
        if (CommonUtils.isNotEmpty(argTypes)) {
            if (argTypes.length != 1 || args.length != 1) {
                throw new JSFCodecException("Parameter size of " + invocation.getClazzName()
                        + "." + invocation.getMethodName() + " must be one.");
            }
            Object arg = args[0];
            if (arg == null) { // 参数值 不能为空
                throw new JSFCodecException("Parameter[" + 0 + "] of " + invocation.getClazzName()
                        + "." + invocation.getMethodName() + " must be NOT null.");
            } else if (arg instanceof MessageLite) { // protobuf生成的自定义对象
                builder.setArgType(StringUtils.EMPTY); // 可以不传类型，
                builder.setArgData(((MessageLite) arg).toByteString());
            } else {
                throw new JSFCodecException("Unsupported class:" + ClassTypeUtils.getTypeStr(arg.getClass()) +
                        ", only support protobuf message");
            }
        }
        // 隐式传参
        Map<String, Object> attachments = invocation.getAttachments();
        if (CommonUtils.isNotEmpty(attachments)) {
            Map<String, String> tmp = new HashMap<String, String>();
            for (Map.Entry<String, Object> entry : attachments.entrySet()) {
                if (entry.getValue() instanceof String) {
                    tmp.put(entry.getKey(), (String) entry.getValue());
                }
            }
            builder.putAllAttachments(tmp);
        }
        // 写入流
        builder.build().writeTo(code);
    }


    /**
     * Invocation特殊处理
     *
     * @param code 输入流
     * @return 解析后的Invocation
     */
    protected Invocation decodeInvocation(CodedInputStream code) throws Exception {
        RpcTemplate.ProtobufInvocation protobufInvocation = RpcTemplate.ProtobufInvocation.parseFrom(code);

        Invocation invocation = new Invocation();
        invocation.setClazzName(protobufInvocation.getService());  // 接口名
        invocation.setMethodName(protobufInvocation.getMethod()); // 方法名
        invocation.setAlias(protobufInvocation.getAlias());  // 服务别名

        String argType = protobufInvocation.getArgType(); // 参数类型先读长度，再挨个读string
        Class argClass;
        if (StringUtils.isEmpty(argType)) {
            Class[] argsClasses = ReflectUtils.getMethodArgsType(invocation.getClazzName(), invocation.getMethodName());
            if (argsClasses == null) {
                throw new JSFCodecException("Can not found argTypes of " + invocation.getClazzName() + "."
                        + invocation.getMethodName() + ", may be no such method in provider.");
            }
            invocation.setArgsType(argsClasses);
            argClass = argsClasses[0];
        } else {
            invocation.setArgsType(new String[]{argType});
            argClass = ClassTypeUtils.getClass(argType);
        }

        Object arg = readFromByteString(argClass, protobufInvocation.getArgData()); // 读二进制
        invocation.setArgs(new Object[]{arg});
        invocation.addAttachments((Map) protobufInvocation.getAttachments());
        return invocation;
    }

    /**
     * ResponseMessage特殊处理
     *
     * @param res  响应对象
     * @param code 输出流
     */
    private void encodeResponse(ResponseMessage res, CodedOutputStream code) throws Exception {
        if (!res.isHeartBeat()) {
            // encode response data or error message.
            Throwable th = res.getException();
            if (th == null) { // 无异常
                Object val = res.getResponse();
                if (val == null) { // 返回值不能为空
                    throw new JSFCodecException("Result of protobuf protocol must be NOT null.");
                }
                RpcTemplate.ProtobufResult.Value protobufValue = RpcTemplate.ProtobufResult.Value.newBuilder()
                        .setType(ClassTypeUtils.getTypeStr(val.getClass()))
                        .setData(((MessageLite) val).toByteString())
                        .build();
                RpcTemplate.ProtobufResult protobufResult = RpcTemplate.ProtobufResult.newBuilder().setValue(protobufValue)
                        .build();
                protobufResult.writeTo(code);
            } else {  // 有异常
                RpcTemplate.ProtobufResult.Exception protobufException = RpcTemplate.ProtobufResult.Exception.newBuilder()
                        .setType(ClassTypeUtils.getTypeStr(th.getClass()))
                        .setMsg(th.getMessage()).build();
                RpcTemplate.ProtobufResult protobufResult = RpcTemplate.ProtobufResult.newBuilder().setException(protobufException)
                        .build();
                protobufResult.writeTo(code);
            }
        }
    }

    /**
     * ResponseMessage特殊处理
     *
     * @param code
     * @return
     * @throws IOException
     */
    private ResponseMessage decodeResponse(CodedInputStream code) throws Exception {
        ResponseMessage res = new ResponseMessage();
        RpcTemplate.ProtobufResult result = RpcTemplate.ProtobufResult.parseFrom(code);
        RpcTemplate.ProtobufResult.Exception protobufException = result.getException();
        if (protobufException == null || !protobufException.isInitialized()) { // 无异常
            RpcTemplate.ProtobufResult.Value value = result.getValue();
            Class clazz = ClassTypeUtils.getClass(value.getType());
            if (MessageLite.class.isAssignableFrom(clazz)) { // protobuf生成的自定义对象
                Object tmp = readFromByteString(clazz, value.getData());
                res.setResponse(tmp);
            } else {
                throw new JSFCodecException("Unsupported class:" + value.getType() +
                        ", only support protobuf message");
            }
        } else { // 有异常
            Class clazz = ClassTypeUtils.getClass(protobufException.getType());
            String message = protobufException.getMsg();
            Throwable throwable = null;
            try {
                Constructor constructor = clazz.getDeclaredConstructor(String.class);
                if (!constructor.isAccessible()) {
                    try {
                        constructor.setAccessible(true);
                        throwable = (Throwable) constructor.newInstance(message);
                    } finally {
                        constructor.setAccessible(false);
                    }
                } else {
                    throwable = (Throwable) constructor.newInstance(message);
                }
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Failed to decode exception: " + clazz, e);
                }
                throwable = new RuntimeException(message);
            }
            res.setException(throwable);
        }
        return res;
    }

    // 方法缓存下。
    private Map<Class, Method> parseMethodMap = Collections.synchronizedMap(new LRUHashMap<Class, Method>(100));

    /**
     * 从ByteString读取对象
     * @param clazz 对象类
     * @param bytes ByteString对象
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private Object readFromByteString(Class clazz, ByteString bytes){
        try {
            Method method = parseMethodMap.get(clazz);
            if (method == null) {
                method = clazz.getMethod("parseFrom", ByteString.class);
                parseMethodMap.put(clazz, method);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                return method.invoke(null, bytes);
            } else {
                throw new JSFCodecException("Cannot found method " + ClassTypeUtils.getTypeStr(clazz)
                        + ".parseFrom(ByteString), please check the generated code");
            }
        } catch (Exception e) {
            throw new JSFCodecException("Cannot found method " + ClassTypeUtils.getTypeStr(clazz)
                    + ".parseFrom(ByteString), please check the generated code", e);
        }
    }
}