/**
 * Copyright 2004-2048 .
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yangyang115 on 17-1-10.
 */
public class JSFMonitorServiceSimpleImpl implements JSFMonitorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(JSFMonitorServiceSimpleImpl.class);

    @Override
    public void collect(List<JSFMetricData> metricDatas) {
        //TODO
    }

    @Override
    public void collectException(List<JSFExceptionData> exceptionDatas) {
        //TODO
    }

    @Override
    public void collectStatus(List<JSFStatusData> statusDatas) {
        //TODO
    }

    @Override
    public void collectElapsedTime(List<JSFElapsedData> statusDatas) {
        //TODO
    }

    private static class JSFMonitorServiceSimpleHolder {
        private static final JSFMonitorServiceSimpleImpl INSTANCE = new JSFMonitorServiceSimpleImpl();
    }

    public static JSFMonitorServiceSimpleImpl getInstance() {
        return JSFMonitorServiceSimpleHolder.INSTANCE;
    }
}
