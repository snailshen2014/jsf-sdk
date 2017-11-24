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

import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.Constants;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.ipd.jsf.gd.error.InitErrorException;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class HttpWsClientTransport extends AbstractProxyClientTransport {

    private final ExtensionManagerBus bus;

    /**
     * Instantiates a new Abstract proxy client transport.
     *
     * @param transportConfig
     *         the transport config
     */
    public HttpWsClientTransport(ClientTransportConfig transportConfig) {
        super(transportConfig);
        this.bus = new ExtensionManagerBus();
        this.bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
    }

    @Override
    protected Object buildProxy(ClientTransportConfig transportConfig) throws Exception {

        Provider provider = transportConfig.getProvider();
        Constants.ProtocolType protocolType = provider.getProtocolType();
        String interfaceId = provider.getInterfaceId();
        Class interfaceClass = ClassLoaderUtils.forName(interfaceId);
        String url = "http://" + provider.getIp() + ":" + provider.getPort() + "/" + interfaceId + "?wsdl";
        ClientProxyFactoryBean proxyFactoryBean = null;
        if (protocolType == Constants.ProtocolType.webservice) {
            proxyFactoryBean = new ClientProxyFactoryBean();
        } else if (protocolType == Constants.ProtocolType.jaxws) {
            proxyFactoryBean = new JaxWsProxyFactoryBean();
        } else {
            throw new InitErrorException("wrong protocol type of webservice");
        }

        proxyFactoryBean.setAddress(url);
        proxyFactoryBean.setServiceClass(interfaceClass);
        proxyFactoryBean.setBus(bus);
        Object ref = proxyFactoryBean.create();
        Client proxy = ClientProxy.getClient(ref);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(transportConfig.getConnectionTimeout());
        policy.setReceiveTimeout(transportConfig.getInvokeTimeout());
        conduit.setClient(policy);
        return ref;
    }
}