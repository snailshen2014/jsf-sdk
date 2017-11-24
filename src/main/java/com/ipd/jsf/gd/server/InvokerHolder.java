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
package com.ipd.jsf.gd.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class InvokerHolder {


    /**
     * 全部Invoker列表 一个接口+alias对应一个Invoker
     */
    private final static ConcurrentHashMap<String, Invoker> allInstanceMap = new ConcurrentHashMap<String, Invoker>();

    /**
     * invoker计数器
     */
    private final static ConcurrentHashMap<String, AtomicInteger> refCounter = new ConcurrentHashMap<String, AtomicInteger>();

    /**
     * 缓存Invoker
     *
     * @param key
     * @param invoker
     */
    public static void cacheInvoker(String key, Invoker invoker) {
        allInstanceMap.putIfAbsent(key, invoker);
        AtomicInteger cnt = CommonUtils.putToConcurrentMap(refCounter, key, new AtomicInteger(0));
        cnt.incrementAndGet();
    }

    /**
     * 取消缓存Invoker
     *
     * @param key
     */
    public static void invalidateInvoker(String key) {
        AtomicInteger cnt = refCounter.get(key);
        if (cnt != null && cnt.decrementAndGet() <= 0) {
            allInstanceMap.remove(key);
            refCounter.remove(key);
        }
    }

    /**
     * 得到全部invoker
     *
     * @return all invoker
     */
    public static Map<String, Invoker> getAllInvoker() {
        return allInstanceMap;
    }

    /**
     * Get invoker.
     *
     * @param interfaceId
     *         the interface id
     * @param alias
     *         the alias
     * @return the invoker
     */
    public static Invoker getInvoker(String interfaceId, String alias) {
        String key = BaseServerHandler.genInstanceName(interfaceId, alias);
        return allInstanceMap.get(key);
    }
}