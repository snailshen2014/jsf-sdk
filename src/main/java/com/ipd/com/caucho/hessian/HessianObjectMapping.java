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
package com.ipd.com.caucho.hessian;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Title: 保存一些新旧类的映射关系<br>
 * <p/>
 * Description: 例如旧的发来com.xxx.Obj，需要拿com.yyy.Obj去解析，则可以使用此类<br>
 * <p/>
 */
public class HessianObjectMapping {

    /**
     * 保留映射关系 旧类-->新类
     */
    private final static ConcurrentHashMap<String, String> objectMap = new ConcurrentHashMap<String, String>();

    /**
     * Registry mapping.
     *
     * @param oldclass
     *         the oldclass
     * @param newclass
     *         the newclass
     */
    public static void registryMapping(String oldclass, String newclass) {
        objectMap.put(oldclass, newclass);
    }

    /**
     * Unregistry mapping.
     *
     * @param oldclass
     *         the oldclass
     */
    public static void unregistryMapping(String oldclass) {
        objectMap.remove(oldclass);
    }

    /**
     * Check mapping.
     *
     * @param clazz
     *         the clazz
     * @return the string
     */
    public static String checkMapping(String clazz) {
        if (objectMap.isEmpty()) {
            return clazz;
        }
        String mapclazz = objectMap.get(clazz);
        return mapclazz != null ? mapclazz : clazz;
    }
}