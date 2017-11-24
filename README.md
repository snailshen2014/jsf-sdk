
JSF is a fully developed high-performance RPC framework.

JSF Feature:
1)  TCP and HTTP protocol
2)  REST protocol service
3)  Serialization formats such as MsgPack,JSON,Hessian
4)  Data Compression
5)  Multiple LB strategies and cluster strategies
6)  Synchronous and asynchronous invocation
7)  Extended filter framework
8)  Active connection health checking

Quick Start

1. Establish project and introduce JSF
2. Implement and publish provider service
3. Implement consumer

Simple Example

1. Spring+XML Mode
1.1)  Publish provider
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

1.2) Start consumer
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

2.API Mode
2.1) Publish provider
    public static void main(String[] args) throws UnsupportedEncodingException {
        HelloService helloService = new HelloServiceImpl();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setProtocol("jsf");
        logger.info("ServerConfig");
        ProviderConfig<HelloService> providerConfig = new ProviderConfig<HelloService>();
        providerConfig.setInterfaceId("com.ipd.testjsf.HelloService");
        providerConfig.setRef(helloService);
        providerConfig.setAlias("JSF:0.0.1");
        providerConfig.setServer(serverConfig); // ¶๶serverԃlist
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
2.2) Start consumer
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