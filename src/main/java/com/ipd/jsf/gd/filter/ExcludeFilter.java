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

import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;

/**
 * Title: 用于排除其它过滤器的特殊过滤器<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ExcludeFilter extends AbstractFilter {

    /**
     * 要排除的过滤器 -*和 -default表示不加载默认过滤器
     */
    private final String excludeFilterName;

    public ExcludeFilter(String excludeFilterName) {
        this.excludeFilterName = excludeFilterName;
    }

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        throw new UnsupportedOperationException();
    }

    public String getExcludeFilterName() {
        return excludeFilterName;
    }
}