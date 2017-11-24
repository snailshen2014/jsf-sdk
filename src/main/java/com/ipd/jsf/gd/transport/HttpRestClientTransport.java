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
package com.ipd.jsf.gd.transport;

import java.util.concurrent.TimeUnit;

import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * Title: 基于resteasy的客户端代理<br>
 * <p/>
 * Description: 生成代理类，无法做到方法级的超时配置<br>
 * <p/>
 */
public class HttpRestClientTransport extends AbstractProxyClientTransport {

    public HttpRestClientTransport(ClientTransportConfig clientTransportConfig) {
        super(clientTransportConfig);
    }

    @Override
    protected Object buildProxy(ClientTransportConfig transportConfig) throws Exception{
        ResteasyClient client = new ResteasyClientBuilder()
                .establishConnectionTimeout(transportConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .socketTimeout(transportConfig.getInvokeTimeout(), TimeUnit.MILLISECONDS)
		        .connectionPoolSize(clientTransportConfig.getClientBusinessPoolSize())// 连接池？
                .build();

        Provider provider = transportConfig.getProvider();
        String url = "http://" + provider.getIp() + ":" + provider.getPort()
                + "/" + StringUtils.trimToEmpty(provider.getPath());
        ResteasyWebTarget target = client.target(url);
        return target.proxy(ClassLoaderUtils.forName(provider.getInterfaceId()));
    }
}