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
package com.ipd.jsf.gd.transport;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.reflect.JDKProxy;
import com.ipd.jsf.gd.server.Invoker;
import com.ipd.jsf.gd.util.CodecUtils;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.ThreadPoolUtils;
import io.netty.channel.Channel;

/**
 * Title: 回调工具类<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class CallbackUtil {

    private final static Logger logger = LoggerFactory.getLogger(CallbackUtil.class);

    private static ConcurrentHashMap<Class, AtomicInteger> callbackCountMap = new ConcurrentHashMap<Class, AtomicInteger>();

    /**
     * 接口+方法 ： 实际的Callback数据类型
     */
    private static ConcurrentHashMap<String, Class> callbackNames = new ConcurrentHashMap<String, Class>();

    private static ConcurrentHashMap<String, JSFClientTransport> clientTransportMap = new ConcurrentHashMap<String, JSFClientTransport>();

    private static ConcurrentHashMap<String,Callback> proxyMap = new ConcurrentHashMap<String, Callback>();//cache the callback stub proxy instance in serverside.

    private static ConcurrentHashMap<Callback,Integer> instancesNumMap = new ConcurrentHashMap<Callback, Integer>();//instance number

    /**
     * calback业务线程池（callback+async）
     */
    private static ThreadPoolExecutor callbackThreadPool;

    private static boolean hasCallbackParam(Method method) {
        Class[] clazzList = method.getParameterTypes();
        int cnt = 0;
        for (Class clazz : clazzList) {
            logger.trace("clazz - {}", clazz);
            if (isCallbackInterface(clazz)) {
                cnt++;
            }
        }
        if (cnt > 1) {
            throw new InitErrorException("Illegal callback parameter at method " + method.getName()
                    + ",just allow one callback parameter");
        }
        return cnt == 1;
    }

    public static Boolean isCallbackInterface(Class clazz) {
        Boolean flag = Boolean.FALSE;
        if (clazz.equals(Callback.class)) {
            flag = Boolean.TRUE;
        } else {
            Class[] types = clazz.getInterfaces();
            for (Class type : types) {
                if (type.equals(Callback.class)) {
                    flag = Boolean.TRUE;
                    break;
                }
            }

        }
        return flag;
    }

    /**
     * 服务端需要提前注册callback事件，Callback<T>里的T一定要指定类型
     *
     * @param clazz 接口类
     */
    public static void autoRegisterCallBack(Class clazz) {
        String interfaceId = clazz.getCanonicalName();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (hasCallbackParam(method)) {
                // 需要解析出Callback<T>里的T的实际类型
                Class reqRealClass = null;
                Class resRealClass = null;
                Type[] tps = method.getGenericParameterTypes();
                for (Type tp : tps) {
                    if (tp instanceof Class) {
                        Class cls = (Class) tp;
                        if (cls.equals(Callback.class)) {
                            throw new InitErrorException("[JSF-24300]Must set actual type of Callback");
                        } else {
                            continue;
                        }
                    }
                    if (tp instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) tp;
                        if (pt.getRawType().equals(Callback.class)) {
                            Type[] actualTypes = pt.getActualTypeArguments();
                            if (actualTypes.length == 2) {
                                reqRealClass = checkClass(actualTypes[0]);
                                resRealClass = checkClass(actualTypes[1]);
                                break;
                            }
                        }
                    }
                }
                if (reqRealClass == null) {
                    throw new InitErrorException("[JSF-24300]Must set actual type of Callback, Can not use <?>");
                }

                callbackRegister(interfaceId, method.getName(), reqRealClass);
                HashSet set = new HashSet<Class<?>>();
                CodecUtils.checkAndRegistryClass(reqRealClass, set);
                CodecUtils.checkAndRegistryClass(resRealClass, set);
            }
        }
    }

    private static Class checkClass(Type actualType) {
        Class realclass;
        try {
            if (actualType instanceof ParameterizedType) {
                // 例如 Callback<List<String>>
                realclass = (Class) ((ParameterizedType) actualType).getRawType();
            } else {
                // 普通的 Callback<String>
                realclass = (Class) actualType;
            }
            return realclass;
        } catch (ClassCastException e) {
            // 抛出转换异常 表示为"?"泛化类型， java.lang.ClassCastException:
            // sun.reflect.generics.reflectiveObjects.WildcardTypeImpl cannot be cast to java.lang.Class
            throw new InitErrorException("[JSF-24300]Must set actual type of Callback, Can not use <?>");
        }
    }

    /*
     *max default is 1000;
     *
     */
    public static String clientRegisterCallback(String interfaceId, String method, Object impl,int port) {

        if (impl == null) {
            throw new RuntimeException("Callback Ins cann't be null!");
        }
        String key = null;

        Integer insNumber = instancesNumMap.get(impl);
        if (insNumber == null) {
            insNumber = getInstanceNumber(impl, interfaceId, method, port);
            Integer num = instancesNumMap.putIfAbsent((Callback) impl, insNumber);
            if (num != null) {
                insNumber = num;
            }
        }

        Class clazz = impl.getClass();

        String ip = JSFContext.getLocalHost();
        String pid = JSFContext.PID;
        if (clazz.getCanonicalName() != null) {
            key = ip+"_"+port+"_"+pid+"_"+clazz.getCanonicalName() + "_" + insNumber;
        } else {
            key = ip+"_"+port+"_"+pid+"_"+clazz.getName()+"_"+ insNumber;
        }

        ClientCallbackHandler.registerCallback(key, (Callback) impl);
        return key;

    }

    private static Integer getInstanceNumber(Object impl,String interfaceId,String method,int port) {
        Class clazz = impl.getClass();
        AtomicInteger num = callbackCountMap.get(clazz);
        if (num == null) {
            num = initNum(clazz);
        }

        if (num.intValue() >= 2000) {
            throw new RuntimeException("[JSF-24301]Callback instance have exceeding 2000 for type:" + clazz+" interfaceId "+interfaceId+" method "+method+" port"+port);
        }

        return num.getAndIncrement();
    }


    private static AtomicInteger initNum(Class clazz) {
        AtomicInteger num = callbackCountMap.get(clazz);
        if (num == null) {
            num = new AtomicInteger(0);
            AtomicInteger nu = callbackCountMap.putIfAbsent(clazz,num);
            if(nu!=null){
                num = nu;
            }
        }
        return num;
    }

    /**
     * 服务端接收带有callback的请求
     *
     * @param msg
     *         请求
     * @param channel
     *         连接
     * @return
     */
    public static RequestMessage msgHandle(RequestMessage msg, Channel channel) {
        Invocation invocation = msg.getInvocationBody();
        Class[] types = invocation.getArgClasses();
        Callback callback = null;
        String callbackInsId = (String) msg.getMsgHeader().getAttrByKey(Constants.HeadKey.callbackInsId);
        if (callbackInsId == null) {
            throw new RuntimeException(" Server side handle RequestMessage callbackInsId can not be null! ");
        }
        int i = 0;
        for (Class type : types) {
            if (isCallbackInterface(type)) {
                Class actualType = callbackNames.get(getName(invocation.getClazzName(), invocation.getMethodName()));
                callback = buildCallbackProxy(channel, callbackInsId, actualType,msg.getMsgHeader().getCodecType());
                break;
            }
            i++;
        }
        Object[] objArr = invocation.getArgs();
        objArr[i] = callback;
        invocation.setArgs(objArr);
        msg.setInvocationBody(invocation);
        return msg;
    }

    /*
     *1.
     */
    public static Invoker serverCallbackInvoker(ClientTransport clientTransport, String callbackInsId, Class actualType,int codecType) {
        ChannelWapperedInvoker callbackInvoker = new ChannelWapperedInvoker(clientTransport, callbackInsId,codecType);
        // 设置实际的参数类型 ActualType
        callbackInvoker.setArgTypes(new String[]{ClassTypeUtils.getTypeStr(actualType)});
        return callbackInvoker;
    }


    private static JSFClientTransport initCallbackTransport(Channel channel) {
        String transportKey = NetUtils.getTransportKey(channel);
        JSFClientTransport temp = clientTransportMap.get(transportKey);
        if (temp == null) {
            temp = new JSFClientTransport(channel);
            JSFClientTransport trans = clientTransportMap.putIfAbsent(transportKey, temp);
            if(trans != null){
                temp = trans;
            }
        }
        return temp;
    }

    public static JSFClientTransport getClientTransport(Channel channel) {
        String transportKey = NetUtils.getTransportKey(channel);
        JSFClientTransport clientTransport = clientTransportMap.get(transportKey);
        return clientTransport;
    }

    /**
     * 服务端构建回调客户端的代理类
     * need another param for callback instanceId
     */
    public static Callback buildCallbackProxy(Channel channel, String callbackInsId, Class actualType,int codecType) {

        if(proxyMap.containsKey(callbackInsId)){
            //if channel failed remember to remove the proxy instance from the proxyMap
            return proxyMap.get(callbackInsId);
        }
        String transportKey = NetUtils.getTransportKey(channel);
        JSFClientTransport clientTransport = clientTransportMap.get(transportKey);
        if (clientTransport == null) {
            clientTransport = initCallbackTransport(channel);
        }

        Invoker invoker = serverCallbackInvoker(clientTransport, callbackInsId, actualType,codecType);
        Callback proxy = null;
        try {
            proxy = JDKProxy.getProxy(Callback.class, invoker);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RpcException("error when create the callbackProxy for callbackInsId.."+callbackInsId,e);
        }
        proxyMap.put(callbackInsId,proxy);
        return proxy;

    }

    public static void removeFromProxyMap(String callbackInsId){
        proxyMap.remove(callbackInsId);
    }

    public static JSFClientTransport getTransportByKey(String transportKey){
         return  clientTransportMap.get(transportKey);

    }



    private static String getName(String interfaceId, String methodName) {

        return interfaceId + "::" + methodName;

    }

    public static String callbackRegister(String interfaceId, String methodName, Class realClass) {
        String callbackKey = getName(interfaceId, methodName);
        logger.debug("register callback method key:{}", callbackKey);
        CallbackUtil.callbackNames.put(callbackKey, realClass);
        return callbackKey;
    }


    public static boolean isCallbackRegister(String interfaceId, String methodName) {
        boolean flag = Boolean.FALSE;
        if (callbackNames.containsKey(getName(interfaceId, methodName))) flag = Boolean.TRUE;
        return flag;
    }

    public static void checkTransportFutureMap() {

        for (Map.Entry<String, JSFClientTransport> entrySet : clientTransportMap.entrySet()) {
            try {
                JSFClientTransport clientTransport = entrySet.getValue();
                clientTransport.checkFutureMap();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void removeTransport(Channel channel) {
        String key = NetUtils.getTransportKey(channel);
        clientTransportMap.remove(key);
    }

    /**
     * 得到callback用的线程池 默认开始创建
     *
     * @return callback用的线程池
     */
    public synchronized static ThreadPoolExecutor getCallbackThreadPool() {
        return getCallbackThreadPool(true);
    }

    /**
     * 得到callback用的线程池
     *
     * @param build
     *         没有时是否构建
     * @return callback用的线程池
     */
    public synchronized static ThreadPoolExecutor getCallbackThreadPool(boolean build) {
        if (callbackThreadPool == null && build) {
            // 一些系统参数，可以从配置或者注册中心获取。
            int coresize = CommonUtils.parseInt(JSFContext.getGlobalVal(Constants.SETTING_CALLBACK_POOL_CORE_SIZE, null), Constants.DEFAULT_CLIENT_CALLBACK_CORE_THREADS);
            int maxsize = CommonUtils.parseInt(JSFContext.getGlobalVal(Constants.SETTING_CALLBACK_POOL_MAX_SIZE, null), Constants.DEFAULT_CLIENT_CALLBACK_MAX_THREADS);
            int queuesize = CommonUtils.parseInt(JSFContext.getGlobalVal(Constants.SETTING_CALLBACK_POOL_QUEUE, null), Constants.DEFAULT_CLIENT_CALLBACK_QUEUE);
            BlockingQueue<Runnable> queue = ThreadPoolUtils.buildQueue(queuesize);
            NamedThreadFactory threadFactory = new NamedThreadFactory("JSF-CLI-CB", true);

            RejectedExecutionHandler handler = new RejectedExecutionHandler() {
                private int i = 1;

                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    if (i++ % 7 == 0) {
                        i = 1;
                        logger.warn("Task:{} has been reject for ThreadPool exhausted!" +
                                        " pool:{}, active:{}, queue:{}, taskcnt: {}",
                                new Object[]{
                                        r,
                                        executor.getPoolSize(),
                                        executor.getActiveCount(),
                                        executor.getQueue().size(),
                                        executor.getTaskCount()
                                });
                    }
                    throw new RejectedExecutionException("Callback handler thread pool has bean exhausted");
                }
            };
            callbackThreadPool = ThreadPoolUtils.newCachedThreadPool(coresize, maxsize, queue, threadFactory, handler);
        }
        return callbackThreadPool;
    }
}