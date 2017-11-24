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
import com.ipd.jsf.gd.config.ConsumerGroupConfig;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.server.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Title: Consumer接到用户调用代理类，远程RPC调用的执行类<br>
 * <p/>
 * Description: 执行过滤器链，最后调用远程服务<br>
 * <p/>
 */
public class MultiClientProxyInvoker implements Invoker {
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MultiClientProxyInvoker.class);

    /**
     * 对应的客户端信息
     */
    private final ConsumerGroupConfig consumerGroupConfig;

    /**
     * 分组路由规则
     */
    private final GroupRouter groupRouter;

    /**
     * 构造执行链
     * @param groupConfig
     *         调用端配置
     * @param groupRouter
     *         分组路由规则
     */
    public MultiClientProxyInvoker(ConsumerGroupConfig groupConfig,
                                   GroupRouter groupRouter) {
        this.consumerGroupConfig = groupConfig;
        this.groupRouter = groupRouter;
        this.consumerGroupConfig.getConfigValueCache(true);
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
        Map<String, ConsumerConfig> aliasConfigMap = consumerGroupConfig.getConsumerConfigs();
        RequestMessage requestMessage = (RequestMessage) request;
        String alias = groupRouter.router(requestMessage.getInvocationBody(), consumerGroupConfig);
        if (alias == null) {
            throw new RpcException("[JSF-22107]Group router return null when select alias");
        }
        ConsumerConfig consumerConfig = aliasConfigMap.get(alias);
        if (consumerConfig == null) {
            // 此时要判断是否存在
            if (consumerGroupConfig.isAliasAdaptive()) { // 不存在，且可以自适应
                consumerGroupConfig.addConsumerConfig(alias); // 第一次初始化一下。
                consumerConfig = aliasConfigMap.get(alias);
            } else {
                throw new RpcException("[JSF-22108]Not found alias named " + alias + " from consumer group config, " +
                        "may be wrong destined alias/param, current is: " + consumerGroupConfig.getAlias() +
                        ". You can add alias automatically by <jsf:consumerGroup aliasAdaptive=\"true\"/>.");
            }
        }
        // 得到结果
        return consumerConfig.getProxyInvoker().invoke(request);
    }
}