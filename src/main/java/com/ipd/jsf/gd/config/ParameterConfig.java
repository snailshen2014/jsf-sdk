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

import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 自定义参数配置，可出现在registry，server，provider，consumer，method下面<br>
 * <p/>
 * Description: 对应jsf:parameter，是key-value形式的参数<br>
 * <p/>
 */
public class ParameterConfig extends AbstractConfig {

    /**
     * 关键字
     */
    private String key;

    /**
     * 值
     */
    private String value;

    /**
     * 是否隐藏（是的话，业务代码不能获取到）
     */
    private boolean hide = false;

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets key.
     *
     * @param key
     *         the key
     */
    public void setKey(String key) {
        if (!StringUtils.isValidParamKey(key)) {
            throw new IllegalConfigureException(21800, "param.key", key, "key can not start with "
                    + Constants.HIDE_KEY_PREFIX + " and " + Constants.INTERNAL_KEY_PREFIX);
        }
        this.key = key;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Is hide.
     *
     * @return the boolean
     */
    public boolean isHide() {
        return hide;
    }

    /**
     * Sets hide.
     *
     * @param hide the hide
     */
    public void setHide(boolean hide) {
        this.hide = hide;
    }
}