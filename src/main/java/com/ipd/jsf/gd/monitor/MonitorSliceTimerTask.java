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
package com.ipd.jsf.gd.monitor;

import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.util.DateUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Title: 监控数据切分定时器<br>
 * <p/>
 * Description: 对监控数据按指定时间定时切分，将切下来的数据丢到发送队列<br>
 * <p/>
 */
public class MonitorSliceTimerTask {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorSliceTimerTask.class);
    /**
     * jsf Logger for this class
     */
    private final static JSFLogger JSF_LOGGER = JSFLoggerFactory.getLogger(MonitorSendTimerTask.class);
    /**
     * 单例模式
     */
    private static MonitorSliceTimerTask monitorSliceTimerTask = new MonitorSliceTimerTask();
    /**
     * 定时任务执行器
     */
    private volatile ScheduledService sliceFuture;

    /**
     * 数据切片时间，默认1分钟
     */
    private int sliceInterval = DateUtils.MILLISECONDS_PER_MINUTE * 1;

    /**
     * 总运行次数
     */
    private final AtomicInteger runtimes = new AtomicInteger(0);

    /**
     * 构造函数
     */
    private MonitorSliceTimerTask() {
        start();
    }

    /**
     * 启动定时分片线程
     */
    public MonitorSliceTimerTask start() {
        if (sliceFuture != null) {
            return this;
        }
        LOGGER.info("Start monitor data slice timertask...");
        Runnable sliceTask = new Runnable() {
            public void run() {
                try {
                    JSF_LOGGER.info("run monitor data slice pre {} ms", sliceInterval);
                    int time = runtimes.incrementAndGet();
                    // 对Monitor进行切片
                    for (Map.Entry<String, Monitor> entry : MonitorFactory.MONITOR_CACHE.entrySet()) {
                        String key = entry.getKey();
                        try {
                            Monitor monitor = entry.getValue();
                            int sliceInterval; // 发送间隔，是分钟的倍数
                            if (monitor instanceof ProviderMetricMonitor) {
                                ProviderMetricMonitor mntr = (ProviderMetricMonitor) monitor;
                                sliceInterval = MonitorFactory.getMonitorSliceInterval(mntr.getInterfaceId(),
                                        mntr.getMethodName());
                                if (time % sliceInterval == 0) { // 命中
                                    // 切分上一统计时间的调用数据，加入发送队列
                                    JSFMetricData metricDatas = (JSFMetricData) monitor.sliceInvoked();
                                    if (metricDatas != null) {
                                        MonitorSendTimerTask.getInstance().addMetricDatas(metricDatas);
                                    }
                                    // 切分上一统计时间的异常数据，加入发送队列
                                    JSFExceptionData exceptionDatas = (JSFExceptionData) monitor.sliceException();
                                    if (exceptionDatas != null) {
                                        MonitorSendTimerTask.getInstance().addExceptionDatas(exceptionDatas);
                                    }
                                }
                            } else if (monitor instanceof ConsumerStatusMonitor) {
                                ConsumerStatusMonitor mntr = (ConsumerStatusMonitor) monitor;
                                sliceInterval = MonitorFactory.getMonitorSliceInterval(mntr.getInterfaceId(),
                                        MonitorFactory.STATUS_FLAG);
                                if (time % sliceInterval == 0) { // 命中
                                    // 切分上一统计时间的状态数据，加入发送队列
                                    JSFStatusData statusDatas = (JSFStatusData) monitor.sliceInvoked();
                                    if (statusDatas != null) {
                                        MonitorSendTimerTask.getInstance().addStatusDatas(statusDatas);
                                    }
                                }
                            } else if (monitor instanceof ConsumerElapsedMonitor) {
                                ConsumerElapsedMonitor mntr = (ConsumerElapsedMonitor) monitor;
                                sliceInterval = MonitorFactory.getMonitorSliceInterval(mntr.getInterfaceId(),
                                        "%" + mntr.getMethodName(), "%*");
                                if (time % sliceInterval == 0) { // 命中
                                    // 切分上一统计时间的状态数据，加入发送队列
                                    JSFElapsedData elapsedData = (JSFElapsedData) monitor.sliceInvoked();
                                    if (elapsedData != null) {
                                        MonitorSendTimerTask.getInstance().addElapsedDatas(elapsedData);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Slice monitor of " + key + " error", e);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error("Slice monitor error", e);
                }
            }
        };
        // 开始执行
        sliceFuture = new ScheduledService("JSF-MNTR-SLICE", ScheduledService.MODE_FIXEDRATE,
                sliceTask,
                DateUtils.getDelayToNextMinute(JSFContext.systemClock.now()),//到下一份钟开始
                sliceInterval, TimeUnit.MILLISECONDS).start();
        return this;
    }

    /**
     * Sets slice interval.
     *
     * @param sliceInterval
     *         the slice interval
    private void changeSliceInterval(int sliceInterval) {
        if (this.sliceInterval != sliceInterval) {
            if (sliceFuture != null) {
                sliceFuture.cancel(false); // 结束当前
                sliceFuture = null;
            }
            start(); // 重新启动
            LOGGER.info("slice interval changed from {} to {}", this.sliceInterval, sliceInterval);
            this.sliceInterval = sliceInterval;
        }
    }*/

    /**
     * 关闭定时发送
     */
    public void stop() {
        if (sliceFuture != null) {
            sliceFuture.stop();
            sliceFuture = null;
        }
    }

    public static MonitorSliceTimerTask getInstance() {
        return monitorSliceTimerTask;
    }
}