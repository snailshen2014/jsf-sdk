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

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.logger.JSFLogger;
import com.ipd.jsf.gd.logger.JSFLoggerFactory;
import com.ipd.jsf.gd.registry.RegistryFactory;
import com.ipd.jsf.gd.util.CircularQueue;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.ScheduledService;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Title: 监控数据发送定时任务<br>
 * <p/>
 * Description: 发送包括指标、异常、状态等数据<br>
 * <p/>
 */
public final class MonitorSendTimerTask {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorSendTimerTask.class);
    /**
     * jsf Logger for this class
     */
    private final static JSFLogger JSF_LOGGER = JSFLoggerFactory.getLogger(MonitorSendTimerTask.class);
    /**
     * 单例模式
     */
    private static MonitorSendTimerTask monitorSendTimerTask = new MonitorSendTimerTask();
    /**
     * 待发送的监控数据（服务端）
     */
    private final CircularQueue<JSFMetricData> metricDataQueue = new CircularQueue<JSFMetricData>(2000);
    /**
     * 待发送的异常数据（服务端）
     */
    private final CircularQueue<JSFExceptionData> exceptionDataQueue = new CircularQueue<JSFExceptionData>(2000);
    /**
     * 待发送的状态数据（客户端）
     */
    private final CircularQueue<JSFStatusData> statusDataQueue = new CircularQueue<JSFStatusData>(2000);
    /**
     * 待发送的耗时分布数据（客户端）
     */
    private final CircularQueue<JSFElapsedData> elapsedDataQueue = new CircularQueue<JSFElapsedData>(2000);

    /**
     * 远程rpc服务配置
     */
    private ConsumerConfig<JSFMonitorService> monitorConfig;
    /**
     * 远程rpc服务
     */
    private JSFMonitorService monitorService;
    /**
     * 统计信息发送Future
     */
    private volatile ScheduledService sendFuture;
    /**
     * 发送间隔
     */
    private int monitorInterval = 20000;

    /**
     * 构造函数
     */
    private MonitorSendTimerTask() {
        if (!CommonUtils.isUnitTestMode()) {
            try {
                LOGGER.info("Reference monitor service...");

                //monitorConfig = buildConfig();
                // refer monitor service
                //monitorService = monitorConfig.refer();
                monitorService = JSFMonitorServiceSimpleImpl.getInstance();

                String interval = JSFContext.getGlobalVal(Constants.SETTING_MONITOR_SEND_INTERVAL, "20000");
                monitorInterval = Math.max(CommonUtils.parseInt(interval, 20000), 10000);// 至少10秒

                // 启动发送线程
                start();
            } catch (Throwable t) {
                LOGGER.error("Failed to init MonitorSendTimerTask", t);
            }
        }
    }

    private ConsumerConfig<JSFMonitorService> buildConfig() {
        ConsumerConfig<JSFMonitorService> monitorConfig = new ConsumerConfig<JSFMonitorService>();
        monitorConfig.setInterfaceId(JSFMonitorService.class.getCanonicalName());
        monitorConfig.setRegistry(RegistryFactory.getRegistryConfigs());
        monitorConfig.setProtocol(Constants.DEFAULT_PROTOCOL);
        monitorConfig.setAlias("1.0.0");
        monitorConfig.setCluster(Constants.CLUSTER_FAILFAST);
        monitorConfig.setCheck(false);
        monitorConfig.setLazy(true);
        monitorConfig.setParameter(Constants.HIDDEN_KEY_MONITOR, "false");
        monitorConfig.setParameter(Constants.HIDDEN_KEY_DESTROY, "false");
        String url = JSFContext.getGlobalVal(Constants.SETTING_MONITOR_SEND_URL, null);
        if(StringUtils.isNotBlank(url)){
            LOGGER.info("Url of monitor service is specified : {}", url);
            monitorConfig.setUrl(url);
        }

        return monitorConfig;
    }

    /**
     * 单例模式
     *
     * @return the monitor send timer task
     */
    public static MonitorSendTimerTask getInstance() {
        return monitorSendTimerTask;
    }

    /**
     * 启动定时发送线程
     *
     * @return the monitor send timer task
     */
    public MonitorSendTimerTask start() {
        if (sendFuture != null) {
            return this;
        }
        Runnable sendTask = new Runnable() {
            @Override
			public void run() {
				if (monitorService != null) {
                    doSend();
				}
            }
        };
        // 定时发送
        sendFuture = new ScheduledService("JSF-MNTR-SEND", ScheduledService.MODE_FIXEDRATE,
                sendTask, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS).start();
        return this;
    }

    /**
     * 发送数据
     */
    private void doSend() {
        try {
            sendMetric(); // 发送监控信息
        } catch (Throwable t) {
            JSF_LOGGER.warn("Failed to send metric data to monitor server, cause: {}", t.getMessage());
        }
        try {
            sendException(); // 发送异常信息
        } catch (Throwable t) {
            JSF_LOGGER.warn("Failed to send exception data to monitor server, cause: {}", t.getMessage());
        }
        try {
            sendStatus(); // 发送状态信息
        } catch (Throwable t) {
            JSF_LOGGER.warn("Failed to send status data to monitor server, cause: {}", t.getMessage());
        }
        try {
            sendElapsed(); // 发送耗时分布信息
        } catch (Throwable t) {
            JSF_LOGGER.warn("Failed to send elapsed data to monitor server, cause: {}", t.getMessage());
        }
    }

    /**
     * 发送异常数据，每次发送小于等于50条，批量发送
     */
    private void sendException() {
        List<JSFExceptionData> exceptionDatas = null;
        if (exceptionDataQueue.peek() != null) {
            do {
                exceptionDatas = exceptionDataQueue.batchPoll(50);
                JSF_LOGGER.info("Send {} exception data to monitor server.", exceptionDatas.size());
                if (CommonUtils.isNotEmpty(exceptionDatas)) {
                    monitorService.collectException(exceptionDatas);
                }
            } while (exceptionDataQueue.peek() != null);
        }
    }

    /**
     * 发送监控数据，每次发送小于等于50条，批量发送
     */
    private void sendMetric() {
        List<JSFMetricData> monitorDatas = null;
        if (metricDataQueue.peek() != null) {
            do {
                monitorDatas = metricDataQueue.batchPoll(50);
                JSF_LOGGER.info("Send {} metric data to monitor server.", monitorDatas.size());
                if (CommonUtils.isNotEmpty(monitorDatas)) {
                    monitorService.collect(monitorDatas);
                }
            } while (metricDataQueue.peek() != null);
        }
    }

    /**
     * 发送状态数据，每次发送小于等于50条，批量发送
     */
    private void sendStatus() {
        List<JSFStatusData> statusDatas = null;
        if (statusDataQueue.peek() != null) {
            do {
                statusDatas = statusDataQueue.batchPoll(50);
                JSF_LOGGER.info("Send {} status data to monitor server.", statusDatas.size());
                if (CommonUtils.isNotEmpty(statusDatas)) {
                    monitorService.collectStatus(statusDatas);
                }
            } while (statusDataQueue.peek() != null);
        }
    }

    /**
     * 发送监控数据，每次发送小于等于50条，批量发送
     */
    private void sendElapsed() {
        List<JSFElapsedData> elapsedDatas = null;
        if (elapsedDataQueue.peek() != null) {
            do {
                elapsedDatas = elapsedDataQueue.batchPoll(50);
                JSF_LOGGER.info("Send {} elapsed data to monitor server.", elapsedDatas.size());
                if (CommonUtils.isNotEmpty(elapsedDatas)) {
                    monitorService.collectElapsedTime(elapsedDatas);
                }
            } while (elapsedDataQueue.peek() != null);
        }
    }

    /**
     * 关闭定时发送
     */
    public void stop() {
        if (monitorConfig != null) {
            if (monitorService != null) { // 关闭前再发一次
                doSend();
            }
            try {
                monitorConfig.unrefer();
            } catch (Exception e) {
                LOGGER.warn("unrefer monitor config error", e);
            }
            monitorService = null;
        }
        if (sendFuture != null) {
            sendFuture.stop();
            sendFuture = null;
        }
    }

    /**
     * 待发送监控数据入队列
     *
     * @param metricData
     *         监控数据
     */
    public void addMetricDatas(JSFMetricData metricData) {
        if (metricData != null && !metricData.isEmpty()) {
            // 超出队列大小，自动丢弃头部数据
            metricDataQueue.add(metricData);
        }
    }

    /**
     * 待发送异常数据入队列
     *
     * @param exceptionData
     *         异常数据
     */
    public void addExceptionDatas(JSFExceptionData exceptionData) {
        if (exceptionData != null && exceptionData.getTimes() > 0) {
            // 超出队列大小，自动丢弃头部数据
            exceptionDataQueue.add(exceptionData);
        }
    }

    /**
     * 待发送状态数据数据入队列
     *
     * @param statusData
     *         状态数据
     */
    public void addStatusDatas(JSFStatusData statusData) {
        if (statusData != null && !statusData.isEmpty()) {
            // 超出队列大小，自动丢弃头部数据
            statusDataQueue.add(statusData);
        }
    }

    /**
     * 待发送耗时分布数据入队列
     *
     * @param elapsedData
     *         耗时分布数据
     */
    public void addElapsedDatas(JSFElapsedData elapsedData) {
        if (elapsedData != null) {
            // 超出队列大小，自动丢弃头部数据
            elapsedDataQueue.add(elapsedData);
        }
    }
}