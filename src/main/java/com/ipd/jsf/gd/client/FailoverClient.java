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

 import com.ipd.jsf.gd.error.RpcException;
 import com.ipd.jsf.gd.msg.Invocation;
 import com.ipd.jsf.gd.registry.Provider;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.ConsumerConfig;
 import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;

 /**
 * Title: 失败重试<br>
 *
 * Description: <br>
 */
public class FailoverClient extends Client {

	/**
	 * slf4j logger for this class
	 */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(FailoverClient.class);

	/**
	 * @param consumerConfig ConsumerConfig
	 */
	public FailoverClient(ConsumerConfig<?> consumerConfig) {
		super(consumerConfig);
	}

	@Override
	public ResponseMessage doSendMsg(RequestMessage msg) {
        Invocation invocation = msg.getInvocationBody();
        String methodName = invocation.getMethodName();
		int retries = consumerConfig.getMethodRetries(methodName);
		int time = 0;
        Throwable throwable = null;// 异常日志
        List<Provider> invokedProviders = new ArrayList<Provider>(retries + 1);
        do {
            Connection connection = super.select(msg, invokedProviders);
			try {
				ResponseMessage result = super.sendMsg0(connection, msg);
                if (result != null) {
                    if (throwable != null) {
                        LOGGER.warn("[JSF-22100]Although success by retry, last exception is: {}", throwable.getMessage());
                    }
                    return result;
                } else {
                    throwable = new RpcException("[JSF-22101]Failed to call "+ invocation.getClazzName() + "." + invocation.getMethodName()
                            + " on remote server " + connection.getProvider() + ", return null");
                }
            } catch (RpcException e) { // rpc异常重试
                throwable = e;
                time++;
			} catch (Exception e) { // 其它异常不重试
                throw new RpcException("[JSF-22102]Failed to call " + invocation.getClazzName() + "." + methodName
                        + " on remote server: " + connection.getProvider() + ", cause by unknown exception: "
                        + e.getClass().getName() + ", message is: " + e.getMessage(), e);
            }
            invokedProviders.add(connection.getProvider());
		} while (time <= retries);

        if (retries == 0) {
            throw new RpcException("[JSF-22103]Failed to call " + invocation.getClazzName() + "." + methodName
                    + " on remote server: " + invokedProviders + ", cause by: "
                    + throwable.getClass().getName() + ", message is: " + throwable.getMessage(), throwable);
        } else {
            throw new RpcException("[JSF-22104]Failed to call " + invocation.getClazzName() + "." + methodName
                    + " on remote server after retry "+ (retries + 1) + " times: "
                    + invokedProviders + ", last exception is cause by:"
                    + throwable.getClass().getName() + ", message is: " + throwable.getMessage(), throwable);
        }
	}
}