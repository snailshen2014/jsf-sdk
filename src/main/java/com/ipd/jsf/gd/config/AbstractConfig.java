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
package com.ipd.jsf.gd.config;

import java.util.regex.Pattern;

import com.ipd.jsf.gd.error.IllegalConfigureException;

/**
 * Title: 最基本的config，包含一些常用方法，无属性<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public abstract class AbstractConfig {

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点.
     * !@#$*,;:有特殊含义
     */
    protected final static Pattern NORMAL = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.]+$");

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 逗号,
     * !@#$*;:有特殊含义
     */
    protected final static Pattern NORMAL_COMMA = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.,]+$");

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 冒号:
     * !@#$*,;有特殊含义
     */
    protected final static Pattern NORMAL_COLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.:]+$");

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 分号;
     * !@#$*,;有特殊含义
     */
    protected final static Pattern NORMAL_SEMICOLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.;]+$");

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 逗号, 冒号:
     * !@#$*,;有特殊含义
     */
    protected final static Pattern NORMAL_COMMA_COLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.,:]+$");

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 分号; 冒号:
     * !@#$*,;有特殊含义
     */
    protected final static Pattern NORMAL_SEMICOLON_COLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.;:]+$");

    /**
     * 判断字符串是否为空或"false"或"null"
     *
     * @param string
     *         字符串
     * @return 是否为空或"false"或"null"
     */
    protected boolean assertFalse(String string) {
        return string == null || "".equals(string)
                || "false".equalsIgnoreCase(string) || "null".equals(string);
    }

    /**
     * 匹配正常字符串
     *
     * @param configValue
     *         配置项
     * @return 是否匹配，否表示有其他字符
     */
    protected boolean match(Pattern pattern, String configValue) {
        return pattern.matcher(configValue).find();
    }

    /**
     * 检查字符串是否是正常值，不是则抛出异常
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkNormal(String configKey, String configValue) throws IllegalConfigureException {
        checkPattern(21003, configKey, configValue, NORMAL, "only allow a-zA-Z0-9 '-' '_' '.'");
    }

    /**
     * 检查字符串是否是正常值（含逗号），不是则抛出异常
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkNormalWithComma(String configKey, String configValue) throws IllegalConfigureException {
        checkPattern(21004, configKey, configValue, NORMAL_COMMA, "only allow a-zA-Z0-9 '-' '_' '.' ','");
    }

    /**
     * 检查字符串是否是正常值（含冒号），不是则抛出异常
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkNormalWithColon(String configKey, String configValue) throws IllegalConfigureException {
        checkPattern(21005, configKey, configValue, NORMAL_COLON, "only allow a-zA-Z0-9 '-' '_' '.' ':'");
    }

    /**
     * 检查字符串是否是正常值（含冒号），不是则抛出异常
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkNormalWithCommaColon(String configKey, String configValue) throws IllegalConfigureException {
        checkPattern(21006, configKey, configValue, NORMAL_COMMA_COLON, "only allow a-zA-Z0-9 '-' '_' '.' ':' ','");
    }

    /**
     * 根据正则表达式检查字符串是否是正常值（含冒号），不是则抛出异常
     *
     * @param errCode
     *         错误码
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @param pattern
     *         正则表达式
     * @param message
     *         消息
     * @throws IllegalConfigureException
     */
    protected void checkPattern(int errCode, String configKey, String configValue, Pattern pattern, String message)
            throws IllegalConfigureException {
        if (configValue != null && !match(pattern, configValue)) {
            throw new IllegalConfigureException(errCode, configKey, configValue, message);
        }
    }

    /**
     * 检查数字是否为正整数（>0)
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkPositiveInteger(String configKey, int configValue) throws IllegalConfigureException {
        if (configValue <= 0) {
            throw new IllegalConfigureException(21001, configKey, configValue + "", "must > 0");
        }
    }

    /**
     * 检查数字是否为非负数（>=0)
     *
     * @param configKey
     *         配置项
     * @param configValue
     *         配置值
     * @throws IllegalConfigureException
     *         非法异常
     */
    protected void checkNonnegativeInteger(String configKey, int configValue) throws IllegalConfigureException {
        if (configValue < 0) {
            throw new IllegalConfigureException(21002, configKey, configValue + "", "must >= 0");
        }
    }
}