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
 * Title: 客户端拿到response后事件处理<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public interface ResponseListener {

    /**
     * 得到正常返回的结果
     *
     * @param result
     *         the result 正常返回结果
     */
    public void handleResult(Object result);

    /**
     * 捕获到异常后
     *
     * @param e
     *         the e 异常
     */
    public void catchException(Throwable e);
}