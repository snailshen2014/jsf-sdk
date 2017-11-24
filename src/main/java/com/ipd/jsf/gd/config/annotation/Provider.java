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
package com.ipd.jsf.gd.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 对应ProviderConfig<br>
 * <p/>
 * Description: 只有常用属性，高级功能请使用API或者spring集成的方式<br>
 * <p/>
 * @see ProviderConfig
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Provider {

    /**
     * 服务别名，必填，兼容saf1.x的格式为"group:version"
     *
     * @return the string
     */
    String alias();

    /**
     * 注册到的服务端，必填
     *
     * @return the server [ ]
     */
    Server[] server();

    /**
     * 是否注册到注册中心
     *
     * @return the boolean
     */
    boolean register() default true;

    /**
     * 是否动态发布服务
     *
     * @return the boolean
     */
    boolean dynamic() default true;

    /**
     * 服务端权重
     *
     * @return the int
     */
    int weight() default Constants.DEFAULT_PROVIDER_WEIGHT;
}