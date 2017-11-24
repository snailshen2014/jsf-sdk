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

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFLogicSwitch;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.RpcContext;

/**
 * Title: 客户端RpcContext处理过滤器<br>
 * <p/>
 * Description: 将用户代码里设置的隐式传参复制到调用过程中<br>
 * <p/>
 */
public class ConsumerContextFilter extends AbstractFilter {
    @Override
    public ResponseMessage invoke(RequestMessage request) {
        RpcContext context = RpcContext.getContext();
        try {
            // 先清除该线程上次调用的缓存，防止数据污染
            context.setLocalAddress(null).setRemoteAddress(null).setAlias(null).setFuture(null).setProviderSide(false);
            context.removeAttachment(Constants.CONFIG_KEY_GENERIC); // 旧版本发的generic特殊处理下
            // 将rpcContext的值复制到invocation
            Invocation invocation = request.getInvocationBody();
            invocation.addAttachments(context.getAttachments());

            // 判断全局开关，如果要求主动关闭发送app，则不发送
            if (JSFLogicSwitch.INVOKE_SEND_APP) {
                String appId = (String) JSFContext.get(JSFContext.KEY_APPID);
                if (appId != null) {
                    invocation.addAttachment(Constants.INTERNAL_KEY_APPID, appId);
                }
                String appName = (String) JSFContext.get(JSFContext.KEY_APPNAME);
                if (appName != null) {
                    invocation.addAttachment(Constants.INTERNAL_KEY_APPNAME, appName);
                }
                String appInsId = (String) JSFContext.get(JSFContext.KEY_APPINSID);
                if (appInsId != null) {
                    invocation.addAttachment(Constants.INTERNAL_KEY_APPINSID, appInsId);
                }
            }

            return getNext().invoke(request);
        } finally {
            //InetSocketAddress address = RpcContext.getContext().getRemoteAddress();
            // 是否返回调用的远程ip？？
            //RpcContext.getContext().setRemoteAddress(address);

            if (context.getFuture() != null) {
                // 异步调用 删除缓存内key-value数据
                context.setLocalAddress(null).setRemoteAddress(null).setAlias(null).clearAttachments();
            } else {
                // 其它调用 删除ThreadLocal对象
                context.setLocalAddress(null).setRemoteAddress(null).setAlias(null).setFuture(null).clearAttachments();
                // RpcContext.removeContext();
            }
        }
    }
}