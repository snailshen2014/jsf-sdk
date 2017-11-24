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
package com.ipd.jsf.gd.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.InitErrorException;

/**
 * Title: 注册中心工厂类<br>
 * <p/>
 * Description: 集中创建和集中销毁<br>
 * <p/>
 */
public final class RegistryFactory {

    /**
     * 保存全部的配置
     */
    private final static ConcurrentHashMap<RegistryConfig, ClientRegistry> CLIENTREGISTRY_MAP
            = new ConcurrentHashMap<RegistryConfig, ClientRegistry>();
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RegistryFactory.class);

    /**
     * 得到注册中心对象
     *
     * @param registryConfig
     *         RegistryConfig类
     * @return ClientRegistry实现
     */
    public static synchronized ClientRegistry getRegistry(RegistryConfig registryConfig) {
        // 注意：RegistryConfig重写了equals方法，如果多个RegistryConfig属性一样，则认为是一个对象
        ClientRegistry clientRegistry = CLIENTREGISTRY_MAP.get(registryConfig);
        if (clientRegistry == null) {
            String protocol = registryConfig.getProtocol();
            if (Constants.REGISTRY_PROTOCOL_JSF.equals(protocol)) {
                if (defaultRegistryConfig != null) { // 要么全部不配，要么全部配
                    throw new InitErrorException("[JSF-20018]Default jsf registry already exists, please make sure" +
                            " all provider/consumer config has been setRegistry()");
                }
                // TODO 以后有需求再放开多注册中心功能
                for (RegistryConfig config : CLIENTREGISTRY_MAP.keySet()) {
                    // 目前如果有别的属性不同的JSFRegistry 抛异常。
                    if (config.getProtocol().equals(Constants.REGISTRY_PROTOCOL_JSF)) {
                        throw new InitErrorException("[JSF-20019]Duplicated jsf registry already exists," +
                                " please make sure jsf registry is a singleton.");
                    }
                }
                clientRegistry = new JSFRegistry(registryConfig);
            } else if (Constants.REGISTRY_PROTOCOL_FILE.equals(protocol)) {
                clientRegistry = new FileRegistry(registryConfig);
            } else if (Constants.REGISTRY_PROTOCOL_CUSTOM.equals(protocol)) {
                try {
                    Class<?> c = Class.forName(registryConfig.getImplClass());
                    /*以下调用带参的、私有构造函数*/
                    Constructor<?> c1 = c.getDeclaredConstructor(RegistryConfig.class);
                    c1.setAccessible(true);
                    clientRegistry = (ClientRegistry) c1.newInstance(registryConfig);
                } catch (Exception e) {
                    throw new InitErrorException("[JSF-20020]custom registry impl : [" + registryConfig.getImplClass() + "] init error!", e);
                }
            } else {
                throw new IllegalConfigureException(21100, "registry.protocol", protocol);
            }
            ClientRegistry registry = CLIENTREGISTRY_MAP.putIfAbsent(registryConfig, clientRegistry);
            if (registry != null) {
                clientRegistry = registry;
            }
        }
        return clientRegistry;
    }

    /**
     * 得到全部注册中心配置
     *
     * @return 注册中心配置
     */
    public static List<RegistryConfig> getRegistryConfigs(){
        return new ArrayList<RegistryConfig>(CLIENTREGISTRY_MAP.keySet());
    }

    /**
     * 得到全部注册中心
     *
     * @return 注册中心
     */
    public static List<ClientRegistry> getRegistries(){
        return new ArrayList<ClientRegistry>(CLIENTREGISTRY_MAP.values());
    }

    /**
     * 关闭全部注册中心
     */
    public static void destroyAll() {
        for (Map.Entry<RegistryConfig, ClientRegistry> entry : CLIENTREGISTRY_MAP.entrySet()) {
            RegistryConfig config = entry.getKey();
            ClientRegistry registry = entry.getValue();
            try {
                registry.destroy();
                CLIENTREGISTRY_MAP.remove(config);
            } catch (Exception e) {
                LOGGER.error("Error when destroy registry :" + config
                        + ", but you can ignore if it's called by JVM shutdown hook", e);
            }
        }
    }

    /**
     * 默认的注册中心配置
     */
    private volatile static RegistryConfig defaultRegistryConfig;

    /**
     * 构建默认的注册中心配置
     *
     * @return 配置中心RegistryConfig
     */
    public static RegistryConfig defaultConfig() {
        if (defaultRegistryConfig == null) {
            synchronized (LOGGER) {
                if (defaultRegistryConfig == null) {
                    RegistryConfig config = new RegistryConfig();
                    config.setParameter("_default", "true");
                    RegistryFactory.getRegistry(config); // 生成一个默认的注册中心
                    defaultRegistryConfig = config;
                }
            }
        }
        return defaultRegistryConfig;
    }

    /**
     * 批量反注册，目前只支持JSFRegistry
     */
    public static void batchUnregister() {
        for (ClientRegistry clientRegistry : CLIENTREGISTRY_MAP.values()) {
            if (clientRegistry instanceof JSFRegistry) {
                ((JSFRegistry) clientRegistry).batchUnregister();
            }
        }
    }
}