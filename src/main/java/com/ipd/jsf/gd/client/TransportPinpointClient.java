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

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.NoAliveProviderException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.transport.ClientTransport;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Title: 可以在调用前指定调用者的地址，配置为pinpoint<br>
 * <p/>
 * Description: <br>
 * <p/>
 * @see RpcContext#setAttachment(String, Object)
 * @see Constants#HIDDEN_KEY_PINPOINT
 */
public class TransportPinpointClient extends Client {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TransportPinpointClient.class);

    /**
     * Instantiates a new Transport pinpoint client.
     *
     * @param consumerConfig
     *         the consumer config
     */
    public TransportPinpointClient(ConsumerConfig consumerConfig) {
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
            String serverip = (String) invocation.getAttachment(Constants.HIDDEN_KEY_PINPOINT);
            Connection connection;
            if (StringUtils.isNotBlank(serverip)) { // 指定了调用地址
                Provider provider = selectProvider(serverip);
                if (provider == null) { // 指定的不存在
                    throw new NoAliveProviderException(consumerConfig.buildKey(), serverip);
                }
                ClientTransport transport = super.selectByProvider(msg, provider);
                if (transport == null) { // 指定的不存在或已死
                    // 抛出异常
                    throw new NoAliveProviderException(consumerConfig.buildKey(), serverip);
                } else {
                    connection = new Connection(provider, transport);
                }
            } else { //未指定调用地址
                connection = super.select(msg);
            }
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
                    + throwable.getClass().getName() + ", message is :" + throwable.getMessage(), throwable);
        } else {
            throw new RpcException("[JSF-22104]Failed to call " + invocation.getClazzName() + "." + methodName
                    + " on remote server after retry "+ (retries + 1) + " times: "
                    + invokedProviders + ", last exception is cause by: "
                    + throwable.getClass().getName() + ", message is: " + throwable.getMessage(), throwable);
        }
    }

    /**
     * The Provider map.
     */
    private Map<String, Provider> providerMap = new ConcurrentHashMap<String, Provider>();

    /**
     * Select provider.
     *
     * @param serverip
     *         the serverip
     * @return the provider
     */
    private Provider selectProvider(String serverip) {
        Provider p = providerMap.get(serverip);
        if (p == null) {
            Provider p1 = Provider.valueOf(serverip);
            for (Provider provider : connectionHolder.getAliveConnections().keySet()) {
                if (provider.getIp().equals(p1.getIp())
                        && provider.getProtocolType() == p1.getProtocolType()
                        && provider.getPort() == p1.getPort()) {
                    // 相等，就是你了
                    p = provider;
                    providerMap.put(serverip, p);
                    return p;
                }
            }
        }
        return p;
    }

    /**
     * Destroy void.
     */
    @Override
    public void destroy() {
        providerMap.clear();
        super.destroy();
    }
}