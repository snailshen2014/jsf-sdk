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
package com.ipd.jsf.gd.config;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ipd.jsf.gd.server.Server;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.msg.ConnectListener;
import com.ipd.jsf.gd.protocol.ProtocolFactory;
import com.ipd.jsf.gd.server.ServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.FileUtils;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 服务发布时服务容器配置<br>
 * <p/>
 * Description: 对应标签:jsf:server，监听一个端口，为provider提供容器<br>
 * <p/>
 */
public class ServerConfig extends AbstractIdConfig implements Serializable {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);


    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 4729708849788157345L;

    /*------------- 参数配置项开始-----------------*/
    /**
     * 配置名称
     */
    protected String protocol = Constants.DEFAULT_PROTOCOL;

    /**
     * 实际监听IP，与网卡对应
     */
    protected String host;

    /**
     * 监听端口
     */
    protected int port = Constants.DEFAULT_SERVER_PORT;

    /**
     * 基本路径 默认"/"
     */
    protected String contextpath = Constants.DEFAULT_SERVER_CONTEXT_PATH;

    /**
     * 业务线程池大小
     */
    protected int threads = Constants.DEFAULT_SERVER_BIZ_THREADS;

    /**
     * io线程池大小
     */
    protected int iothreads;

    /**
     * 线程池类型
     */
    protected String threadpool = Constants.THREADPOOL_TYPE_CACHED;

    /**
     * 是否允许telnet，针对自定义协议
     */
    protected boolean telnet = true;

    /**
     * 业务线程池队列大小
     */
    protected int queues = Constants.DEFAULT_SERVER_QUEUE;

    /**
     * 服务端允许客户端建立的连接数
     */
    protected int accepts = Integer.MAX_VALUE;

    /**
     * 最大数据包大小
     */
    protected int payload = Constants.DEFAULT_PAYLOAD;

    /**
     * IO的buffer大小
     */
    protected int buffer = Constants.DEFAULT_BUFFER_SIZE;

    /**
     * 序列化方式
     */
    protected String serialization = Constants.DEFAULT_CODEC;

    /**
     * 事件分发规则。
     */
    protected String dispatcher = Constants.DISPATCHER_MESSAGE;

    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String> parameters;

    /**
     * 镜像ip，例如监听地址是1.2.3.4，告诉注册中心的确是3.4.5.6
     */
    protected String virtualhost;

    /**
     * 镜像ip文件，例如保存到"echo 127.0.0.1 > /etc/vitualhost"，启动时会自动读取
     */
    protected String virtualhostfile;

    /**
     * 连接事件监听器实例，连接或者断开时触发
     */
    protected transient List<ConnectListener> onconnect;

    /**
     * 是否打印消息信息
     */
    protected boolean debug = false;

    /**
     * 是否启动epoll，
     */
    protected boolean epoll = false;

    /**
     * 线程池类型，默认普通线程池
     */
    protected String queuetype = Constants.QUEUE_TYPE_NORMAL;

    /**
     * 是否hold住端口，true的话随主线程退出而退出，false的话则要主动退出
     */
    protected boolean daemon = true;

    /*------------- 参数配置项结束-----------------*/

    /**
     * 是否随机端口
     */
    private transient boolean randomPort;

    /**
     * 服务端对象
     */
    private transient volatile Server server;

    /**
     * 绑定的地址。是某个网卡，还是全部地址
     */
    private transient String boundHost;

    /**
     * 启动服务
     */
    public synchronized void start() {
        if (server != null) {
            return;
        }

        String host = this.getHost();
        if (StringUtils.isBlank(host)) {
            host = JSFContext.getLocalHost();
            this.host = host;
            // windows绑定到0.0.0.0的某个端口以后，其它进程还能绑定到该端口
            this.boundHost = Constants.isWindows ? host : NetUtils.ANYHOST;
        } else {
            this.boundHost = host;
        }

        // 绑定到指定网卡 或全部网卡
        int port = NetUtils.getAvailablePort(this.boundHost, this.getPort());
        if (port != this.port) {
            LOGGER.info("[JSF-21400]Changed port from {} to {} because the config port is disabled", this.port, port);
            this.port = port;
        }

        // 提前检查协议+序列化方式
        ProtocolFactory.check(Constants.ProtocolType.valueOf(getProtocol()),
                Constants.CodecType.valueOf(getSerialization()));

        server = ServerFactory.getServer(this);
        server.start();

        // 解析虚拟ip文件
        if (virtualhost != null) {
            LOGGER.info("Virtual host is specified, host will be change from {} to {} when register",
                    this.host, this.virtualhost);
        } else {
            if (virtualhostfile != null) {
                try {
                    virtualhost = FileUtils.file2String(new File(virtualhostfile));
                } catch (Exception e) {
                    throw new IllegalConfigureException(21403, "server.virtualhostfile", virtualhostfile,
                            "file can not read cause by " + e.getMessage());
                }
                LOGGER.info("Virtual host file is specified, host will be change from {} to {} when register",
                        this.host, this.virtualhost);
            }
        }
    }

    /**
     * 关闭服务
     */
    public synchronized void stop() {
        //Server server = ServerFactory.getServer(this);
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Gets virtual host.
     *
     * @return the virtual host
     */
    public String getVirtualhost() {
        return virtualhost;
    }

    /**
     * Sets virtual host.h
     *
     * @param virtualHost          the virtual host
     */
    public void setVirtualhost(String virtualHost) {
        this.virtualhost = virtualHost;
    }

    /**
     * Gets virtualhostfile.
     *
     * @return the virtualhostfile
     */
    public String getVirtualhostfile() {
        return virtualhostfile;
    }

    /**
     * Sets virtualhostfile.
     *
     * @param virtualhostfile          the virtualhostfile
     */
    public void setVirtualhostfile(String virtualhostfile) {
        this.virtualhostfile = virtualhostfile;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol          the protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets host.
     *
     * @param host          the host
     */
    public void setHost(String host) {
//        if (!NetUtils.isValidHost(host)) {
//            throw new IllegalConfigureException("server.host", host);
//        }
        this.host = host;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets port.
     *
     * @param port          the port
     */
    public void setPort(int port) {
        if (NetUtils.isRandomPort(port)) {
            randomPort = true;
        } else if (NetUtils.isInvalidPort(port)) {
            throw new IllegalConfigureException(21402, "server.port", port + "",
                    "port must between -1 and 65535 (-1 means random port)");
        }
        this.port = port;
    }

    /**
     * Gets threads.
     *
     * @return the threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Sets threads.
     *
     * @param threads          the threads
     */
    public void setThreads(int threads) {
        this.threads = threads;
    }

    /**
     * Gets iothreads.
     *
     * @return the iothreads
     */
    public int getIothreads() {
        return iothreads;
    }

    /**
     * Sets iothreads.
     *
     * @param iothreads          the iothreads
     */
    public void setIothreads(int iothreads) {
        this.iothreads = iothreads;
    }

    /**
     * Gets threadpool.
     *
     * @return the threadpool
     */
    public String getThreadpool() {
        return threadpool;
    }

    /**
     * Sets threadpool.
     *
     * @param threadpool          the threadpool
     */
    public void setThreadpool(String threadpool) {
        this.threadpool = threadpool;
    }

    /**
     * Is telnet.
     *
     * @return the boolean
     */
    public boolean isTelnet() {
        return telnet;
    }

    /**
     * Sets telnet.
     *
     * @param telnet          the telnet
     */
    public void setTelnet(boolean telnet) {
        this.telnet = telnet;
    }

    /**
     * Is random port.
     *
     * @return the boolean
     */
    public boolean isRandomPort() {
        return randomPort;
    }

    /**
     * Gets contextpath.
     *
     * @return the contextpath
     */
    public String getContextpath() {
        return contextpath;
    }

    /**
     * Sets contextpath.
     *
     * @param contextpath          the contextpath
     */
    public void setContextpath(String contextpath) {
        this.contextpath = contextpath;
    }

    /**
     * Gets accepts.
     *
     * @return the accepts
     */
    public int getAccepts() {
        return accepts;
    }

    /**
     * Sets accepts.
     *
     * @param accepts          the accepts
     */
    public void setAccepts(int accepts) {
        checkPositiveInteger("server.accept", accepts);
        this.accepts = accepts;
    }

    /**
     * Gets serialization.
     *
     * @return the serialization
     */
    public String getSerialization() {
        return serialization;
    }

    /**
     * Gets serialization.
     *
     * @param serialization          the serialization
     */
    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    /**
     * Gets onconnect.
     *
     * @return the onconnect
     */
    public List<ConnectListener> getOnconnect() {
        return onconnect;
    }

    /**
     * Sets onconnect.
     *
     * @param onconnect          the onconnect
     */
    public void setOnconnect(List<ConnectListener> onconnect) {
        this.onconnect = onconnect;
    }

    /**
     * Is debug.
     *
     * @return the boolean
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets debug.
     *
     * @param debug          the debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Gets queues.
     *
     * @return the queues
     */
    public int getQueues() {
        return queues;
    }

    /**
     * Sets queues.
     *
     * @param queues          the queues
     */
    public void setQueues(int queues) {
        this.queues = queues;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public int getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload          the payload
     */
    public void setPayload(int payload) {
        this.payload = payload;
    }

    /**
     * Gets dispatcher.
     *
     * @return the dispatcher
     */
    public String getDispatcher() {
        return dispatcher;
    }

    /**
     * Sets dispatcher.
     *
     * @param dispatcher          the dispatcher
     */
    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Is epoll.
     *
     * @return the boolean
     */
    public boolean isEpoll() {
        return epoll;
    }

    /**
     * Sets epoll.
     *
     * @param epoll          the epoll
     */
    public void setEpoll(boolean epoll) {
        this.epoll = epoll;
    }

    /**
     * Gets queuetype.
     *
     * @return the queuetype
     */
    public String getQueuetype() {
        return queuetype;
    }

    /**
     * Sets queuetype.
     *
     * @param queuetype          the queuetype
     */
    public void setQueuetype(String queuetype) {
        this.queuetype = queuetype;
    }

    /**
     * Is daemon.
     *
     * @return the boolean
     */
    public boolean isDaemon() {
        return daemon;
    }

    /**
     * Sets daemon.
     *
     * @param daemon          the daemon
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters          the parameters
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets buffer.
     *
     * @return the buffer
     */
    public int getBuffer() {
        return buffer;
    }

    /**
     * Sets buffer.
     *
     * @param buffer          the buffer
     */
    public void setBuffer(int buffer) {
        if (buffer > Constants.MAX_BUFFER_SIZE) {
            this.buffer = Constants.MAX_BUFFER_SIZE;
        } else if (buffer < Constants.MIN_BUFFER_SIZE) {
            this.buffer = Constants.MIN_BUFFER_SIZE;
        } else {
            this.buffer = buffer;
        }
    }

    /**
     * Gets server.
     *
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Gets bound host
     *
     * @return bound host
     */
    public String getBoundHost() {
        return boundHost;
    }

    /**
     * Hash code.
     *
     * @return int int
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result
                + ((protocol == null) ? 0 : protocol.hashCode());
        return result;
    }

    /**
     * Equals boolean.
     *
     * @param obj          the obj
     * @return boolean boolean
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerConfig other = (ServerConfig) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        return true;
    }

    /**
     * To string.
     *
     * @return string string
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ServerConfig [protocol=" + protocol + ", port=" + port + ", host=" + host + "]";
    }

}