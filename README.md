JSF- An RPC library and framework
===================================

# What is JSF
  
JSF is an RPC framework developed by the **TIG**(Technical Infrastructure Group).
   
It is now supporting thouthands of applications, providing over 200 billions of RPC calls everyday. During the China's big shopping festivals like Double 11 and 618, the total number of JSF-based RPC calls can reach 400 billions.

## JSF Features
* TCP and HTTP protocols
* REST style service
* Multiple serialization data formats such as MsgPack,JSON,Hessian
* Data Compression
* Multiple load balancing strategies and cluster strategies
* Synchronous and asynchronous invocation
* Extendable filtering framework
* Active connection health checking

# Documentation

* [Documentation Home](https://github.com/tigcode/jsf-sdk/wiki)
* [Documentation JSF-CORE](https://github.com/tigcode/jsf-core)

# Quick Start

* Establish project and introduce JSF
* Implement and publish provider service
* Implement consumer

# Simple Example
There are two approaches shown as follows to define providers and consumers.
## Spring + XML
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jsf="http://jsf.ipd.com/schema/jsf"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://jsf.ipd.com/schema/jsf http://jsf.ipd.com/schema/jsf/jsf.xsd">
    
    <bean id="helloService" class="com.ipd.testjsf.HelloServiceImpl"/>
    <jsf:server id="jsf" protocol="jsf" port="9090"/>
    <jsf:provider id="helloServiceExport" interface="com.ipd.testjsf.HelloService"
                      ref="helloService" server="jsf" alias="JSF:0.0.1" register="false">
    </jsf:provider>
</beans>
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jsf="http://jsf.ipd.com/schema/jsf"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://jsf.ipd.com/schema/jsf  http://jsf.ipd.com/schema/jsf/jsf.xsd">

    <jsf:consumer id="helloService" interface="com.ipd.testjsf.HelloService" retries="0"
              protocol="jsf" alias="JSF:0.0.1" timeout="60000" url="jsf://127.0.0.1:9090">
    </jsf:consumer>
</beans>
```

## API
```
public static void main(String[] args) throws UnsupportedEncodingException {
    HelloService helloService = new HelloServiceImpl();
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setProtocol("jsf");
    logger.info("ServerConfig");
    ProviderConfig<HelloService> providerConfig = new ProviderConfig<HelloService>();
    providerConfig.setInterfaceId("com.ipd.testjsf.HelloService");
    providerConfig.setRef(helloService);
    providerConfig.setAlias("JSF:0.0.1");
    providerConfig.setServer(serverConfig);
    logger.info("ProviderConfig");
    providerConfig.export();
    logger.info("Publishing is finished");
    synchronized (ServerMainAPI.class) {
        while (true) {
            try {
                ServerMainAPI.class.wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
```

```
public static void main(String[] args){
    ConsumerConfig<HelloService> consumerConfig = new ConsumerConfig<HelloService>();
    consumerConfig.setInterfaceId("com.ipd.testjsf.HelloService");
    consumerConfig.setProtocol("jsf");
    consumerConfig.setAlias("JSF:0.0.1");
    consumerConfig.setUrl("jsf://127.0.0.1:22000;jsf://127.0.0.1:22001");
    logger.info("ConsumerConfig");
    HelloService service = consumerConfig.refer();
    while (true) {
        try {
            String result = service.echoStr("string put");
            logger.info("result msg:{}", result);
        } catch (Exception e) {
            logger.error("",e);
        }
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
    }
}
```
