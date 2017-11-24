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

import com.ipd.jsf.gd.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created on 14-6-10.
 *
 */
public class ResourceScheduleChecker {

    private static final Logger logger = LoggerFactory.getLogger(ResourceScheduleChecker.class);

    private static ScheduledService checkScheduledService;

    private static ScheduledService checkCallbackScheduledService;

    public static void resourceCheck(){
        // 检查已经超时的future
        checkScheduledService = new ScheduledService("JSF-Future-Checker", ScheduledService.MODE_FIXEDDELAY,
                new Runnable() {
            @Override
            public void run() {
                //check all the ClientTransport include the callback transport
                try {
                    ClientTransportFactory.checkFuture();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }, 1000, 1000, TimeUnit.MILLISECONDS).start();
        checkCallbackScheduledService = new ScheduledService("JSF-Future-Checker-CB", ScheduledService.MODE_FIXEDDELAY,
                new Runnable() {
            @Override
            public void run() {
                //check all the ClientTransport include the callback transport
                try {
                    CallbackUtil.checkTransportFutureMap();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }, 3, 3, TimeUnit.MINUTES).start();
    }

    public static void close() {
        if (checkScheduledService != null) {
            try {
                checkScheduledService.shutdown();
            } catch (Exception e) {
                logger.warn("Fail to close resource schedule checker, cause by :{}", e.getMessage());
            }
        }
        if (checkCallbackScheduledService != null) {
            try {
                checkCallbackScheduledService.shutdown();
            } catch (Exception e) {
                logger.warn("Fail to close resource schedule checker, cause by :{}", e.getMessage());
            }
        }
    }
}