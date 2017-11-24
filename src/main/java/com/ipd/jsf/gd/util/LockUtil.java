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
package com.ipd.jsf.gd.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Title: 锁工具包<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class LockUtil {
    /**
     * 锁失败
     */
    public static final int LOCK_FAIL = -2;

    /**
     * 锁
     *
     * @param lock    锁
     * @param timeout 超时时间
     *                <li>>0 等待超时时间</li>
     *                <li>=0 无限等待</li>
     *                <li><0 无限等待</li>
     * @return 剩余的超时时间
     * <li>>0 锁成功，timeout>0，剩余超时时间</li>
     * <li>0 锁成功，timeout=0</li>
     * <li>-1 锁成功，timeout<0</li>
     *
     */
    public static long tryLock(final Lock lock, final long timeout) {
        long time;
        if (timeout > 0) {
            time = JSFContext.systemClock.now();
            try {
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    time = timeout - (JSFContext.systemClock.now() - time);
                    if (time > 0) {
                        return time;
                    }else{
                        lock.unlock();
                    }
                }
                return LOCK_FAIL;
            } catch (InterruptedException e) {
                return LOCK_FAIL;
            }
        } else {
            lock.lock();
            return timeout == 0 ? 0 : -1;
        }
    }
}