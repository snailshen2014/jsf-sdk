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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.registry.Provider;

/**
 * Title: 负载均衡轮询算法<br>
 * <p/>
 * Description: 按方法级进行轮询，互不影响<br>
 * <p/>
 */
public class RoundrobinLoadbalance extends Loadbalance {

    private final ConcurrentMap<String, PositiveAtomicCounter> sequences = new ConcurrentHashMap<String, PositiveAtomicCounter>();

    private final ConcurrentMap<String, PositiveAtomicCounter> weightSequences = new ConcurrentHashMap<String, PositiveAtomicCounter>();

    /**
     * @see Loadbalance#doSelect(Invocation, java.util.List)
     */
    public Provider doSelect(Invocation invocation, List<Provider> providers) {

        Provider provider = null;
        String key = getServiceKey(invocation); // 每个方法级自己轮询，互不影响
        int length = providers.size(); // 总个数
        int maxWeight = 0; // 最大权重
        int minWeight = Integer.MAX_VALUE; // 最小权重
        for (int i = 0; i < length; i++) {
            int weight = getWeight(providers.get(i));
            maxWeight = Math.max(maxWeight, weight); // 累计最大权重
            minWeight = Math.min(minWeight, weight); // 累计最小权重
        }
        if (maxWeight > 0 && minWeight < maxWeight) { // 权重不一样,不再按照之前轮询顺序，
            PositiveAtomicCounter weightSequence = weightSequences.get(key);
            if (weightSequence == null) {
                weightSequences.putIfAbsent(key, new PositiveAtomicCounter());
                weightSequence = weightSequences.get(key);
            }
            int currentWeight = weightSequence.getAndIncrement() % maxWeight;
            List<Provider> weightInvokers = new ArrayList<Provider>();
            for (Provider invoker : providers) { // 筛选权重大于当前权重基数的provider,保证权重大的服务哪怕是轮询，被调用的机会也是最多的
                if (getWeight(invoker) > currentWeight) {
                    weightInvokers.add(invoker);
                }
            }
            int weightLength = weightInvokers.size();
            if (weightLength == 1) {
                return weightInvokers.get(0);
            } else if (weightLength > 1) {
                providers = weightInvokers;
                length = providers.size();
            }
        }
        PositiveAtomicCounter sequence = sequences.get(key);
        if (sequence == null) {
            sequences.putIfAbsent(key, new PositiveAtomicCounter());
            sequence = sequences.get(key);
        }
        provider = providers.get(sequence.getAndIncrement() % length);
        return provider;
    }


    private String getServiceKey(Invocation invocation) {
        StringBuilder builder = new StringBuilder();
        builder.append(invocation.getClazzName()).append("::")
                .append(invocation.getAlias()).append("::")
                .append(invocation.getMethodName());
        return builder.toString();
    }

    private class PositiveAtomicCounter {
        private final AtomicInteger atom;
        private static final int mask = 0x7FFFFFFF;

        public PositiveAtomicCounter() {
            atom = new AtomicInteger(0);
        }

        public final int incrementAndGet() {
            final int rt = atom.incrementAndGet();
            return rt & mask;
        }

        public final int getAndIncrement() {
            final int rt = atom.getAndIncrement();
            return rt & mask;
        }

        public int intValue() {
            return atom.intValue();
        }
    }
}