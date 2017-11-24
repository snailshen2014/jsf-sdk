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
package com.ipd.jsf.gd.util;

import com.ipd.fastjson.parser.Feature;
import com.ipd.fastjson.serializer.SerializerFeature;

import java.util.HashSet;

/**
 * Title: <br>
 * <p/>
 * Description: 存储一些启动后的开关，加快访问速度，如果更新请自己调用更新<br>
 * <p/>
 */
public final class JSFLogicSwitch {

    /**
     * 全局参数 调用是否发送app信息，默认true
     *
     * @see Constants#SETTING_INVOKE_SEND_APP
     */
    public static boolean INVOKE_SEND_APP = true;

    /**
     * 全局参数，JSON序列化支持特性
     *
     * @see Constants#SETTING_JSON_SERIALIZER_FEATURES
     */
    public static SerializerFeature[] JSON_SERIALIZER_FEATURES;

    /**
     * 全局参数，JSON反序列化支持特性
     *
     * @see Constants#SETTING_JSON_PARSER_FEATURES
     */
    public static Feature[] JSON_PARSER_FEATURES;

    /**
     * 全局参数 分组调用是否批量注册，默认true
     *
     * @see Constants#SETTING_REGISTRY_REGISTER_BATCH
     */
    public static boolean REGISTRY_REGISTER_BATCH = true;

    /**
     * 全局参数 序列化是否检测Object的类型（父子类检查）
     *
     * @see Constants#SETTING_SERIALIZE_CHECK_CLASS
     */
    public static boolean SETTING_SERIALIZE_CHECK_CLASS = true;

    /**
     * 全局参数 序列化是否检测Object的类型（父子类检查）
     *
     * @see Constants#SETTING_SERIALIZE_CHECK_REFERENCE
     */
    public static boolean SETTING_SERIALIZE_CHECK_REFERENCE = true;

    static {
        loadFromContext();
    }

    protected static void loadFromContext() {
        String tmp;
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_INVOKE_SEND_APP, null)) != null) {
            INVOKE_SEND_APP = !CommonUtils.isFalse(tmp);
        }
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_REGISTER_BATCH, null)) != null) {
            REGISTRY_REGISTER_BATCH = !CommonUtils.isFalse(tmp);
        }
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_SERIALIZE_CHECK_CLASS, null)) != null) {
            SETTING_SERIALIZE_CHECK_CLASS = !CommonUtils.isFalse(tmp);
        }
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_SERIALIZE_CHECK_REFERENCE, null)) != null) {
            SETTING_SERIALIZE_CHECK_REFERENCE = !CommonUtils.isFalse(tmp);
        }
        HashSet<SerializerFeature> sfTmp = new HashSet<SerializerFeature>();
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_JSON_SERIALIZER_FEATURES, null)) != null) {
            String[] features = tmp.split(",", -1);
            for (String feature : features) {
                if (StringUtils.isNotEmpty(feature)) {
                    sfTmp.add(SerializerFeature.valueOf(feature));
                }
            }
        }
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_JSON_SERIALIZE_FILL_EMPTY, null)) != null) {
            if (CommonUtils.isTrue(tmp)) {
                sfTmp.add(SerializerFeature.WriteNullListAsEmpty);
                sfTmp.add(SerializerFeature.WriteNullStringAsEmpty);
                sfTmp.add(SerializerFeature.WriteNullBooleanAsFalse);
                sfTmp.add(SerializerFeature.WriteNullNumberAsZero);
                sfTmp.add(SerializerFeature.WriteMapNullValue);
            } else if (CommonUtils.isFalse(tmp)) {
                sfTmp.remove(SerializerFeature.WriteNullListAsEmpty);
                sfTmp.remove(SerializerFeature.WriteNullStringAsEmpty);
                sfTmp.remove(SerializerFeature.WriteNullBooleanAsFalse);
                sfTmp.remove(SerializerFeature.WriteNullNumberAsZero);
                sfTmp.remove(SerializerFeature.WriteMapNullValue);
            }
        }
        HashSet<Feature> pfTmp = new HashSet<Feature>();
        if ((tmp = JSFContext.getGlobalVal(Constants.SETTING_JSON_PARSER_FEATURES, null)) != null) {
            String[] features = tmp.split(",", -1);
            for (String feature : features) {
                if (StringUtils.isNotEmpty(feature)) {
                    pfTmp.add(Feature.valueOf(feature));
                }
            }
        }
        JSON_SERIALIZER_FEATURES = sfTmp.toArray(new SerializerFeature[sfTmp.size()]);
        JSON_PARSER_FEATURES = pfTmp.toArray(new Feature[pfTmp.size()]);
    }
}