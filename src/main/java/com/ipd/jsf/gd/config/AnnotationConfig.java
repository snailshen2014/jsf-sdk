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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class AnnotationConfig extends AbstractIdConfig implements Serializable {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AnnotationConfig.class);

    /*---------- 参数配置项开始 ------------*/

    /**
     * 包的基本路径（前缀）
     */
    protected String basepackage;

    /**
     * 是否扫描provider
     */
    protected boolean provider = true;

    /**
     * 是否扫描consumer
     */
    protected boolean consumer = true;

    /*---------- 参数配置项结束 ------------*/

    /**
     * 解析出来的各个包
     */
    protected transient String[] annotationPackages;

    /**
     * Gets basepackage.
     *
     * @return the basepackage
     */
    public String getBasepackage() {
        return basepackage;
    }

    /**
     * Sets basepackage.
     *
     * @param basepackage          the basepackage
     */
    public void setBasepackage(String basepackage) {
        this.basepackage = basepackage;
        this.annotationPackages = StringUtils.isBlank(basepackage) ? null
                : StringUtils.splitWithCommaOrSemicolon(this.basepackage);
    }

    /**
     * Is provider.
     *
     * @return the boolean
     */
    public boolean isProvider() {
        return provider;
    }

    /**
     * Sets provider.
     *
     * @param provider the provider
     */
    public void setProvider(boolean provider) {
        this.provider = provider;
    }

    /**
     * Is consumer.
     *
     * @return the boolean
     */
    public boolean isConsumer() {
        return consumer;
    }

    /**
     * Sets consumer.
     *
     * @param consumer the consumer
     */
    public void setConsumer(boolean consumer) {
        this.consumer = consumer;
    }


    /**
     * Is match package.
     *
     * @param bean
     *         the bean
     * @return the boolean
     */
    protected boolean isMatchPackage(Object bean) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = bean.getClass().getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}