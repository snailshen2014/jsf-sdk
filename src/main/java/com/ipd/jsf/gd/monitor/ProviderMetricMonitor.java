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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: 监控实现类<br>
 * <p/>
 * Description: 记录各种指标和异常数据，提供切分方法<br>
 * <p/>
 */
public class ProviderMetricMonitor implements Monitor<Invocation> {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderMetricMonitor.class);

    /**
     * The Interface id.
     */
    private final String interfaceId;

    /**
     * The Method name.
     */
    private final String methodName;

    /**
     * The Host.
     */
    private final String host;

    /**
     * The Port.
     */
    private final int port;

    /**
     * 远程地址统计数据 key是远程ip地址
     */
    private ConcurrentHashMap<String, StatData> remoteStatData = new ConcurrentHashMap<String, StatData>();

    /**
     * The Exception times.
     */
    private AtomicInteger exceptionTimes = new AtomicInteger(0);

    /**
     * 各类异常统计数据 key是异常类名
     */
    private ConcurrentHashMap<String, AtomicInteger> exceptionStats = new ConcurrentHashMap<String, AtomicInteger>();


    /**
     * Instantiates a new JSF monitor.
     *
     * @param interfaceId
     *         the interface id
     * @param methodName
     *         the method name
     * @param host
     *         the host
     * @param port
     *         the port
     */
    public ProviderMetricMonitor(String interfaceId, String methodName, String host, int port) {
        this.interfaceId = interfaceId;
        this.methodName = methodName;
        this.host = host;
        this.port = port;
    }

    @Override
    public void recordInvoked(Invocation invocation) {
        InetSocketAddress remoteAddress = (InetSocketAddress) invocation.getAttachment(Constants.INTERNAL_KEY_REMOTE);
        String protocol = (String) invocation.getAttachment(Constants.INTERNAL_KEY_PROTOCOL);
        String alias = invocation.getAlias();
        String appid = (String) invocation.getAttachment(Constants.INTERNAL_KEY_APPID);
        if (remoteAddress != null) {
            // String remoteIp, String protocol, String alias, String appId
            String key = NetUtils.toIpString(remoteAddress) + "%" + StringUtils.defaultString(protocol)
                    + "%" + StringUtils.defaultString(alias) + "%" + StringUtils.defaultString(appid);
            StatData data = remoteStatData.get(key);
            if (data == null) { // 第一次得到此指标
                synchronized (this) {
                    if (data == null) {
                        data = new StatData(); // 初始化统计数据
                        StatData d = remoteStatData.putIfAbsent(key, data);
                        if (d != null) {
                            data = d;
                        }
                    }
                }
            }
            // 数据累加
            addData(invocation, data);
        }
    }

    /**
     * Add data.
     *
     * @param invocation
     *         the invocation
     * @param data
     *         the data
     */
    private void addData(Invocation invocation, StatData data) {

        Integer input = (Integer) invocation.getAttachment(Constants.INTERNAL_KEY_INPUT);
        Integer output = (Integer) invocation.getAttachment(Constants.INTERNAL_KEY_OUTPUT);
        Integer elapseL = (Integer) invocation.getAttachment(Constants.INTERNAL_KEY_ELAPSED);

        data.recordOnce(elapseL == null ? 0 : elapseL, input == null ? 0 : input, output == null ? 0 : output);
    }

    @Override
    public MetricData sliceInvoked() {
        JSFMetricData metricData = new JSFMetricData(interfaceId, methodName, host, port);
        metricData.setCollectTime(System.currentTimeMillis());
        for (Map.Entry<String, StatData> entry : remoteStatData.entrySet()) {
            StatData rData = entry.getValue();
            if (rData == null) continue;
            Integer[] data = rData.snapshot();
            if (data[0] == 0) continue;
            String key = entry.getKey();
            String[] keys = key.split("%", -1);
            // String remoteIp, String protocol, String alias, String appId
            RemoteStat r1 = new RemoteStat(keys[0], keys[1], keys[2], keys[3], data);
            metricData.addRemoteStat(r1);
            rData.reset(data);
        }
        return metricData;
    }

    /**
     * 指标值
     */
    static class StatData {
        /**
         * The Succ.
         */
        private AtomicInteger callTimes = new AtomicInteger(0); //调用次数

        /**
         * The Elapse.
         */
        private AtomicInteger elapse = new AtomicInteger(0); // 总耗时

        /**
         * The In.
         */
        private AtomicInteger in = new AtomicInteger(0); // 接收流量大小
        /**
         * The Out.
         */
        private AtomicInteger out = new AtomicInteger(0); // 发送流量大小

        /**
         * Instantiates a new Data.
         */
        protected StatData() {
        }

        /**
         * Record once.
         *
         * @param elapse
         *         the elapse
         * @param in
         *         the in
         * @param out
         *         the out
         */
        public void recordOnce(int elapse, int in, int out) {
            // 可能会有只执行部分添加时（例如2/4），另一个线程snapshot就可能截取到不正确的值
            this.callTimes.incrementAndGet();
            this.elapse.addAndGet(elapse);
            this.in.addAndGet(in);
            this.out.addAndGet(out);
        }

        /**
         * 得到当前数据快照
         *
         * @return the integer [ ]
         */
        public Integer[] snapshot() {
            Integer[] snap = new Integer[4];
            snap[0] = callTimes.get();
            snap[1] = elapse.get();
            snap[2] = in.get();
            snap[3] = out.get();
            return snap;

        }

        /**
         * 重设值（截取已取出的部分）
         *
         * @param data
         *         the data 已经获取的部分
         */
        public void reset(Integer[] data) {
            this.callTimes.addAndGet(-1 * data[0]);
            this.elapse.addAndGet(-1 * data[1]);
            this.in.addAndGet(-1 * data[2]);
            this.out.addAndGet(-1 * data[3]);
        }
    }

    @Override
    public void recordException(Invocation invocation, Throwable exception) {
        exceptionTimes.incrementAndGet();
        String exceptionName = exception.getClass().getCanonicalName();
        AtomicInteger count = exceptionStats.get(exceptionName);
        if (count == null) {
            count = new AtomicInteger(0);
            AtomicInteger d = exceptionStats.putIfAbsent(exceptionName, count);
            if (d != null) {
                count = d;
            }
        }
        count.incrementAndGet();

    }

    @Override
    public JSFExceptionData sliceException() {
        JSFExceptionData exceptionData = new JSFExceptionData(interfaceId, methodName, host, port);
        exceptionData.setCollectTime(System.currentTimeMillis());
        for (Map.Entry<String, AtomicInteger> entry : exceptionStats.entrySet()) {
            try {
                String key = entry.getKey();
                AtomicInteger aNum = entry.getValue();
                if (aNum != null) {
                    Integer num = aNum.get();
                    if (num > 0) {
                        ExceptionDetail detail = new ExceptionDetail(key, num);
                        exceptionData.addDetail(detail);
                    }
                    aNum.addAndGet(-1 * num);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        int times = exceptionTimes.get();
        exceptionData.setTimes(times); // 设置错误总次数
        exceptionTimes.addAndGet(-times); // 减去已经获取
        return exceptionData;
    }

    /**
     * 得到接口名称
     *
     * @return 接口名称
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * 得到
     * @return
     */
    public String getMethodName() {
        return methodName;
    }
}