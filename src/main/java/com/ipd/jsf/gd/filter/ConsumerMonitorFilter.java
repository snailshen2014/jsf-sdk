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

import com.ipd.jsf.gd.monitor.Monitor;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 客户端收集监控过滤器<br>
 * <p/>
 * Description: 目前收集耗时数据<br>
 * <p/>
 */
public class ConsumerMonitorFilter extends AbstractFilter{

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        Invocation invocation = request.getInvocationBody();
        String className = request.getClassName();
        String methodName = request.getMethodName();
        // 是否强制关闭监控，默认开启
        String monitorCfg = (String) getConfigContext().get(Constants.HIDDEN_KEY_MONITOR);
        // 判断是否启动监控，如果启动则运行
        if (!CommonUtils.isFalse(monitorCfg) && MonitorFactory.isMonitorOpen(className, "%" + methodName, "%*")) {
            long start = JSFContext.systemClock.now();
            ResponseMessage responseMessage = getNext().invoke(request);
            long end = JSFContext.systemClock.now();
            String providerIp = RpcContext.getContext().getRemoteHostName();
            if (providerIp != null) { // 远程服务端为空，表示没有调用
                Monitor monitor = MonitorFactory.getMonitor(MonitorFactory.MONITOR_CONSUMER_ELAPSED,
                        className, methodName, providerIp, JSFContext.getLocalHost());
                if (monitor != null) { // 需要记录日志
                    boolean iserror = responseMessage.isError();
                    invocation.addAttachment(Constants.INTERNAL_KEY_ELAPSED, end - start);
                    //invocation.addAttachment(Constants.INTERNAL_KEY_RESULT, !iserror);
                    //invocation.addAttachment(Constants.INTERNAL_KEY_PROTOCOL, Constants.ProtocolType.jsf.value() + "");
                    if (iserror) { // 失败
                        //monitor.recordException(invocation, responseMessage.getException());
                    } else { // 成功
                        monitor.recordInvoked(invocation);
                    }
                }
            }
            return responseMessage;
        } else {
            return getNext().invoke(request);
        }
    }
}