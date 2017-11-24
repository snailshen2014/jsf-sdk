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
import java.util.concurrent.locks.ReentrantLock;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: 可以运行时替换transport的client,，配置为resett    able<br>
 * <p/>
 * Description: 切换中无法调用<br>
 * <p/>
 */
public class TransportResettableClient extends FailoverClient {

    private volatile ReentrantLock lock = new ReentrantLock();

    public TransportResettableClient(ConsumerConfig config) {
        super(config);
    }

    @Override
    public ResponseMessage doSendMsg(RequestMessage msg) {
        if (lock.isLocked()) {
            throw new RpcException("[JSF-22105]Transport resettable client is resetting transports...");
        }
        return super.doSendMsg(msg);
    }

    /**
     * 重置客户端连接
     *
     * @param providers
     *         新的客户端列表
     */
    public void resetTransport(List<Provider> providers) {
        lock.lock();
        try {
            super.closeTransports(); // 关闭旧的
            super.connectToProviders(providers); // 连接新的
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭连接
     */
    public void closeTransports() {
        super.closeTransports();
    }

    /**
     * 重置客户端连接
     *
     * @param url
     *         新的地址
     */
    public void resetTransport(String url) {
        List<Provider> tmpProviderList = new ArrayList<Provider>();
        if (StringUtils.isNotEmpty(url)) {
            Constants.ProtocolType pt = Constants.ProtocolType.valueOf(consumerConfig.getProtocol());
            String[] providerStrs = StringUtils.splitWithCommaOrSemicolon(url);
            for (int i = 0; i < providerStrs.length; i++) {
                Provider provider = Provider.valueOf(providerStrs[i]);
                if (provider.getProtocolType() != pt) {
                    throw new IllegalConfigureException(21308, "consumer.url", url,
                            "there is a mismatch protocol between url[" + provider.getProtocolType()
                                    + "] and consumer[" + pt + "]"
                    );
                }
                tmpProviderList.add(provider);
            }
        }
        resetTransport(tmpProviderList);
    }
}