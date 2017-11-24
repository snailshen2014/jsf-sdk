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
package com.ipd.jsf.gd.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title: 二级的Map<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class TwoLevelMap<K, V> {

    protected ConcurrentHashMap<K, ConcurrentHashMap<K, V>> entries =
            new ConcurrentHashMap<K, ConcurrentHashMap<K, V>>();

    /**
     * 设置值
     *
     * @param parentKey
     *         一级key
     * @param childKey
     *         二级key
     * @param value
     *         值
     * @return 设置结果
     */
    public V put(K parentKey, K childKey, V value) {
        ConcurrentHashMap<K, V> data = entries.get(parentKey);
        if (data == null) {
            ConcurrentHashMap<K, V> newMap = new ConcurrentHashMap<K, V>();
            ConcurrentHashMap<K, V> old = entries.putIfAbsent(parentKey, newMap);
            data = old != null ? old : newMap;
        }
        return data.put(childKey, value);
    }

    /**
     * 直接覆盖设置一级值
     *
     * @param parentKey
     *         一级key
     * @param childMap
     *         一级值
     * @return 设置结果
     */
    public ConcurrentHashMap<K, V> put(K parentKey, Map<K, V> childMap) {
        return entries.put(parentKey, new ConcurrentHashMap<K, V>(childMap));
    }

    /**
     * 得到值
     *
     * @param parentKey
     *         一级key
     * @param childKey
     *         二级key
     * @return 值
     */
    public V get(K parentKey, K childKey) {
        ConcurrentHashMap<K, V> data = entries.get(parentKey);
        if (data == null) {
            return null;
        }
        return data.get(childKey);
    }

    /**
     * 得到值，找不到二级key的值找默认值
     *
     * @param parentKey
     *         一级key
     * @param childKey
     *         二级key
     * @param defChildKey
     *         二级默认key
     * @return 值
     */
    public V getOrDefault(K parentKey, K childKey, K defChildKey) {
        ConcurrentHashMap<K, V> data = entries.get(parentKey);
        if (data == null) {
            return null;
        }
        V child = data.get(childKey);
        return child == null ? data.get(defChildKey) : child;
    }

    /**
     * 得到值
     *
     * @param parentKey
     *         一级key
     * @return 值
     */
    public ConcurrentHashMap<K, V> get(K parentKey) {
        return entries.get(parentKey);
    }

    /**
     * 遍历一级key
     *
     * @return
     */
    public Set<Map.Entry<K, ConcurrentHashMap<K, V>>> entrySet() {
        return entries.entrySet();
    }

    /**
     * 删除一级key
     *
     * @param parentKey
     *         一级key
     * @return 删除结果
     */
    public ConcurrentHashMap<K, V> remove(K parentKey) {
        return entries.remove(parentKey);
    }

    /**
     * 删除一级key
     *
     * @param parentKey
     *         一级key
     * @return 删除结果
     */
    public ConcurrentHashMap<K, V> removeIfEmpty(K parentKey) {
        ConcurrentHashMap<K, V> map = get(parentKey);
        return map.isEmpty() ? entries.remove(parentKey) : null;
    }

    /**
     * 删除二级key
     *
     * @param parentKey
     *         一级key
     * @param childKey
     *         二级key
     * @return 删除结果
     */
    public V remove(K parentKey, K childKey) {
        ConcurrentHashMap<K, V> map = get(parentKey);
        return map != null ? map.remove(childKey) : null;
    }
}