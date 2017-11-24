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
package com.ipd.testjsf.async;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.RegistryConfig;
import com.ipd.jsf.gd.msg.ResponseListener;


/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class AsyncGenericClientMainAPI {

    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncGenericClientMainAPI.class);
    static int msgid = 0;
    public static void main(String[] args) {
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId("com.ipd.testjsf.HelloService");// 这里写真实的类名
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("JSF_0.0.1");
        consumerConfig.setUrl("jsf://127.0.0.1:11090");// 打开表示直连
        consumerConfig.setGeneric(true); // 需要指定是Generic调用true
        LOGGER.info("实例ConsumerConfig");

        // 得到泛化调用实例，此操作很重，请缓存service 对象！！！！
        GenericService service = consumerConfig.refer();

        while (true) {
            try {
                // 传递对象的, 如果没有对象类，可以通过map来描述一个对象
                Map map = new HashMap();
                map.put("id",1);
                map.put("name","xxxxgenericobj");
                map.put("class", "ExampleObj"); // class属性就传真实类名

                // 异步回调
                service.$asyncInvoke("echoObject", new String[]{"ExampleObj"},
                        new Object[]{map}, new ResponseListener() {
                            @Override
                            public void handleResult(Object result) {
                                LOGGER.info("[{}] aysnc invoke result :{}", msgid++, result);
                            }

                            @Override
                            public void catchException(Throwable e) {
                                LOGGER.error("[{}] aysnc invoke error :{}", msgid++, e.getMessage());
                            }
                        });

                service.$asyncInvoke("get", new String[]{"java.lang.Integer"},
                        new Object[]{11}, new ResponseListener() {
                            @Override
                            public void handleResult(Object result) {
                                LOGGER.info("[{}] aysnc invoke result :{}", msgid++, result);
                            }

                            @Override
                            public void catchException(Throwable e) {
                                LOGGER.error("[{}] aysnc invoke error :{}", msgid++, e.getMessage());
                            }
                        });

            } catch (Exception e) {
                LOGGER.error("echoObject出现异常", e);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}