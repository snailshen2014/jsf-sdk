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

import java.util.List;
import java.util.Random;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.config.ConsumerGroupConfig;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Title: 默认的组路由<br>
 *
 * Description: <br>
 */
public class DefaultGroupRouter implements GroupRouter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = getLogger(DefaultGroupRouter.class);

    /**
     * 随机
     */
    private final Random random = new Random();

    @Override
    public String router(Invocation invocation, ConsumerGroupConfig config) {
        String resultAlias;
        List<String> aliases = config.currentAliases();
        if (CommonUtils.isEmpty(aliases)) { // 一个都没有
            throw new RpcException("[JSF-21314]Aliases of multi-client proxy invoker is empty");
        } else {
            // 先从上下文里面取 目标路由参数 dstParam
            String dstParam = findDstParam(invocation, config);
            if (dstParam == null) { // 没有目标参数配置，随机全部分组
                resultAlias = randomAlias(aliases);
            } else { // 有目标参数配置
                String aliasesByParam = GroupRouterFactory.getAliasesByParam(config.getInterfaceId(),
                        invocation.getMethodName(), dstParam);
                // 映射里找不到 认为传入值就当是参数值
                resultAlias = aliasesByParam != null ? aliasesByParam : dstParam;
            }
        }
        return resultAlias;
    }

    protected String findDstParam(Invocation invocation, ConsumerGroupConfig config) {
        // 先从上下文里面取 目标机房
        String dstParam = (String) RpcContext.getContext().getAttachment(Constants.HIDDEN_KEY_DST_PARAM);
        if (dstParam == null) { // 为空，再从session里取 目标机房
            dstParam = (String) RpcContext.getContext().getSessionAttribute(Constants.HIDDEN_KEY_DST_PARAM);
            if (dstParam == null) {// 为空，再从参数里面取 目标机房
                String methodName = invocation.getMethodName();
                Integer index = (Integer) config.getMethodConfigValue(methodName, "dstParam", config.getDstParam());
                if (index != null) { // 目标机房分组ip的参数索引
                    Object[] args = invocation.getArgs();
                    if (args.length > index) {
                        dstParam = StringUtils.toString(args[index]);
                    } else {
                        throw new RpcException("[JSF-21315]Length of args must greater than index of dstParam," +
                                " method :" + methodName);
                    }
                }
            }
        }
        return dstParam;
    }

    protected String randomAlias(List<String> aliases) {
        return aliases.size() == 1 ? aliases.get(0) : aliases.get(random.nextInt(aliases.size()));
    }
}