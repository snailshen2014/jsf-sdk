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

import com.ipd.jsf.gd.filter.limiter.bucket.LimitedException;
import com.ipd.jsf.gd.filter.limiter.bucket.RateLimiter;

/**
 * Title: <br>
 * <p>
 * Description: <br>
 *     服务端限流
 * </p>
 *
 * @since 2016/04/27 15:59
 */
public class ProviderInvokerLimiter implements Limiter {

    private RateLimiter rateLimiter;

    /**
     * 限制次数/s
     */
    private int limit;

    public ProviderInvokerLimiter(int limit) {
        this.limit = limit;
        rateLimiter = RateLimiter.builder()
                .withTokePerSecond(this.limit)
                .withType(RateLimiter.RateLimiterType.FFTB)
                .build();
    }

    public void updateLimit(int limit){
        this.limit = limit;
        rateLimiter.setRate(limit);
    }

    @Override
    public boolean isOverLimit(String interfaceName, String methodName, String alias, String appId) {
        try {
            rateLimiter.getToken(1);
        } catch (LimitedException e){
            return true;
        }
        return false;
    }

    @Override
    public String getDetails() {
        return "ProviderLimit:"+limit;
    }

    public int getLimit() {
        return limit;
    }

}