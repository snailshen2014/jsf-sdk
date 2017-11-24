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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 基于监控服务的限制过滤器<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class MonitorInvokeLimiter implements Limiter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorInvokeLimiter.class);

    /**
     * 是否可调用，默认是
     */
    private boolean canInvoke = true;

    /**
     * 关键字
     */
    private String key;

    /**
     *
     * @param key
     */
    public MonitorInvokeLimiter(String key) {
        this.key = key;
    }

    @Override
    public boolean isOverLimit(String interfaceId, String methodName, String alias, String appId) {
        return !canInvoke;
    }

    @Override
    public String getDetails() {
        return "MonitorInvokeLimiter:" + canInvoke;
    }

    /**
     * 设置是否可以调用
     *
     * @param canInvoke
     */
    public void setCanInvoke(boolean canInvoke) {
        if (this.canInvoke != canInvoke) {
            LOGGER.info("Monitor limiter of " + key + ": canInvoke changed from {} to {}",
                    this.canInvoke, canInvoke);
            this.canInvoke = canInvoke;
        }
    }
}