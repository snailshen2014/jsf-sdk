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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AsyncServerMain {


    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncServerMain.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("ServerMain start");

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/jsf-provider-example.xml");

        // 关闭服务
        // appContext.close();
        // JSFContext.destroy();

        // 启动本地服务，然后hold住本地服务
        synchronized (AsyncServerMain.class) {
            while (true) {
                try {
                    AsyncServerMain.class.wait();
                } catch (InterruptedException e) {

                }
            }
        }
    }
}