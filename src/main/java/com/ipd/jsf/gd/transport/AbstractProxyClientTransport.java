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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import com.ipd.jsf.gd.client.MsgFuture;
import com.ipd.jsf.gd.error.ClientClosedException;
import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageBuilder;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: 代理客户端类型的短连接<br>
 * <p/>
 * Description: 例如webservice，rest等<br>
 * <p/>
 */
public abstract class AbstractProxyClientTransport extends AbstractClientTransport {

    /**
     * 代理类，例如cxf或resteasy生成的代理
     */
    private final Object proxy;

    /**
     * 是否已连接（默认可连接，直到连不上）
     */
    private boolean open;

    /**
     * Instantiates a new Abstract proxy client transport.
     *
     * @param transportConfig
     *         the transport config
     */
    public AbstractProxyClientTransport(ClientTransportConfig transportConfig) {
        super(transportConfig);
        Provider provider = transportConfig.getProvider();
        try {
            proxy = buildProxy(transportConfig);
        } catch (Exception e) {
            throw new InitErrorException("[JSF-21312]Fail to build proxy client of consumer!", e);
        }
        open = proxy != null && NetUtils.canTelnet(provider.getIp(), provider.getPort(),
                clientTransportConfig.getConnectionTimeout());
        super.remoteAddress = InetSocketAddress.createUnresolved(provider.getIp(), provider.getPort());
        super.localAddress = InetSocketAddress.createUnresolved(JSFContext.getLocalHost(), 0);// 端口不准
    }

    /**
     * 构造远程调用代理
     *
     * @param transportConfig
     *         the transport config
     * @return the object
     * @throws Exception
     *         the exception
     */
    protected abstract Object buildProxy(ClientTransportConfig transportConfig) throws Exception;

    @Override
    public void reconnect() {
        // 能telnet通
        Provider provider = clientTransportConfig.getProvider();
        open = NetUtils.canTelnet(provider.getIp(), provider.getPort(),
                clientTransportConfig.getConnectionTimeout());
    }

    @Override
    public void shutdown() {
        open = false;
    }

    @Override
    public MsgFuture sendAsyn(BaseMessage msg, int timeout) {
        throw new UnsupportedOperationException("Unsupported asynchronous RPC in short connection");
    }

    @Override
    public ResponseMessage send(BaseMessage msg, int timeout) {
        RequestMessage request = (RequestMessage) msg;
        Invocation invocation = request.getInvocationBody();
        ResponseMessage response = MessageBuilder.buildResponse(request);
        try {
            super.currentRequests.incrementAndGet();
            Method method = ReflectUtils.getMethod(invocation.getClazzName(),
                    invocation.getMethodName(), invocation.getArgsType());
            Object o = method.invoke(proxy, invocation.getArgs());
            response.setResponse(o);
            return response;
        } catch (InvocationTargetException e) { // 代理类包装的原因
            Throwable ie = e.getCause();
            if (ie != null) { // 包装的原因
                Throwable realexception = ie.getCause(); // 真正的原因
                if (realexception != null) {
                    if (realexception instanceof SocketTimeoutException) {
                        throw new ClientTimeoutException("[JSF-22120]Client read timeout!", realexception);
                    } else if (realexception instanceof ConnectException) {
                        open = false;
                        throw new ClientClosedException("[JSF-22121]Connect to remote " + clientTransportConfig.getProvider()
                                +" error!", realexception);
                    } else {
                        throw new RpcException("[JSF-22122]Send message to remote catch error: "
                                + realexception.getMessage(), realexception);
                    }
                } else {
                    throw new RpcException("[JSF-22122]Send message to remote catch error: " + ie.getMessage(), ie);
                }
            } else {
                throw new RpcException("[JSF-22122]Send message to remote catch error: " + e.getMessage(), e);
            }
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException("Fail to send message to remote", e);
        } finally {
            super.currentRequests.decrementAndGet();
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }
}