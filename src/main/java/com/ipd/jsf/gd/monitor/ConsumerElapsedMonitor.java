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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ConsumerElapsedMonitor implements Monitor<Invocation> {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerElapsedMonitor.class);

    /**
     * 接口名
     */
    private String interfaceId;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 服务端IP
     */
    private String providerIp;
    /**
     * 客户端IP
     */
    private String consumerIp;

    /**
     * 配置的指标数据Id
     */
    private int elaspedMetricsId;

    /**
     * 配置的指标数据，例如配置50,150 这里是[0,50,150,2147483647]
     */
    private int[] elasped_metrics;

    /**
     *  最小（含）_最大（不含）对应的计数器 例如 {"0_50":11,"50_150":222,"150_2147483647":33}
     */
    private AtomicInteger[] elapsed_distribution;

    /**
     * Instantiates a new Consumer elapsed monitor.
     *
     * @param interfaceId the interface id
     * @param methodName the method name
     * @param providerIp the provider ip
     * @param consumerIp the consumer ip
     */
    public ConsumerElapsedMonitor(String interfaceId, String methodName, String providerIp, String consumerIp) {
        this.interfaceId = interfaceId;
        this.methodName = methodName;
        this.providerIp = providerIp;
        this.consumerIp = consumerIp;
        String cfg = JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_MONITOR_ELASPED_METRIC, null);
        if (StringUtils.isEmpty(cfg)) {
            LOGGER.error("Metric of elapsed monitor is empty, please set \"mntr.elasped.metric\" " +
                    "at jsf admin website");
        } else {
            Map tmpmap = JsonUtils.parseObject(cfg, Map.class);
            String methodCfg = methodName == null ? null : (String) tmpmap.get("%" + methodName); // 方法有配置
            if (methodCfg == null) {
                methodCfg = (String) tmpmap.get("%*"); // 取下默认值
            }
            if (methodCfg == null) {
                LOGGER.error("Metric of elapsed monitor is empty, please set \"mntr.elasped.metric\" " +
                        "of " + interfaceId + "." + methodName + " at jsf admin website");
                elasped_metrics = new int[0];
                elapsed_distribution = new AtomicInteger[0];
            } else {
                // 解析配置 格式为： id;m0,m1,m2
                String[] ss = methodCfg.split(";");
                elaspedMetricsId = Integer.parseInt(ss[0]);
                int[] ms = CommonUtils.parseInts(ss[1], ",");
                Arrays.sort(ms);
                int size = ms.length + 2;
                elasped_metrics = new int[size];
                elapsed_distribution = new AtomicInteger[size];
                elasped_metrics[0] = 0;
                System.arraycopy(ms, 0, elasped_metrics, 1, ms.length);
                elasped_metrics[size - 1] = Integer.MAX_VALUE;
                for (int i = 0; i < size; i++) {
                    elapsed_distribution[i] = new AtomicInteger(0);
                }
                LOGGER.debug("{}.{} {}<->{} Metric of consumer elapsed monitor is {}:{}",
                        new Object[]{interfaceId, methodName, providerIp, consumerIp,
                                elaspedMetricsId, Arrays.toString(elasped_metrics)});
            }
        }
    }

    @Override
    public void recordInvoked(Invocation invocation) {
        if (elasped_metrics.length == 0) {
            return;
        }
        Long elapse = (Long) invocation.getAttachment(Constants.INTERNAL_KEY_ELAPSED);
        // 遍历区间进行匹配
        int low = elasped_metrics[0];
        for (int i = 1; i < elasped_metrics.length; i++) {
            int high = elasped_metrics[i];
            if (elapse >= low && elapse < high) {
                // 命中区间 统计数量
                elapsed_distribution[i - 1].incrementAndGet();
                return;
            }
            low = high;
        }
    }

    @Override
    public void recordException(Invocation invocation, Throwable e) {
        // NOT SUPPORTED
    }

    @Override
    public MetricData sliceInvoked() {
        if (elapsed_distribution.length == 0) {
            return null;
        }
        JSFElapsedData data = new JSFElapsedData(interfaceId, methodName, providerIp, consumerIp);
        data.setElapsedMetricsId(elaspedMetricsId);
        int[] distribution = new int[elapsed_distribution.length];
        for (int i = 0; i < elapsed_distribution.length; i++) {
            AtomicInteger counter = elapsed_distribution[i];
            int count = counter.get();
            distribution[i] = count;
            if (count > 0) {
                counter.getAndAdd(-count); // 减去已经统计的值
            }
        }
        data.setDistribution(distribution);

        return data;
    }

    @Override
    public MetricData sliceException() {
        return null;
    }

    /**
     * Gets interface id.
     *
     * @return the interface id
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Sets interface id.
     *
     * @param interfaceId the interface id
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets provider ip.
     *
     * @return the provider ip
     */
    public String getProviderIp() {
        return providerIp;
    }

    /**
     * Sets provider ip.
     *
     * @param providerIp the provider ip
     */
    public void setProviderIp(String providerIp) {
        this.providerIp = providerIp;
    }

    /**
     * Gets consumer ip.
     *
     * @return the consumer ip
     */
    public String getConsumerIp() {
        return consumerIp;
    }

    /**
     * Sets consumer ip.
     *
     * @param consumerIp the consumer ip
     */
    public void setConsumerIp(String consumerIp) {
        this.consumerIp = consumerIp;
    }
}