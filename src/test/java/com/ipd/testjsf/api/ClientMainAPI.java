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
package com.ipd.testjsf.api;

import java.util.HashSet;

import com.ipd.testjsf.TestObj;
import com.ipd.testjsf.TestSubObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.SpringVersion;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.testjsf.HelloService;

/**
 * Title: <br>
 *
 * Description: <br>
 */
public class ClientMainAPI {
    private final static Logger logger = LoggerFactory.getLogger(ClientMainAPI.class);

    /**
     *
     * @param args
     */
    public static void main(String[] args){
        logger.info("Refer begin: {}", SpringVersion.getVersion());

        // 服务调用者连接注册中心，设置属性
        ConsumerConfig<HelloService> consumerConfig = new ConsumerConfig<HelloService>();
        consumerConfig.setInterfaceId("com.ipd.testjsf.HelloService");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("JSF:0.0.1");
        consumerConfig.setUrl("jsf://127.0.0.1:22000;jsf://127.0.0.1:22001"); //直连
        logger.info("实例ConsumerConfig");

        HelloService service = consumerConfig.refer();

        while (true) {
            try {
                String result = service.echoStr("string put");
                logger.info("result msg:{}", result);

                HashSet set = new HashSet();
                set.add("xxx set");
                HashSet set2 = service.getHashSet(set);
                logger.info("result msg:{}", set2.size());

                TestSubObj obj = new TestSubObj();
                obj.setName("ppppname");
                obj.setSname("ssssname");
                obj.setT(set);
                TestObj obj2 = service.echoObj(obj);
                logger.info("result msg:{}", obj2.getT());
            } catch (Exception e) {
                logger.error("",e);
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }

//		JSFContext.destroy();

    }

}