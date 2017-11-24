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

import java.net.InetSocketAddress;
import java.util.Map;

import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 服务端端RpcContext处理过滤器<br>
 * <p/>
 * Description: 将调用里的一些特殊处理放入到隐式传参中<br>
 * <p/>
 */
public class ProviderContextFilter extends AbstractFilter {
    @Override
    public ResponseMessage invoke(RequestMessage request) {
        try {
            RpcContext context = RpcContext.getContext();
            context.setProviderSide(true);
            // 自定义参数从invocation里复制到RpcContext
            Map<String, Object> attachments = request.getInvocationBody().getAttachments();
            context.setAttachments(attachments);
            context.removeAttachment(Constants.CONFIG_KEY_GENERIC); // 旧版本发的generic特殊处理下
            context.setSession((Map<String, Object>) attachments.get(Constants.HIDDEN_KEY_SESSION));

            if (context.getRemoteAddress() == null) { // 未提前设置过
                // 设置远程客户端的服务地址
                InetSocketAddress address = (InetSocketAddress) attachments.get(Constants.INTERNAL_KEY_REMOTE);
                if (address != null) {
                    context.setRemoteAddress(address);
                }
            }
            if (context.getLocalAddress() == null) { // 未提前设置过
                // 设置本地服务端地址
                InetSocketAddress address = (InetSocketAddress) attachments.get(Constants.INTERNAL_KEY_LOCAL);
                if (address != null) {
                    context.setLocalAddress(address);
                }
            }
            // 是否强制关闭监控，默认开启
            String monitor = (String) getConfigContext().get(Constants.HIDDEN_KEY_MONITOR);
            if (CommonUtils.isFalse(monitor)) { // 主动关闭
                request.getInvocationBody().addAttachment(Constants.INTERNAL_KEY_MONITOR, monitor);
            }
            context.setAlias(request.getInvocationBody().getAlias());

            // 服务端获取应用信息
            String appId = (String) attachments.get(Constants.INTERNAL_KEY_APPID);
            if (appId != null) {
                context.setAttachment(Constants.HIDDEN_KEY_APPID, appId);
            }
            String appName = (String) attachments.get(Constants.INTERNAL_KEY_APPNAME);
            if (appName != null) {
                context.setAttachment(Constants.HIDDEN_KEY_APPNAME, appName);
            }
            String appInsId = (String) attachments.get(Constants.INTERNAL_KEY_APPINSID);
            if (appInsId != null) {
                context.setAttachment(Constants.HIDDEN_KEY_APPINSID, appInsId);
            }

            return getNext().invoke(request);
        } finally {
            RpcContext.removeContext();
        }
    }
}