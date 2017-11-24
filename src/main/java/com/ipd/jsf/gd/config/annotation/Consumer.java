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

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 对应ConsumerConfig<br>
 * <p/>
 * Description: 只有常用属性，高级功能请使用API或者spring集成的方式<br>
 * <p/>
 * @see ConsumerConfig
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Consumer {

    /**
     * 服务别名，必填，兼容saf1.x的格式为"group:version"
     *
     * @return the string
     */
    String alias();

    /**
     * 调用协议，必填.
     *
     * @return the string
     */
    String protocol() default Constants.DEFAULT_PROTOCOL;

    /**
     * 集群策略，选填
     *
     * @return the string
     */
    String cluster() default Constants.CLUSTER_FAILOVER;

    /**
     * 失败后重试次数，选填
     *
     * @return the int
     */
    int retries() default Constants.DEFAULT_RETRIES_TIME;

    /**
     * 调用超时，选填
     *
     * @return the int
     */
    int timeout() default Constants.DEFAULT_CLIENT_INVOKE_TIMEOUT;

    /**
     * 直连地址，选填
     *
     * @return the string
     */
    String url() default "";

    /**
     * 负载均衡算法，选填
     *
     * @return the string
     */
    String loadbalance() default Constants.LOADBALANCE_RANDOM;

    /**
     * 序列化方式，选填
     *
     * @return the string
     */
    String serialization() default Constants.DEFAULT_CODEC;

    /**
     * 是否延迟加载服务端连接，选填
     *
     * @return the boolean
     */
    boolean lazy() default false;
}