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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.ipd.jsf.gd.server.Invoker;
import com.ipd.jsf.gd.util.ClassLoaderUtils;

/**
 * Title: JDK代理<br>
 * <p/>
 * Description: 原生<br>
 * <p/>
 */
public class JDKProxy {

    public static <T> T getProxy(Class<T> interfaceClass, Invoker proxyInvoker) throws Exception {
        InvocationHandler handler = new JDKInvocationHandler(proxyInvoker);
        ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
        T result = (T) Proxy.newProxyInstance(classLoader,
                new Class[]{interfaceClass}, handler);
        return result;
    }
}