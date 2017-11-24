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

 import com.ipd.jsf.gd.msg.Invocation;
 import com.ipd.jsf.gd.msg.ResponseMessage;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.RpcException;
 import com.ipd.jsf.gd.msg.RequestMessage;

 /**
 * Title: 快速失败<br>
 *
 * Description: 不重试<br>
 */
public class FailFastClient extends Client {

     /**
      * slf4j Logger for this class
      */
     private final static Logger LOGGER = LoggerFactory.getLogger(FailFastClient.class);

	/**
	 * @param consumerConfig ConsumerConfig
	 */
	public FailFastClient(ConsumerConfig<?> consumerConfig) {
		super(consumerConfig);
	}

	@Override
	protected ResponseMessage doSendMsg(RequestMessage msg) {
        Connection connection = super.select(msg);
        Invocation invocation = msg.getInvocationBody();
        try {
            ResponseMessage result = super.sendMsg0(connection, msg);
            if (result != null) {
                return result;
            } else {
                throw new RpcException("[JSF-22101]Failed to call "+ invocation.getClazzName() + "." + invocation.getMethodName()
                    + " on remote server " + connection.getProvider() + ", return null");
            }
        } catch (Exception e) {
            throw new RpcException("[JSF-22103]Failed to call " + invocation.getClazzName() + "." + invocation.getMethodName()
                    + " on remote server: " + connection.getProvider() + ", cause by: "
                    + e.getClass().getName() + ", message is: " + e.getMessage(), e);
        }
    }
}