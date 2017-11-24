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
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFLogicSwitch;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.org.msgpack.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title: Msgpack工具类<br>
 * <p/>
 * Description: 初始化一些模板，封装code方法<br>
 * <p/>
 */
public class MsgpackUtil {

    private static Logger logger = LoggerFactory.getLogger(MsgpackUtil.class);
    private static JSFMsgPack msgpack = new JSFMsgPack();
    private static Map<String, Class<?>> classMap = new ConcurrentHashMap<String, Class<?>>();
    private static Map<String, Template> templateMap = new ConcurrentHashMap<String, Template>();
    private volatile static boolean init = false;

    static {
        //templateMap.put(Constants.KEYMAPTEMPLATE, new KeyMapTemplate());
        //templateMap.put("Map<T,Object>", JSFMapTemplate.getInstance());
        templateMap.put(Invocation.class.getCanonicalName(), InvocationTemplate.getInstance());
        templateMap.put(ResponseMessage.class.getCanonicalName(), new ResponseTemplate());

    }

    private static MsgpackUtil msgpackUtil = new MsgpackUtil();

    private MsgpackUtil() {

    }

    public static void registerClass(Class<?> clazz) {
        String className = clazz.getName();
        if (classMap.containsKey(className)) {
            Class clazz1 = classMap.get(className);
            logger.error("class have be Register:className {} class:{}", className, clazz1);
            return;
        }
        JSFMsgPack.registry(clazz);
//      if(clazz.equals(HashMap.class)){
//          Templates.
//          Template template = Templates.tMap(Templates.TString,Object);
//          msgpack.register(clazz,template);
//      } else{
//          msgpack.register(clazz);
//      }
        classMap.put(className, clazz);
    }

    public static MsgpackUtil getIns() {
        return msgpackUtil;
    }

    public JSFMsgPack getMsgPack() {
        return msgpack;
    }

    /*
     * before write check the template
     */
    public byte[] write(Object obj) {

        String className = obj.getClass().getCanonicalName();
        Template template = templateMap.get(className);
        byte[] result = null;
        try {
            if (template != null) {
                result = msgpack.write(obj, template);
            } else {
                result = msgpack.write(obj);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JSFCodecException("Fail to encode " + className, e);
        }
        return result;
    }

    public byte[] write(Object obj, Template template) {
        byte[] result = null;
        try {
            result = msgpack.write(obj, template);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JSFCodecException("Fail to encode " + obj.getClass().getCanonicalName(), e);
        }
        return result;
    }

    /*
     *
     */
    public byte[] write(Object obj, String classTypeName) {
        Template template = getTemplate(classTypeName);
        byte[] result = write(obj, template);
        return result;
    }

    public <T> T read(byte[] bytes, Class<T> clazz) {
        T result = null;
        try {
            result = msgpack.read(bytes, clazz);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JSFCodecException("Fail to decode by class " + clazz.getCanonicalName(), e);
        }
        return result;
    }

    public <T> T read(byte[] bytes, Template template) {
        T result = null;
        try {
            result = (T) msgpack.read(bytes, template);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JSFCodecException("Fail to decode by template " + template, e);
        }
        return result;
    }


    public void registerTemplate(Class<?> type, Template template) {
        msgpack.register(type, template);

    }


    public Object read(byte[] bytes, String className) {
        Class clazz = classMap.get(className);
        return read(bytes, clazz);
    }


    public Template getTemplate(String clazzTypeName) {
        return templateMap.get(clazzTypeName);
    }

    /**
     * 是否开启循环依赖检查
     *
     * @return 是否开启
     */
    public static boolean openRefCheck() {
        if (!JSFLogicSwitch.SETTING_SERIALIZE_CHECK_REFERENCE) {
            return false; // 主动关闭的
        }
        Byte srcLan = (Byte) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE);
        if (srcLan != null && srcLan > 0) {
            return false; // 跨语言的
        }
        Short versionObj = (Short) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
        if (versionObj == null || versionObj < 1500) {
            return false; // 低版本的
        }
        return true;
    }

    /**
     * 是否开启循环依赖检查
     *
     * @param isPack
     * @return 是否开启
     */
    public static boolean openRefCheck(boolean isPack) {
        if (isPack && !JSFLogicSwitch.SETTING_SERIALIZE_CHECK_REFERENCE) {
            return false; // 主动关闭的
        }
        Byte srcLan = (Byte) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_LANGUAGE);
        if (srcLan != null && srcLan > 0) {
            return false; // 跨语言的
        }
        Short versionObj = (Short) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_JSF_VERSION);
        if (versionObj == null || versionObj < 1500) {
            return false; // 低版本的
        }
        return true;
    }

}