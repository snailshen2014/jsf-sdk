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
package com.ipd.jsf.gd.server;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.gd.codec.http.HttpJsonHandler;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.monitor.Monitor;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFLogicSwitch;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Title: HTTP协议执行的任务<br>
 * <p/>
 * Description: 会丢到业务线程执行<br>
 * <p/>
 */
public class JSFHttpTask extends BaseTask {

    private static final Logger logger = LoggerFactory.getLogger(JSFHttpTask.class);

    private final BaseServerHandler serverHandler;

    private final RequestMessage request;

    private final Channel channel;

    public JSFHttpTask(BaseServerHandler serverHandler, RequestMessage request, Channel channel) {
        this.serverHandler = serverHandler;
        this.request = request;
        this.channel = channel;
    }

    @Override
    void doRun() {
        boolean isKeepAlive = false;
        try {
            String className = request.getClassName();
            String methodName = request.getMethodName();
            String aliasName = request.getAlias();
            Invocation invocation = request.getInvocationBody();
            //AUTH check for blacklist/whitelist
            final InetSocketAddress remoteAddress = (InetSocketAddress) invocation.getAttachment(Constants.INTERNAL_KEY_REMOTE);
            final InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
            isKeepAlive = CommonUtils.isTrue((Boolean) invocation.getAttachment(Constants.INTERNAL_KEY_KEEPALIVE));

            if (!ServerAuthHelper.isValid(className, aliasName, NetUtils.toIpString(remoteAddress))) {
                throw new RpcException(request.getMsgHeader(),
                        "[JSF-23007]Fail to pass the server auth check in server: " + localAddress
                                + ", May be your host in blacklist of server");
            }

            Invoker invoker = serverHandler.getOwnInvoker(className, aliasName);
            if (invoker == null) {
                //logger.warn("No such invoker! for interfaceId:{} alias:{}",className,aliasName);
                throw new RpcException(request.getMsgHeader(), "[JSF-23006]Cannot found invoker of "
                        + BaseServerHandler.genInstanceName(className, aliasName)
                        + " in channel:" + NetUtils.channelToString(remoteAddress, localAddress)
                        + ", current invokers is " + serverHandler.getAllOwnInvoker().keySet());
            }


            invocation.addAttachment(Constants.INTERNAL_KEY_LOCAL, localAddress);
            //logger.debug("Handler HandleTask:{}", request);
            ResponseMessage responseMessage = invoker.invoke(request); // 执行调用，包括过滤器链

            int responseLength;
            if (responseMessage.isError()) {
                Throwable throwable = responseMessage.getException();
                responseLength = HttpJsonHandler.writeExceptionBack(channel, throwable, isKeepAlive);
            } else {
                String resultStr = JSON.toJSONString(responseMessage.getResponse(),
                        JSFLogicSwitch.JSON_SERIALIZER_FEATURES);
                responseLength = HttpJsonHandler.writeBack(channel, true, resultStr, isKeepAlive);
            }

            // 判断是否启动监控，如果启动则运行
            if (!CommonUtils.isFalse((String) invocation.getAttachment(Constants.INTERNAL_KEY_MONITOR))
                    && MonitorFactory.isMonitorOpen(className, methodName)) {
                String ip = NetUtils.toIpString(localAddress);
                int port = localAddress.getPort();
                Monitor monitor = MonitorFactory.getMonitor(MonitorFactory.MONITOR_PROVIDER_METRIC,
                        className, methodName, ip, port);
                if (monitor != null) { // 需要记录日志
                    boolean isError = responseMessage.isError();
                    invocation.addAttachment(Constants.INTERNAL_KEY_INPUT, request.getMsgHeader().getLength());
                    // TODO 报文长度 httpbody长度+ head长度（未知？？)
                    invocation.addAttachment(Constants.INTERNAL_KEY_OUTPUT, responseLength);
                    invocation.addAttachment(Constants.INTERNAL_KEY_RESULT, !isError);
                    invocation.addAttachment(Constants.INTERNAL_KEY_PROTOCOL, Constants.ProtocolType.http.value() + "");
                    if (isError) { // 失败
                        monitor.recordException(invocation, responseMessage.getException());
                    } else { // 成功
                        monitor.recordInvoked(invocation);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            HttpJsonHandler.writeExceptionBack(channel, e, isKeepAlive);
        }
    }
}