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
package com.ipd.jsf.gd.filter;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.DateUtils;
import com.ipd.jsf.gd.util.ScheduledService;

/**
 * Title: 系统时间检测过滤器<br>
 * <p/>
 * Description: 如果发现系统时间被大幅度改动，需要重置定时任务<br>
 * <p/>
 */
public class SystemTimeCheckFilter extends AbstractFilter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(SystemTimeCheckFilter.class);

    /**
     * slf4j Logger for this class
     */
    private final static JSFLogger JSFLOGGER = JSFLoggerFactory.getLogger(SystemTimeCheckFilter.class);

    /**
     * 给注册中心的心跳间隔时间
     */
    private int heartbeatPeriod;

    /**
     * 最后重建时间
     */
    //private long lastResetTime;

    /**
     * 重置定时器间隔
     */
    //private final int resetLimit = 360000;  // 限制6分钟重置一次

    /**
     * 调用次数统计
     */
    private AtomicInteger invokeCount = new AtomicInteger(0);

    /**
     * Instantiates a new System time check filter.
     */
    public SystemTimeCheckFilter() {
        heartbeatPeriod = Math.max(5000, CommonUtils.parseInt(
                JSFContext.getGlobalVal(Constants.SETTING_REGISTRY_HEARTBEAT_INTERVAL, null), 30000));
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        if (invokeCount.incrementAndGet() % 10 == 0   // 1/10的概率去检测时间
                && !ScheduledService.isResetting()) {
            checkAndReset();
        }
        return getNext().invoke(request);
    }

    /**
     * 最后调用时间
     */
    private volatile static long lastInvokeTime = System.currentTimeMillis();

    /**
     * 检查，发现修改就重置
     */
    public void checkAndReset() {
        final long now = JSFContext.systemClock.now();
        JSFLOGGER.info("Check system time lastInvokeTime：{}, now：{}", lastInvokeTime, now);
        if (now < lastInvokeTime - 3 * heartbeatPeriod) {
            // 时间被往前改了，定时任务全部失效，并且改动变化还挺大，超过3个心跳周期
            if (!ScheduledService.isResetting()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.warn("System time has been modified from last invoke time {}" +
                                        " to the previous time {}",
                                DateUtils.dateToStr(new Date(lastInvokeTime)),
                                DateUtils.dateToStr(new Date(now)));
                        ScheduledService.reset(); // 重置全部
                        lastInvokeTime = now;
                    }
                }, "JSF-Reset-Scheduled").start();
            }
        } else {
            lastInvokeTime = now;
        }
    }
}