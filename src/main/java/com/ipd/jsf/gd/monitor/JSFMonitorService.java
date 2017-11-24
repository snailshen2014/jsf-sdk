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

import java.util.List;

/**
 * Title: JSF监控接口<br>
 * <p/>
 * Description: 包含收集指标和异常数据方法<br>
 * <p/>
 */
public interface JSFMonitorService {

    /**
     * 收集监控到的指标数据
     *
     * @param metricDatas
     *         错误数据list
     */
    public void collect(List<JSFMetricData> metricDatas);

    /**
     * 收集监控到的错误数据
     *
     * @param exceptionDatas
     *         错误数据list
     */
    public void collectException(List<JSFExceptionData> exceptionDatas);

    /**
     * 客户端收集服务端状态数据
     *
     * @param statusDatas
     */
    public void collectStatus(List<JSFStatusData> statusDatas);

    /**
     * 客户端收集服务端状态数据
     *
     * @param statusDatas
     */
    public void collectElapsedTime(List<JSFElapsedData> statusDatas);
}