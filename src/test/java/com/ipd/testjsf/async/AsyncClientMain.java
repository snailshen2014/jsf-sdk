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

import com.ipd.testjsf.ExampleObj;
import com.ipd.testjsf.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.SpringVersion;

import com.ipd.jsf.gd.msg.ResponseFuture;
import com.ipd.jsf.gd.util.RpcContext;

import java.util.HashSet;

/**
 * Title: <br>
 *
 * Description: <br>
 */
public class AsyncClientMain {


	private final static Logger logger = LoggerFactory.getLogger(AsyncClientMain.class);

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Refer begin: {}", SpringVersion.getVersion());
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/jsf-consumer-async.xml");
		final HelloService service = (HelloService) appContext.getBean("helloService");
        HelloService service2 = (HelloService) appContext.getBean("helloService2");

        String result = null;
        while (true){
            try {
                result = service.echoStr("aaaa");
                logger.info("interface async result is null : {}", result);
                ResponseFuture<String> future1 = RpcContext.getContext().getFuture();

                ExampleObj obj = new ExampleObj();
                obj.setName("xxxxName");
                obj.setId(123);
                ExampleObj objresult = service.echoObject(obj);
                logger.info("interface async objresult is null : {}", objresult);
                ResponseFuture<ExampleObj> objfuture2 = RpcContext.getContext().getFuture();

                boolean put = service2.put(1,"bbb");
                logger.info("interface sync result is : {}", put);

                result = service2.echoStr("ccc");
                logger.info("method async result is null : {}", result);
                ResponseFuture<String> future3 = RpcContext.getContext().getFuture();



                result = future1.get();
                logger.info("interface async result now is : {} ", result);
                logger.info("interface async result usetime is : {} ", future1.getUseTime());
                objresult = objfuture2.get();
                logger.info("interface async objresult now is : {} ", objresult);
                logger.info("interface async result objresult usetime is : {} ", objfuture2.getUseTime());
                result = future3.get();
                logger.info("method async result now is : {} ", result);
                logger.info("interface async result result usetime is : {} ", future3.getUseTime());

                /*String result1 = service.echoStr("aaaa"); // 发起一次调用 此时返回null
                ResponseFuture<String> future1 = RpcContext.getContext().getFuture(); // 得到第一次调用的Future
                String result2 = service.echoStr("bbbb"); // 发起第二次调用 此时返回null
                ResponseFuture<String> future2 = RpcContext.getContext().getFuture(); // 得到第二次调用的Future
                String result3 = service.echoStr("ccc"); // 发起第三次调用 此时返回null
                ResponseFuture<String> future3 = RpcContext.getContext().getFuture(); // 得到第三次调用的Future

                // 依次拿到结果
                result1 = future1.get();
                logger.info("result1 now is : {} ", result1);
                logger.info("result1 use time is : {} ", future1.getUseTime());
                result2 = future2.get();
                logger.info("result2 now is : {} ", result2);
                logger.info("result2 use time is : {} ", future2.getUseTime());
                result3 = future3.get();
                logger.info("result3 now is : {} ", result3);
                logger.info("result3 use time is : {} ", future3.getUseTime());
                */

                /*ResponseFuture future4 = ResponseFuture.build(new ResponseFuture.AsyncCall() {
                    @Override
                    public void invoke() throws Throwable {
                        service.get(1);
                    }
                });
                logger.info("interface async future4 now is : {}", future4.get());
                logger.info("interface async future4 useTime is : {}", future4.getUseTime());*/

                ResponseFuture future5 = ResponseFuture.build(new ResponseFuture.AsyncCall() {
                    @Override
                    public void invoke() throws Throwable {
                        service.getHashSet(new HashSet());
                    }
                });
                try{
                    //future5.cancel(true);
                    //logger.info("interface async future5 now is cancled or not: {}", future5.isCancelled());
                    //Thread.sleep(5500);
                    logger.info("interface async future5 now is : {}", future5.get());
                }catch (Exception e){
                    logger.error("catch " + e.getClass().getCanonicalName() + " " + e.getMessage(),e);
                }
                logger.info("interface async future5 useTime is : {}", future5.getUseTime());


            } catch (Throwable e) {
                logger.error("catch " + e.getClass().getCanonicalName() + " " + e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch(Exception e) {
            }
        }
	}
}