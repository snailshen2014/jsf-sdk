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
package com.ipd.jsf.gd.filter.limiter;

import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.Counter;
import com.ipd.jsf.gd.registry.RegistryFactory;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 基于计数器服务的限制器<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class CounterInvokeLimiter implements Limiter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CounterInvokeLimiter.class);

    /**
     * 本地计数器
     */
    private AtomicInteger localCnt = new AtomicInteger();

    /**
     * 批量调用次数
     */
    private int batchSize;

    /**
     * 上次返回结果
     */
    private volatile int lastResult = 0;

    public CounterInvokeLimiter() {
        // 批量调用次数
        batchSize = CommonUtils.parseInt(JSFContext.getGlobalVal("counter.batch", "1"), 1);
    }

    @Override
    public boolean isOverLimit(String interfaceName, String methodName, String alias, String appId) {

        if (localCnt.incrementAndGet() % batchSize == 0) {
            try {
                lastResult = getCounter().count(interfaceName, alias, methodName, batchSize, appId);
            } catch (Throwable e) {
                LOGGER.warn("Failed to invoke counter service", e);
                lastResult = 1;
            }
        }
        if (lastResult == -1) {
            return true;
        } else if (lastResult >= 100) {
            LOGGER.warn("Counter service return exception code {}", lastResult);
        }

        return false;
    }

    @Override
    public String getDetails() {
        return "CounterInvokeLimiter:" + lastResult;
    }

    /**
     * 远程计数器配置
     */
    private static ConsumerConfig<Counter> counterConfig;
    /**
     * 远程计数器
     */
    private static Counter counter;

    private static Counter getCounter() {
        if (counter == null) {
            synchronized (LOGGER) {
                if (counter == null) {
                    // CounterService调用超时时间
                    int timeout = CommonUtils.parseInt(JSFContext.getGlobalVal("counter.timeout", "50"), 50);

                    counterConfig = new ConsumerConfig<Counter>();
                    counterConfig.setRegistry(RegistryFactory.getRegistryConfigs());
                    counterConfig.setInterfaceId(Counter.class.getName());
                    counterConfig.setProtocol(Constants.DEFAULT_PROTOCOL);
                    counterConfig.setAlias(Constants.DEFAULT_PROTOCOL);
                    counterConfig.setTimeout(timeout);
                    counterConfig.setParameter(Constants.HIDDEN_KEY_MONITOR, "false");
                    counter = counterConfig.refer();
                }
            }
        }
        return counter;
    }

    /**
     * 释放资源
     */
    protected static void unrefer() {
        ConsumerConfig tmp = counterConfig;
        counterConfig = null;
        counter = null;
        try {
            tmp.unrefer();
        } catch (Exception e) {
            LOGGER.error("Exception when unrefer consumer config of Counter", e);
        }
    }
}