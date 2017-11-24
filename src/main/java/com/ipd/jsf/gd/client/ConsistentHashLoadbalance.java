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
package com.ipd.jsf.gd.client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 一致性hash算法，同样的请求（第一参数）会打到同样的节点 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ConsistentHashLoadbalance extends Loadbalance {

    /**
     * {interface#method : selector}
     */
    private ConcurrentHashMap<String, Selector> selector_cache = new ConcurrentHashMap<String, Selector>();

    /**
     * Do select.
     *
     * @param invocation
     *         the invocation
     * @param providers
     *         the providers
     * @return the provider
     */
    @Override
    public Provider doSelect(Invocation invocation, List<Provider> providers) {
        String interfaceId = invocation.getClazzName();
        String method = invocation.getMethodName();
        String key = interfaceId + "#" + method;
        int hashcode = providers.hashCode(); // 判断是否同样的服务列表
        Selector selector = selector_cache.get(key);
        if (selector == null // 原来没有
                || selector.getHashCode() != hashcode) { // 或者服务列表已经变化
            selector = new Selector(interfaceId, method, providers, hashcode);
            selector_cache.put(key, selector);
        }
        return selector.select(invocation);
    }

    /**
     * 选择器
     */
    private class Selector {

        /**
         * The Hashcode.
         */
        private final int hashcode;

        /**
         * The Interface id.
         */
        private final String interfaceId;

        /**
         * The Method name.
         */
        private final String method;

        /**
         * 虚拟节点
         */
        private final TreeMap<Long, Provider> virtualNodes;

        /**
         * Instantiates a new Selector.
         *
         * @param interfaceId
         *         the interface id
         * @param method
         *         the method
         * @param actualNodes
         *         the actual nodes
         */
        public Selector(String interfaceId, String method, List<Provider> actualNodes) {
            this(interfaceId, method, actualNodes, actualNodes.hashCode());
        }

        /**
         * Instantiates a new Selector.
         *
         * @param interfaceId
         *         the interface id
         * @param method
         *         the method
         * @param actualNodes
         *         the actual nodes
         * @param hashcode
         *         the hashcode
         */
        public Selector(String interfaceId, String method, List<Provider> actualNodes, int hashcode) {
            this.interfaceId = interfaceId;
            this.method = method;
            this.hashcode = hashcode;
            // 创建虚拟节点环 （默认一个provider共创建128个虚拟节点，较多比较均匀）
            this.virtualNodes = new TreeMap<Long, Provider>();
            int num = 128;
            for (Provider provider : actualNodes) {
                for (int i = 0; i < num / 4; i++) {
                    byte[] digest = messageDigest(provider.getIp() + provider.getPort() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualNodes.put(m, provider);
                    }
                }
            }
        }

        /**
         * Select provider.
         *
         * @param invocation
         *         the invocation
         * @return the provider
         */
        public Provider select(Invocation invocation) {
            String key = buildKeyOfHash(invocation.getArgs());
            byte[] digest = messageDigest(key);
            return sekectForKey(hash(digest, 0));
        }

        /**
         * 获取第一参数作为hash的key
         *
         * @param args
         *         the args
         * @return the string
         */
        private String buildKeyOfHash(Object[] args) {
            if (CommonUtils.isEmpty(args)) {
                return StringUtils.EMPTY;
            } else {
                return StringUtils.toString(args[0]);
            }
        }

        /**
         * Sekect for key.
         *
         * @param hash
         *         the hash
         * @return the provider
         */
        private Provider sekectForKey(long hash) {
            Provider provider = virtualNodes.get(hash);
            if (provider == null) {
                SortedMap<Long, Provider> tailMap = virtualNodes.tailMap(hash);
                if (tailMap.isEmpty()) {
                    hash = virtualNodes.firstKey();
                } else {
                    hash = tailMap.firstKey();
                }
                provider = virtualNodes.get(hash);
            }
            return provider;
        }

        /**
         * 换算法？ MD5  SHA-1 MurMurHash???
         *
         * @param value
         *         the value
         * @return the byte [ ]
         */
        private byte[] messageDigest(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
                // md5.reset();
                md5.update(value.getBytes("UTF-8"));
                return md5.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new RpcException("No such algorithm named md5", e);
            } catch (UnsupportedEncodingException e) {
                throw new RpcException("Unsupported encodinf of" + value, e);
            }
        }

        /**
         * Hash long.
         *
         * @param digest
         *         the digest
         * @param index
         *         the number
         * @return the long
         */
        private long hash(byte[] digest, int index) {
            long f = ((long) (digest[3 + index * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + index * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + index * 4] & 0xFF) << 8)
                    | (digest[index * 4] & 0xFF);
            return f & 0xFFFFFFFFL;
        }

        /**
         * Gets hash code.
         *
         * @return the hash code
         */
        public int getHashCode() {
            return hashcode;
        }
    }
}