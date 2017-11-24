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
package com.ipd.testjsf;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.gd.util.RpcContext;

public class HelloServiceImpl implements HelloService {

    private static Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String echoStr(String str) {
//        String myname = (String) RpcContext.getContext().getAttachment("myname");
//        Integer myage = (Integer) RpcContext.getContext().getAttachment("myage");
//        logger.info("server get str : {} ", str);
//        logger.info("my name is : {} , age is {}", myname, myage);
//        RpcContext.getContext().setAttachment("yourage", 2);
        return str + " server";
    }

    @Override
    public TestObj<HashSet> echoObj(TestObj obj) {
        logger.info("server get obj : " + obj.getName());
        TestSubObj subObj = (TestSubObj) obj;
        logger.info("server get subobj : " + subObj.getName());
        obj.setName(obj.getName() + " server");
        HashSet hSet = new HashSet();
        hSet.add("111");
        hSet.add("222");
        obj.setT(hSet);
        obj.setS(hSet);

//        hSet = new HashSet(new ArrayList());
        return obj;
    }

    @Override
    public ExampleObj echoObject(ExampleObj obj) {
        //logger.info("server get obj : " + obj.getName());
        obj.setId(110);
        obj.setName(obj.getName() + " server");
        //obj.setList(Arrays.asList("111", "222"));

        return obj;
    }

    @Override
    public boolean put(int id, String str)  {
        InetSocketAddress address = RpcContext.getContext().getRemoteAddress();
        logger.info("server put id :{} ,str : {} from " + address, id, str);
        String myname = (String) RpcContext.getContext().getAttachment("myname");
        Integer myage = (Integer) RpcContext.getContext().getAttachment("myage");
        logger.info("server get str : {} ", str);
        logger.info("my name is : {} , age is {}", myname, myage);
//        try {
//            Thread.sleep(10000);
//        } catch (Exception e) {
//        }
//        throw new SafJosException("", "");
//        return "Helloservice.put " + str;
        return true;
    }

    @Override
    public String get(Integer id) throws MyException{
        logger.info("server get id : {}", id);
        try {
            Thread.sleep(new Random().nextInt(500));
        } catch (Exception e) {
        }
        throw new MyException("aaaa");
    }

    @Override
    public HashSet getHashSet(HashSet f) {
        f.add("xxxxxxxxxx");
        f.add("xxxxxxxxx1");
        f.add("xxxxxxxxx2");
        f.add("xxxxxxxxx3");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        return f;
    }

    @Override
    public void callBackString(String request, Callback resultListener) {

    }

    @Override
    public void callBackList(String request, Callback resultListener) {

    }

    @Override
    public void callBackObj(String request, Callback resultListener) {

    }


}