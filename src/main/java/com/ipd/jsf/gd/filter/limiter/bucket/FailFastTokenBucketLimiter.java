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
package com.ipd.jsf.gd.filter.limiter.bucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: <br>
 * <p>
 * Description: <br>
 * </p>
 *
 * @since 2016/04/26 21:31
 */
public class FailFastTokenBucketLimiter extends AbstractTokenBucketLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailFastTokenBucketLimiter.class);

    @Override
    public double getToken(double requiredToken) {
        long nowMicros = duration();
        synchronized (mutex){
            syncAvailableToken(nowMicros);
            double tokenPermitted = Math.min(requiredToken, availableTokens);
            double needNewToken = requiredToken - tokenPermitted;
            if (needNewToken > 0){
                LOGGER.trace("no token.needNewToken:{},tokenPermitted:{}",needNewToken,tokenPermitted);
                throw new LimitedException(String.format("[JSF-22211]Invoked exceed the provider limit[%s]",this.maxTokens));
            }
            availableTokens -= tokenPermitted;
        }
        return 0;
    }
}