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

import com.ipd.jsf.gd.config.ServerConfig;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 对于ServerConfig<br>
 *
 * Description: 服务端配置的常用参数，高级功能请使用API或者spring集成的方式<br>
 *
 * @see ServerConfig
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Server {

    /**
     * 协议名，必填
     *
     * @return the string
     */
	String protocol() default Constants.DEFAULT_PROTOCOL;

    /**
     * 主机地址，选填
     *
     * @return the string
     */
    String host() default "";

    /**
     * 端口地址，选填
     *
     * @return the int
     */
    int port() default Constants.DEFAULT_SERVER_PORT;

    /**
     * 业务线程池大小，选填
     *
     * @return the int
     */
    int threads() default Constants.DEFAULT_SERVER_BIZ_THREADS;

    /**
     * 线程池类型，选填
     *
     * @return the string
     */
    String threadpool() default Constants.THREADPOOL_TYPE_CACHED;
}