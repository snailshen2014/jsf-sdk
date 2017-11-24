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

import java.util.List;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.NoAliveProviderException;

/**
 * Title: 负载均衡算法基类+工厂类<br>
 *
 * Description: <br>
 */
public abstract class Loadbalance {

    /**
     * 一些客户端的配置
     */
    protected ConsumerConfig consumerConfig;

    /**
     * 得到负载均衡算法
     *
     * @param loadBalanceName
     *         负载均衡名称
     * @return Loadbalance实现 loadbalance
     */
    public static Loadbalance getInstance(String loadBalanceName) {
        if (Constants.LOADBALANCE_RANDOM.equals(loadBalanceName)) {
            return new RandomLoadbalance();
        } else if (Constants.LOADBALANCE_ROUNDROBIN.equals(loadBalanceName)) {
            return new RoundrobinLoadbalance();
        } else if (Constants.LOADBALANCE_LEASTACTIVE.equals(loadBalanceName)) {
            return new LeastActiveLoadbalance();
        } else if (Constants.LOADBALANCE_CONSISTENTHASH.equals(loadBalanceName)) {
            return new ConsistentHashLoadbalance();
        } else if (Constants.LOADBALANCE_LOCALPREF.equals(loadBalanceName)) {
            return new LocalPreferenceLoadbalance();
        } else {
            // 非法配置
            throw new IllegalConfigureException(21317, "consumer.loadbalance",
                    loadBalanceName);
        }
    }

    /**
     * 筛选服务端连接
     *
     * @param invocation
     *         请求
     * @param providers
     *         可用连接
     * @return provider
     */
    public Provider select(Invocation invocation, List<Provider> providers) {
        if (providers.size() == 0) {
            throw new NoAliveProviderException(consumerConfig.buildKey(), providers);
        }
        if (providers.size() == 1) {
            return providers.get(0);
        } else {
            return doSelect(invocation, providers);
        }
    }

    /**
     * 根据负载均衡筛选
     *
     * @param invocation
     *         请求
     * @param providers
     *         全部服务端连接
     * @return 服务端连接 provider
     */

    public abstract Provider doSelect(Invocation invocation, List<Provider> providers);

    /**
     * Gets consumer config.
     *
     * @return the consumer config
     */
    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    /**
     * Sets consumer config.
     *
     * @param consumerConfig
     *         the consumer config
     */
    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    /**
     * Gets weight.
     *
     * @param provider
     *         the provider
     * @return the weight
     */
    protected int getWeight(Provider provider) {
        // 从provider中或得到相关权重,默认值100
        return provider.getWeight() < 0 ? 0 : provider.getWeight();
    }
}