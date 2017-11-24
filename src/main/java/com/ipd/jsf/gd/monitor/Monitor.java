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

/**
 * Title: 监控器<br>
 * <p/>
 * Description: 方法级的监控收集器，支持记录+数据分片<br>
 * <p/>
 */
public interface Monitor<Origin> {

    /**
     * 记录数据
     *
     * @param invocation
     *         调用包装对象
     */
    public void recordInvoked(Origin invocation);

    /**
     * 记录异常
     *
     * @param invocation
     *         调用包装对象
     * @param e
     *         异常
     */
    public void recordException(Origin invocation, Throwable e);


    /**
     * 切片，把已有监控数据封装为MetricData，清空当前统计数据
     *
     * @return 监控统计数据
     */
    public MetricData sliceInvoked();

    /**
     * 切片，把已有异常数据封装为ExceptionData，清空当前统计数据
     *
     * @return 监控异常数据
     */
    public MetricData sliceException();

}