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
package com.ipd.testjsf.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ipd.testjsf.HelloService;


/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ExampleStrClientMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExampleStrClientMain.class);


    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/jsf-consumer-example.xml");
        HelloService service = (HelloService) appContext.getBean("helloService");
        LOGGER.info("service:" + service);

        while (true) {
            try {
                //RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_PINPOINT, "jsf://127.0.0.1:11091/");
                boolean result = service.put(1, "zhanggeng test");
                LOGGER.info("result is :{}", result);
            } catch (Exception e) {
                LOGGER.error("echoObject出现异常", e);
            }
            try {
                Thread.sleep(2000);
            } catch(Exception e) {
            }
        }

        // JSFContext.destroy();
    }
}