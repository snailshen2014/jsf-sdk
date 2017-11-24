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
package com.ipd.jsf.gd.transport;

/**
 * Title: 回调抽象类<br>
 * <p/>
 * Description: 实现通知方法，指定传递对象<br>
 * <p/>
 *
 * @param <Q> 请求值类型
 * @param <S> 返回值类型
 */
public interface Callback<Q,S> {

    /**
     * 回调通知
     *
     * @param result
     *         通知对象
     * @return 返回值对象
     */
    S notify(Q result);
}