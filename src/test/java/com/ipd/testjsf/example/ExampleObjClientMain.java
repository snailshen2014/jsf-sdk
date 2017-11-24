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

import com.ipd.testjsf.ExampleObj;
import com.ipd.testjsf.TestObj;
import com.ipd.testjsf.TestSubObj;
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
public class ExampleObjClientMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExampleObjClientMain.class);


    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/jsf-consumer-example.xml");
        HelloService service = (HelloService) appContext.getBean("helloService");
        LOGGER.info("service:" + service);

        while (true) {
            try {
                ExampleObj obj = new ExampleObj();
                obj.setName("zhanggeng obj");
                ExampleObj result = service.echoObject(obj);
                System.out.println(result.getName());

                TestSubObj subObj = new TestSubObj();
                subObj.setName("zhanggeng");
                subObj.setSname("son name");
                TestObj result1 = service.echoObj(subObj);
                System.out.println(result1.getName());
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