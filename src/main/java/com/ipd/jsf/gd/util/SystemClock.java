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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Title:系统时钟<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class SystemClock {

    private final long precision;
    private final AtomicLong now;
    private static final Logger logger = LoggerFactory.getLogger(SystemClock.class);
    private ScheduledService scheduler = null;


    public SystemClock(long precision) {
        this.precision = precision <= 0 ? 0 : precision;
        now = new AtomicLong(System.currentTimeMillis());
        if (this.precision > 0) {
            scheduleClockUpdating();
        }
    }

    private void scheduleClockUpdating() {
        InTimer iTimer = new InTimer(now);
        scheduler = new ScheduledService("System Clock", ScheduledService.MODE_FIXEDRATE,
                iTimer, precision, precision, TimeUnit.MILLISECONDS).start();
    }

    public long now() {
        return precision == 0 ? System.currentTimeMillis() : now.get();
    }

    public long precision() {
        return precision;
    }

    class InTimer implements Runnable{
        private final AtomicLong inNow;

        private Integer count = 0;

        public InTimer(AtomicLong nowLong){
             inNow = nowLong;
        }

        @Override
        public void run() {
            try {
                inNow.set(System.currentTimeMillis());
            } catch (Throwable e) {
                count++;
                if(count % 100 == 1){
                    logger.error(e.getMessage(),e);
                }
                if(count>10000) count = 0;
            }
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (Exception e) {
                logger.warn("Failed to shutdown system clock", e);
            }
        }
    }
}