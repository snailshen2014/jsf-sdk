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
package com.ipd.jsf.gd.reflect;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.server.Invoker;

/**
 * Title: 代理工厂类<br>
 * <p/>
 * Description: 目前支持jdk和javassist<br>
 * <p/>
 */
public final class ProxyFactory {

    /**
     * 构建代理类实例
     *
     * @param proxy
     *         代理类型
     * @param clazz
     *         原始类
     * @param proxyInvoker
     *         代码执行的Invoker
     * @param <T>
     *         类型
     * @return 代理类实例
     * @throws Exception
     */
    public static <T> T buildProxy(String proxy, Class<T> clazz, Invoker proxyInvoker) throws Exception {
        if (Constants.PROXY_JAVASSIST.equals(proxy)) {
            return JavassistProxy.getProxy(clazz, proxyInvoker);
        } else if (Constants.PROXY_JDK.equals(proxy)) {
            return JDKProxy.getProxy(clazz, proxyInvoker);
        } else {
            throw new IllegalConfigureException(21316, "consumer.proxy", proxy);
        }
    }
}