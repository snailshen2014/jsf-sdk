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
package com.ipd.jsf.gd.client;

import java.util.ArrayList;
import java.util.List;

import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 本机优先的随机算法 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class LocalPreferenceLoadbalance extends RandomLoadbalance {

    @Override
    public Provider doSelect(Invocation invocation, List<Provider> providers) {
        String localhost = JSFContext.getLocalHost();
        if (StringUtils.isEmpty(localhost)) {
            return super.doSelect(invocation, providers);
        }
        List<Provider> localProvider = new ArrayList<Provider>();
        for (Provider provider : providers) { // 解析IP，看是否和本地一致
            if (localhost.equals(provider.getIp())) {
                localProvider.add(provider);
            }
        }
        if (CommonUtils.isNotEmpty(localProvider)) { // 命中本机的服务端
            return super.doSelect(invocation, localProvider);
        } else { // 没有命中本机上的服务端
            return super.doSelect(invocation, providers);
        }
    }
}