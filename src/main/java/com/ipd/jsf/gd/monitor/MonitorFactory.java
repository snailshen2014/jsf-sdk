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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 监控器工厂类<br>
 * <p/>
 * Description: 根据请求，得到方法级的监控<br>
 * <p/>
 */
public final class MonitorFactory {

    /**
     * monitor缓存
     */
    protected final static ConcurrentHashMap<String, Monitor> MONITOR_CACHE = new ConcurrentHashMap<String, Monitor>();
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorFactory.class);
    /**
     * 生成的锁
     */
    private final static Lock LOCK = new ReentrantLock();
    /**
     * 定时发送任务
     */
    private static volatile MonitorSendTimerTask monitorSendTimerTask;
    /**
     * 定时切片任务
     */
    private static volatile MonitorSliceTimerTask monitorSliceTimertask;

    /**
     * 状态监控标识位
     */
    public static final String STATUS_FLAG = "#status";

    /**
     * Instantiates a new Monitor factory.
     */
    private MonitorFactory() {

    }

    /**
     * 得到监控器
     *
     * @param type
     *         监控类型
     * @param interfaceId
     *         接口
     * @param args
     *         其它附加参数
     * @return Monitor
     */
    public static Monitor getMonitor(int type, String interfaceId, Object... args) {
        String key = null;
        switch (type) {
            case MONITOR_PROVIDER_METRIC:
                // key = interfaceId + ":" + methodName + ":" + ip + ":" + port;
                key = interfaceId + ":" + args[0] + ":" + args[1] + ":" + args[2];
                break;
            case MONITOR_CONSUMER_STATUS:
                key = interfaceId + ":" + STATUS_FLAG;
                break;
            case MONITOR_CONSUMER_ELAPSED:
                // key = interfaceId + ":%" + methodName + ":" + providerIp + ":" + consumerIp;
                key = interfaceId + ":%" + args[0] + ":" + args[1] + ":" + args[2];
                break;
            default:
                throw new IllegalArgumentException("Unknown monitor type :" + type);
        }
        Monitor monitor = MONITOR_CACHE.get(key);
        if (monitor != null) {
            return monitor;
        } else {
            LOCK.lock(); // 加锁
            try {
                monitor = MONITOR_CACHE.get(key);
                if (monitor == null) {
                    initTask();
                    switch (type) {
                        case MONITOR_PROVIDER_METRIC:
                            monitor = new ProviderMetricMonitor(interfaceId, (String) args[0],
                                    (String) args[1], (Integer) args[2]);
                            break;
                        case MONITOR_CONSUMER_STATUS:
                            monitor = new ConsumerStatusMonitor(interfaceId);
                            break;
                        case MONITOR_CONSUMER_ELAPSED:
                            monitor = new ConsumerElapsedMonitor(interfaceId, (String) args[0],
                                    (String) args[1], (String) args[2]);
                            break;
                    }
                    Monitor mntr = MONITOR_CACHE.putIfAbsent(key, monitor);
                    if (mntr != null) {
                        monitor = mntr;
                    } else {
                        LOGGER.info("build new monitor for {}, type: {}", key, type);
                    }
                }
                return monitor;
            } finally {
                LOCK.unlock();
            }
        }
    }

    /**
     * 初始化定时任务
     */
    private static void initTask(){
        if (monitorSendTimerTask == null) {
            monitorSendTimerTask = MonitorSendTimerTask.getInstance();
        }
        if (monitorSliceTimertask == null) {
            monitorSliceTimertask = MonitorSliceTimerTask.getInstance();
        }
    }

    /**
     * 销毁全部
     */
    public static void destroyAll() {
        LOGGER.info("Destroy all monitor and it's task");
        MONITOR_CACHE.clear();
        MONITOR_OPEN_CACHE.clear();
        if (monitorSendTimerTask != null) {
            monitorSendTimerTask.stop();
        }
        if (monitorSliceTimertask != null) {
            monitorSliceTimertask.stop();
        }
    }

    /**
     * 是否开启监控缓存{接口+方法名：是否开启}
     */
    private final static Map<String, Boolean> MONITOR_OPEN_CACHE = new ConcurrentHashMap<String, Boolean>();

    /**
     * 该接口该方法是否开启监控
     *
     * @param interfaceId
     *         接口名称
     * @param methodName
     *         方法名称
     * @return the boolean
     */
    public static boolean isMonitorOpen(String interfaceId, String methodName) {
        return isMonitorOpen(interfaceId, methodName, "*");
    }

    /**
     * 该接口该方法是否开启监控
     *
     * @param interfaceId
     *         接口名称
     * @param methodName
     *         方法名称
     * @param wildcard
     *         找不到使用通配符
     * @return the boolean
     */
    public static boolean isMonitorOpen(String interfaceId, String methodName, String wildcard) {
        String key = buildKey(interfaceId, methodName);
        Boolean isopen = MONITOR_OPEN_CACHE.get(key);
        if (isopen == null) {
            // 全局打开，且接口打开？
            String globalOpen = JSFContext.getGlobalVal(Constants.SETTING_MONITOR_GLOBAL_OPEN, "false");
            if (CommonUtils.isTrue(globalOpen)) { // 全局打开
                String interfaceOpen = JSFContext.getInterfaceVal(interfaceId,
                        Constants.SETTING_MONITOR_OPEN, null);
                if (StringUtils.isNotBlank(interfaceOpen)) { // 接口配置
                    if (CommonUtils.isTrue(interfaceOpen)) {
                        isopen = true;
                    } else if (CommonUtils.isFalse(interfaceOpen)) {
                        isopen = false;
                    } else {
                        try {
                            Map tmpmap = JsonUtils.parseObject(interfaceOpen, Map.class);
                            Object methodopen = tmpmap.get(methodName); // 方法有配置
                            if (methodopen == null && wildcard != null) {
                                methodopen = tmpmap.get(wildcard);
                            }
                            isopen = CommonUtils.isTrue(methodopen == null ? null : methodopen.toString());
                            if (isopen) { // 接口方法打开
                                String whitelist = JSFContext.getInterfaceVal(interfaceId,
                                        Constants.SETTING_MONITOR_WHITELIST, null);
                                // 看是否在白名单中
                                isopen = NetUtils.isMatchIPByPattern(whitelist, JSFContext.getLocalHost());
                            }
                        } catch (Exception e) {
                            // 非法json格式
                            LOGGER.error("Illegal monitor config " + Constants.SETTING_MONITOR_GLOBAL_OPEN
                                    + " of " + interfaceId + ": " + interfaceOpen + ", please check it", e);
                        }
                    }
                }
            }
            if (isopen == null) {
                isopen = false;
            }
            MONITOR_OPEN_CACHE.put(key, isopen);
        }
        return isopen;
    }

    /**
     * 发送监控间隔缓存{接口+方法名：发送间隔}
     */
    private final static Map<String, Integer> MONITOR_SLICE_INTERVAL_CACHE = new ConcurrentHashMap<String, Integer>();

    /**
     * 该接口该方法的执行间隔
     *
     * @param interfaceId
     *         接口名称
     * @param methodName
     *         方法名称
     * @return int间隔
     */
    public static int getMonitorSliceInterval(String interfaceId, String methodName) {
        return getMonitorSliceInterval(interfaceId, methodName, "*");
    }
    /**
     * 该接口该方法的执行间隔
     *
     * @param interfaceId
     *         接口名称
     * @param methodName
     *         方法名称
     * @param wildcard
     *         找不到使用通配符
     * @return int间隔
     */
    public static int getMonitorSliceInterval(String interfaceId, String methodName, String wildcard) {
        String key = buildKey(interfaceId, methodName);
        Integer sliceInterval = MONITOR_SLICE_INTERVAL_CACHE.get(key);
        if (sliceInterval == null) {
            String interfaceSlice = JSFContext.getInterfaceVal(interfaceId,
                    Constants.SETTING_MONITOR_SLICE_INTERVAL, null);
            if (StringUtils.isNotBlank(interfaceSlice)) { // 接口配置
                try {
                    Map tmpmap = JsonUtils.parseObject(interfaceSlice, Map.class);
                    Object methodSlice = methodName == null ? null : tmpmap.get(methodName); // 方法有配置
                    if (methodSlice == null && wildcard != null) {
                        methodSlice = tmpmap.get(wildcard);
                    }
                    if (methodSlice != null) {
                        sliceInterval = Integer.parseInt(methodSlice.toString());
                    }
                } catch (Exception e) {
                    LOGGER.error("Illegal monitor config " + Constants.SETTING_MONITOR_SLICE_INTERVAL
                            + " of " + interfaceId + ": " + interfaceSlice + ", please check it", e);
                }
            }
            if (sliceInterval == null || sliceInterval < 1) {
                sliceInterval = 1;
            }
            MONITOR_SLICE_INTERVAL_CACHE.put(key, sliceInterval);
        }
        return sliceInterval;
    }

    /**
     * 构建关键字
     *
     * @param interfaceId
     *         接口名称
     * @param methodName
     *         方法名称
     * @return 关键字
     */
    private static String buildKey(String interfaceId, String methodName) {
        return interfaceId + ":" + methodName;
    }

    /**
     * 该key是否属于该接口
     *
     * @param interfaceId
     *         接口名称
     * @param key
     *         关键字
     * @return
     */
    private static boolean isMatch(String interfaceId, String key) {
        return key.startsWith(interfaceId + ":");
    }

    /**
     * 清空缓存，用于得到配置变化通知的时候
     */
    public static void invalidateCache(String interfaceId) {
        // 全局变量修改
        if (interfaceId == null || Constants.GLOBAL_SETTING.equals(interfaceId)) {
            MONITOR_OPEN_CACHE.clear();
            MONITOR_SLICE_INTERVAL_CACHE.clear();
        }
        // 接口变量修改
        else {
            for (String key : MONITOR_OPEN_CACHE.keySet()) {
                if (isMatch(interfaceId, key)) {
                    MONITOR_OPEN_CACHE.remove(key);
                }
            }
            for (String key : MONITOR_SLICE_INTERVAL_CACHE.keySet()) {
                if (isMatch(interfaceId, key)) {
                    MONITOR_SLICE_INTERVAL_CACHE.remove(key);
                }
            }
        }
    }

    public static final int MONITOR_ALL = 0;
    public static final int MONITOR_PROVIDER_METRIC = 1;
    public static final int MONITOR_CONSUMER_STATUS = 2;
    public static final int MONITOR_CONSUMER_ELAPSED = 3;

    /**
     * 重建监控对象
     *
     * @param interfaceId
     *         接口
     * @param type
     *         类型 0全部 1服务端 2客户端耗时 3客户端异常
     */
    public static void invalidateMonitor(String interfaceId, int type) {
        switch (type) {
            case MONITOR_ALL:
                LOGGER.info("Invalidate all cached monitor of {}", interfaceId);
                MONITOR_CACHE.clear();
                break;
            case MONITOR_PROVIDER_METRIC:
                for (Map.Entry<String, Monitor> entry : MONITOR_CACHE.entrySet()) {
                    if (entry.getValue() instanceof ProviderMetricMonitor) {
                        ProviderMetricMonitor monitor = (ProviderMetricMonitor) entry.getValue();
                        if (monitor.getInterfaceId().equals(interfaceId)) {
                            MONITOR_CACHE.remove(entry.getKey());
                            LOGGER.info("Invalidate provider metric monitor of {}", entry.getKey());
                        }
                    }
                }
                break;
            case MONITOR_CONSUMER_STATUS:
                for (Map.Entry<String, Monitor> entry : MONITOR_CACHE.entrySet()) {
                    if (entry.getValue() instanceof ConsumerStatusMonitor) {
                        ConsumerStatusMonitor monitor = (ConsumerStatusMonitor) entry.getValue();
                        if (monitor.getInterfaceId().equals(interfaceId)) {
                            MONITOR_CACHE.remove(entry.getKey());
                            LOGGER.info("Invalidate consumer status monitor of {}", entry.getKey());
                        }
                    }
                }
                break;
            case MONITOR_CONSUMER_ELAPSED:
                for (Map.Entry<String, Monitor> entry : MONITOR_CACHE.entrySet()) {
                    if (entry.getValue() instanceof ConsumerElapsedMonitor) {
                        ConsumerElapsedMonitor monitor = (ConsumerElapsedMonitor) entry.getValue();
                        if (monitor.getInterfaceId().equals(interfaceId)) {
                            MONITOR_CACHE.remove(entry.getKey());
                            LOGGER.info("Invalidate consumer elapsed monitor of {}", entry.getKey());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}