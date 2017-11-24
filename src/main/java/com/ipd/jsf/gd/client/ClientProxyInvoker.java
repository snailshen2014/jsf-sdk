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
import java.util.Map;

import com.ipd.jsf.gd.filter.FilterChain;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.filter.ConsumerInvokeFilter;
import com.ipd.jsf.gd.server.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseListener;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: Consumer接到用户调用代理类，远程RPC调用的执行类<br>
 * <p/>
 * Description: 执行过滤器链，最后调用远程服务<br>
 * <p/>
 */
public class ClientProxyInvoker implements Invoker {
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientProxyInvoker.class);

    /**
     * 对应的客户端信息
     */
    private final ConsumerConfig consumerConfig;

    /**
     *
     */
    private Client client;

    /**
     * 过滤器执行链
     */
    private FilterChain filterChain;

    /**
     * 构造执行链
     *
     * @param consumerConfig
     *         调用端配置
     */
    public ClientProxyInvoker(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
        // 构建客户端
        this.client = consumerConfig.getClient();
        // 构造执行链,最底层是调用过滤器
        this.filterChain = FilterChain.buildConsumerChain(this.consumerConfig,
                new ConsumerInvokeFilter(consumerConfig, client));
    }

    /**
     * proxy拦截的调用
     *
     * @param request
     *         请求消息
     * @return 调用结果
     */
    @Override
    public ResponseMessage invoke(BaseMessage request) {
        RequestMessage requestMessage = (RequestMessage) request;
        String methodName = requestMessage.getMethodName();

        requestMessage.setAlias(consumerConfig.getAlias());
        requestMessage.setClassName(consumerConfig.getInterfaceId());
        
        MessageHeader header = requestMessage.getMsgHeader();
        // 是否缓存，减少valueof操作？
        header.setProtocolType(Constants.ProtocolType.valueOf(consumerConfig.getProtocol()).value());
        header.setCodecType(Constants.CodecType.valueOf(consumerConfig.getSerialization()).value());
        String compress = (String) consumerConfig.getMethodConfigValue(methodName,
                Constants.CONFIG_KEY_COMPRESS, consumerConfig.getCompress());
        if (compress != null) {
            header.setCompressType(Constants.CompressType.valueOf(compress).value());
        }
        header.addHeadKey(Constants.HeadKey.timeout, consumerConfig.getMethodTimeout(methodName));

        // 将接口的<jsf:param />的配置复制到invocation
        Map params = consumerConfig.getParameters();
        if (params != null) {
            requestMessage.getInvocationBody().addAttachments(params);
        }
        // 将方法的<jsf:param />的配置复制到invocation
        params = (Map) consumerConfig.getMethodConfigValue(methodName, Constants.CONFIG_KEY_PARAMS);
        if (params != null) {
            requestMessage.getInvocationBody().addAttachments(params);
        }

        // 调用
        ResponseMessage response = filterChain.invoke(requestMessage);

        // 通知ResponseListener
        // 异步的改到msgfuture处返回才是真正的异步
        if (!consumerConfig.getMethodAsync(methodName)) {
            notifyResponseListener(methodName, response);
        }

        // 得到结果
        return response;
    }

    /**
     * 通知响应监听器
     *
     * @param response
     *         响应结果
     * @see AsyncResultListener#operationComplete(MsgFuture)
     */
    private void notifyResponseListener(String methodName, ResponseMessage response){
        // 返回结果增加事件监听
        List<ResponseListener> onreturn = consumerConfig.getMethodOnreturn(methodName);
        if (onreturn != null && !onreturn.isEmpty()) {
            if (response.isError()) {
                Throwable responseException = response.getException();
                for (ResponseListener responseListener : onreturn) {
                    try {
                        responseListener.catchException(responseException);
                    } catch (Exception e) {
                        LOGGER.warn("notify response listener error", e);
                    }
                }
            } else {
                Object result = response.getResponse();
                for (ResponseListener responseListener : onreturn) {
                    try {
                        responseListener.handleResult(result);
                    } catch (Exception e) {
                        LOGGER.warn("notify response listener error", e);
                    }
                }
            }
        }
    }

    /**
     * @return the consumerConfig
     */
    public ConsumerConfig<?> getConsumerConfig() {
        return consumerConfig;
    }

    /**
     * 切换客户端
     *
     * @param newClient
     *         新客户端
     * @return 旧客户端
     */
    public Client setClient(Client newClient) {
        // 构造执行链,最底层是调用过滤器
        FilterChain newChain = FilterChain.buildConsumerChain(this.consumerConfig,
                new ConsumerInvokeFilter(consumerConfig, newClient));
        // 开始切换
        Client old = this.client;
        this.client = newClient;
        this.filterChain = newChain;
        return old;
    }
}