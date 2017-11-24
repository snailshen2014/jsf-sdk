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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.msg.ResponseListener;


/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class AsyncGenericClientMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncGenericClientMain.class);
    static int msgid = 0;
    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/jsf-consumer-async.xml");
        GenericService service = (GenericService) appContext.getBean("helloServiceG");
        LOGGER.info("service:" + service);


        while (true) {
            try {

                Map map = new HashMap();
                map.put("id",1);
                map.put("name","xxxName");
                map.put("class", "ExampleObj");
                Object objresult = service.$invoke("echoObject", new String[]{"ExampleObj"},
                        new Object[]{map});
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
                LOGGER.error("echoStr出现异常", e);
            }
            Thread.sleep(2000);
        }

        // JSFContext.destroy();
    }
}