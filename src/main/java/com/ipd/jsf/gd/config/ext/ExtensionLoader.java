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

import com.ipd.jsf.gd.config.annotation.AutoActive;
import com.ipd.jsf.gd.config.annotation.Extensible;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 * @see Extensible
 * @see AutoActive
 */
public class ExtensionLoader<T> {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);

    /**
     * 当前加载的接口类名
     */
    protected final Class<T> interfaceClass;

    /**
     * 接口名字
     */
    protected final String interfaceName;

    /**
     * 全部的加载的实现类
     */
    protected final ConcurrentHashMap<String, ExtensibleClass> all = new ConcurrentHashMap<String, ExtensibleClass>();

    /**
     * 自动激活的
     */
    protected final ConcurrentHashMap<String, ExtensibleClass> autoActives = new ConcurrentHashMap<String, ExtensibleClass>();

    /**
     * 构造函数（自动加载）
     *
     * @param interfaceClass 接口类
     */
    public ExtensionLoader(Class<T> interfaceClass) {
        this(interfaceClass, true);
    }

    /**
     * 构造函数（主要测试用）
     *
     * @param interfaceClass 接口类
     * @param autoLoad       是否自动开始加载
     */
    protected ExtensionLoader(Class<T> interfaceClass, boolean autoLoad) {
        if (interfaceClass == null || !interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Extension class must be interface!");
        }
        this.interfaceClass = interfaceClass;
        this.interfaceName = ClassTypeUtils.getTypeStr(interfaceClass);
        if (autoLoad) {
            loadFromFile("META-INF/jsf/");
            loadFromFile("META-INF/services/");
        }
    }

    /**
     * @param path path必须以/结尾
     */
    protected synchronized void loadFromFile(String path) {
        String fileName = path + interfaceName;
        try {
            ClassLoader classLoader = ClassLoaderUtils.getClassLoader(getClass());
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources(fileName)
                    : ClassLoader.getSystemResources(fileName);
            // 可能存在多个文件。
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    // 读取一个文件
                    URL url = urls.nextElement();
                    LOGGER.debug("Loading extension of interface {} from file: {}",interfaceName, url);
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            readLine(url, line);
                        }
                    } catch (Throwable t) {
                        LOGGER.error("Failed to load extension of interface " + interfaceName
                                + " from file:" + url, t);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to load extension of interface " + interfaceName
                    + " from path:" + fileName, t);
        }
    }

    protected void readLine(URL url, String line) throws Throwable {
        String[] aliasAndClassName = parseAliasAndClassName(line);
        if (aliasAndClassName == null || aliasAndClassName.length != 2) {
            return;
        }
        String alias = aliasAndClassName[0];
        String className = aliasAndClassName[1];
        // 读取配置的实现类
        Class<?> implClass = ClassLoaderUtils.forName(className, true);
        if (!interfaceClass.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException("Error when load extension of interface " + interfaceName
                    + " from file:" + url + ", " + className + " is not subtype of interface.");
        }

        // 检查是否有可扩展标识
        Extensible extensible = implClass.getAnnotation(Extensible.class);
        if (extensible == null) {
            throw new IllegalArgumentException("Error when load extension of interface " + interfaceName
                    + " from file:" + url + ", " + className + " must add annotation @Extensible.");
        } else {
            String aliasInCode = extensible.value();
            if (StringUtils.isBlank(aliasInCode)) {
                throw new IllegalArgumentException("Error when load extension of interface "
                        + interfaceClass + " from file:" + url + ", " + className
                        + "'s alias of @extensible is blank");
            }
            if (alias == null) {
                alias = aliasInCode;
            } else {
                if (!aliasInCode.equals(alias)) {
                    throw new IllegalArgumentException("Error when load extension of interface "
                            + interfaceName + " from file:" + url + ", aliases of " + className + " are " +
                            "not equal between " + aliasInCode + "(code) and " + alias + "(file).");
                }
            }
        }
        // 提前试试能不能实例化，
        try {
            implClass.getConstructor(interfaceClass);
        } catch (NoSuchMethodException e) {
            implClass.getConstructor(); // 有没有默认的空的构造函数
        }
        // 检查是否有存在不对的
        ExtensibleClass old = all.get(alias);
        if (old != null) {
            throw new IllegalStateException("Error when load extension of interface "
                    + interfaceClass + " from file:" + url + ", Duplicate class with same alias: "
                    + alias + ", " + old.getClazz() + " and " + implClass);
        } else {
            ExtensibleClass extensibleClass = new ExtensibleClass();
            extensibleClass.setAlias(alias);
            extensibleClass.setClazz(implClass);
            extensibleClass.setOrder(extensible.order());
            // 读取自动加载的类列表。
            AutoActive autoActive = implClass.getAnnotation(AutoActive.class);
            if (autoActive != null) {
                extensibleClass.setAutoActive(true);
                extensibleClass.setProviderSide(autoActive.providerSide());
                extensibleClass.setConsumerSide(autoActive.consumerSide());
                autoActives.put(alias, extensibleClass);
                LOGGER.debug("Extension of interface " + interfaceName + " from file:" + url
                        + ", " + implClass + "(" + alias + ") will auto active");
            }
            all.put(alias, extensibleClass);
        }
    }

    protected String[] parseAliasAndClassName(String line) {
        line = line.trim();
        int i0 = line.indexOf("#");
        if (i0 == 0 || line.length() == 0) {
            return null; // 当前行是注释 或者 空
        }
        if (i0 > 0) {
            line = line.substring(0, i0).trim();
        }

        String alias = null;
        String className = null;
        int i = line.indexOf('=');
        if (i > 0) {
            alias = line.substring(0, i).trim(); // 以代码里的为准
            className = line.substring(i + 1).trim();
        } else {
            className = line;
        }
        if (className.length() == 0) {
            return null;
        }
        return new String[]{alias, className};
    }

    /**
     * 得到服务端的全部自动激活扩展
     *
     * @return 自动激活扩展列表
     */
    public List<ExtensibleClass> getProviderSideAutoActives() {
        List<ExtensibleClass> extensibleClasses = new ArrayList<ExtensibleClass>();
        for (ConcurrentHashMap.Entry<String, ExtensibleClass> entry : all.entrySet()) {
            ExtensibleClass extensibleClass = entry.getValue();
            if (extensibleClass.isAutoActive() && extensibleClass.isProviderSide()) {
                extensibleClasses.add(extensibleClass);
            }
        }
        Collections.sort(extensibleClasses, new OrderComparator());
        return extensibleClasses;
    }

    /**
     * 得到调用端的全部自动激活扩展
     *
     * @return 自动激活扩展列表
     */
    public List<ExtensibleClass> getConsumerSideAutoActives() {
        List<ExtensibleClass> extensibleClasses = new ArrayList<ExtensibleClass>();
        for (ConcurrentHashMap.Entry<String, ExtensibleClass> entry : all.entrySet()) {
            ExtensibleClass extensibleClass = entry.getValue();
            if (extensibleClass.isAutoActive() && extensibleClass.isConsumerSide()) {
                extensibleClasses.add(extensibleClass);
            }
        }
        Collections.sort(extensibleClasses, new OrderComparator());
        return extensibleClasses;
    }

    /**
     * 根据服务别名查找扩展类
     *
     * @param alias 扩展别名
     * @return 扩展类对象
     */
    public ExtensibleClass getExtensibleClass(String alias) {
        return all.get(alias);
    }


    protected static class OrderComparator implements Comparator<ExtensibleClass> {
        @Override
        public int compare(ExtensibleClass o1, ExtensibleClass o2) {
            // order一样的情况下，先加入的在前面
            return o1.getOrder() > o2.getOrder() ? 1 : -1;
        }
    }
}