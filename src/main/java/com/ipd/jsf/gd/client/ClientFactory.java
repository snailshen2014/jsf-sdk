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

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 客户端工厂类<br>
 * <p/>
 * Description: 无缓存<br>
 * <p/>
 */
public class ClientFactory {

	/**
	 * 构造Client对象
	 * 
	 * @param consumerConfig
	 *            客户端配置
	 * @return Client对象
	 */
	public static Client getClient(ConsumerConfig consumerConfig) {
		Client client = null;
		String cluster = consumerConfig.getCluster();
		if (Constants.CLUSTER_FAILOVER.equals(cluster)) {
			client = new FailoverClient(consumerConfig);
		} else if (Constants.CLUSTER_FAILFAST.equals(cluster)) {
			client = new FailFastClient(consumerConfig);
//        } else if (Constants.CLUSTER_BROADCAST.equals(cluster)) {
//            if (consumerConfig.hasAsyncMethod()) { // 不能异步调用
//                throw new InitErrorException("Broadcast cluster can't use async call, " +
//                        "please set <jsf:consumer async=\"false\" />");
//            }
//            client = new BroadcastClient(consumerConfig);
        } else if (Constants.CLUSTER_TRANSPORT_RESETTABLE.equals(cluster)) {
            if(StringUtils.isBlank(consumerConfig.getUrl())){ // 必须直连
                throw new InitErrorException("[JSF-21310]Transport-resettable cluster must use direct call," +
                        "please set <jsf:consumer url=\"protocol://ip:address\" />");
            }
            client = new TransportResettableClient(consumerConfig);
        } else if (Constants.CLUSTER_TRANSPORT_PINPOINT.equals(cluster)) {
//            if(StringUtils.isBlank(consumerConfig.getUrl())){ // 必须直连
//                throw new InitErrorException("[JSF-21311]Transport-pinpoint cluster must use direct call," +
//                        "please set <jsf:consumer url=\"protocol://ip:address\" />");
//            }
            client = new TransportPinpointClient(consumerConfig);
		} else {
			throw new IllegalConfigureException(21309, "consumer.cluster", cluster);
		}

		return client;
	}
}