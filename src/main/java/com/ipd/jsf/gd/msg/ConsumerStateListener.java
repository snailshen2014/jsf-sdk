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
package com.ipd.jsf.gd.msg;

/**
 * Title: 调用者客户端状态变化监听器<br>
 * <p/>
 * Description: 当consumer状态发送变化的时候使用，destroy的时候不会触发<br>
 * <p/>
 */
public interface ConsumerStateListener<T> {

    /**
     * 状态变成可用（有可用长连接）的时候
     *
     * @param proxy
     *         接口实现代理类
     */
    public void onAvailable(T proxy);

    /**
     * 状态变成不可用（无可用长连接）的时候
     *
     * @param proxy
     *         接口实现代理类
     */
    public void onUnavailable(T proxy);
}