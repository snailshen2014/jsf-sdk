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
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ConsumerStatusMonitor implements Monitor<ProviderStat> {

    /**
     * 一个接口一个
     */
    private String interfaceId;

    /**
     * Instantiates a new Status monitor.
     *
     * @param interfaceId the interface id
     */
    public ConsumerStatusMonitor(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    private ConcurrentHashMap<ProviderStat, AtomicInteger> providers = new ConcurrentHashMap<ProviderStat,
            AtomicInteger>();

    @Override
    public void recordInvoked(ProviderStat statusData) {
        AtomicInteger cnt = providers.get(statusData);
        if (cnt == null) {
            cnt = CommonUtils.putToConcurrentMap(providers, statusData, new AtomicInteger());
        }
        cnt.incrementAndGet();
    }

    @Override
    public void recordException(ProviderStat invocation, Throwable e) {
        // NOT SUPPORTED
    }

    @Override
    public MetricData sliceInvoked() {
        JSFStatusData jsfStatusData = new JSFStatusData(interfaceId);
        jsfStatusData.setCollectTime(System.currentTimeMillis());
        ConcurrentHashMap<ProviderStat, AtomicInteger> tmp = new ConcurrentHashMap<ProviderStat, AtomicInteger>
                (providers);
        providers.clear();
        for (Map.Entry<ProviderStat, AtomicInteger> entry : tmp.entrySet()) {
            ProviderStat rData = entry.getKey();
            int num = entry.getValue().get();
            if (num > 0) {
                rData.setNum(num);
                jsfStatusData.getProviders().add(rData);
            }
        }
        return jsfStatusData;
    }

    @Override
    public MetricData sliceException() {
        // NOT SUPPORTED
        return null;
    }

    /**
     * 得到接口名称
     *
     * @return 接口名称 interface id
     */
    public String getInterfaceId() {
        return interfaceId;
    }
}