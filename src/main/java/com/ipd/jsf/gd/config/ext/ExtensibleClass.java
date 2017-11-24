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
package com.ipd.jsf.gd.config.ext;

import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.config.annotation.AutoActive;
import com.ipd.jsf.gd.config.annotation.Extensible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: <br>
 *
 * Description: <br>
 * @see Extensible
 * @see AutoActive
 */
public class ExtensibleClass<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensibleClass.class);

    protected String alias; // 扩展别名,不是provider alias
    protected Class<T> clazz; // 扩展接口类
    protected int order; // 扩展点排序
    protected boolean autoActive; // 是否自动激活
    protected boolean providerSide; // 服务端是否激活
    protected boolean consumerSide; // 调用端是否激活


    /**
     *
     * @return instance of clazz
     */
    public T getExtInstance(){
        if ( clazz != null ){
            try {
                return ClassLoaderUtils.newInstance(clazz);
            } catch (Exception e) {
                logger.error("create {} instance error",clazz.getCanonicalName(),e);
                return null;
            }
        }
        return null;
    }


    public String getAlias() {
        return alias;
    }

    public ExtensibleClass setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public ExtensibleClass setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public ExtensibleClass setOrder(int order) {
        this.order = order;
        return this;
    }

    public boolean isProviderSide() {
        return providerSide;
    }

    public ExtensibleClass setProviderSide(boolean providerSide) {
        this.providerSide = providerSide;
        return this;
    }

    public boolean isConsumerSide() {
        return consumerSide;
    }

    public ExtensibleClass setConsumerSide(boolean consumerSide) {
        this.consumerSide = consumerSide;
        return this;
    }

    public boolean isAutoActive() {
        return autoActive;
    }

    public ExtensibleClass setAutoActive(boolean autoActive) {
        this.autoActive = autoActive;
        return this;
    }

    @Override
    public String toString() {
        return "ExtensibleClass{" +
                "alias='" + alias + '\'' +
                ", clazz=" + clazz +
                ", order=" + order +
                ", providerSide=" + providerSide +
                ", consumerSide=" + consumerSide +
                ", autoActive=" + autoActive +
                '}';
    }
}