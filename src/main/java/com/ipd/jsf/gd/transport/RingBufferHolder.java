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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.msg.BaseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.gd.util.ringbuf.RingBufferService;
import io.netty.channel.Channel;

/**
 * Title: RingBufferHolder<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class RingBufferHolder {

    private String name;

    private int batchSize = 0;

    private int model;

    private final RingBufferService ringBufferService;

    private static final Logger logger = LoggerFactory.getLogger(RingBufferHolder.class);

    private static Map<String,RingBufferHolder> holderMap = new ConcurrentHashMap<String, RingBufferHolder>(4);

    private RingBufferHolder(){
        ringBufferService = null;
    }

    private RingBufferHolder(String name, int model, int batchSize){
        this.name = name;
        this.batchSize = batchSize;
        this.model = model;
        this.ringBufferService = new RingBufferService(1024, this.name);
        initRingBuffer();
    }

    protected static RingBufferHolder getServerRingBufferHolder(String key){
        return holderMap.get(key);
    }

    protected static RingBufferHolder getServerRingBufferHolder(String key,int model,int batchSize){
        RingBufferHolder holder = holderMap.get(key);
        if(holder == null){
            holder = initHolder(key,model,batchSize);
        }
        return holder;
    }

    private static synchronized RingBufferHolder initHolder(String key,int model,int batchSize){
        RingBufferHolder holder = holderMap.get(key);
        if(holder == null){
            holder = new RingBufferHolder(key,model,batchSize);
            holderMap.put(key,holder);
        }
        return holder;
    }

    public void submit(BaseMessage msg){

        ringBufferService.add(msg);
    }

    private  void initRingBuffer() {
        if(!ringBufferService.isStarted()){
            ringBufferService.setHandler(new RingBufferEventHanler());
            try {
                ringBufferService.start();
            } catch (Exception e) {
                InitErrorException errorException = new InitErrorException("Init ringBuffer error!",e);
                throw errorException;
            }

        }

    }

    public void stop(){
        ringBufferService.stop();
    }

    private class RingBufferEventHanler implements RingBufferService.EventHandler {

        @Override
        public void onEvent(Object[] elements) throws Exception {
            BaseMessage msg;
            Map<Channel,List<BaseMessage>> msgMap = new ConcurrentHashMap<Channel, List<BaseMessage>>();
            for(Object obj:elements){
                msg = (BaseMessage) obj;
                List<BaseMessage> msgList = msgMap.get(msg.getChannel());
                if(msgList == null){
                    msgList = new LinkedList<BaseMessage>();
                    msgMap.put(msg.getChannel(),msgList);
                }
                msgList.add(msg);

            }

            // Traverse the map
            for (Channel keyChannel : msgMap.keySet()) {
                if (keyChannel == null) {
                    logger.error("Channel {} have been destoryed/removed for case of connection been close!", keyChannel);
                    return;
                }
                List<BaseMessage> msgList1 = msgMap.get(keyChannel);
                if (logger.isTraceEnabled()) {
                    logger.trace("get channel here::{}", keyChannel);
                }
                for (BaseMessage msgIns : msgList1) {
                    keyChannel.write(msgIns, keyChannel.voidPromise());
                }
                keyChannel.flush();
            }

        }

        @Override
        public void onException(Throwable e) {

            logger.error("error when handle message:" + e.getMessage(), e);

        }

        @Override
        public int getBatchSize() {
            if(batchSize <= 0) return 60;
            return batchSize;
        }
    }

    public static final int CLIENTSIDE_MODEL = 1;

    public static final int SERVERSIDE_MODEL = 2;

    public static final String CLIENT_SIDE_RINGBUFFER = "CLIENT_SIDE_RINGBUFFER";

    public static final String SERVER_SIDE_RINGBUFFER = "SERVER_SIDE_RINGBUFFER";

    /**
     * 接口对应的服务端ringbuffer，按接口名字缓存
     */
    private static ConcurrentHashMap<String,RingBufferHolder> serverRingbufferMap = new ConcurrentHashMap<String, RingBufferHolder>();

    /**
     * 接口对应的客户端ringbuffer，按接口名字缓存
     */
    private static ConcurrentHashMap<String,RingBufferHolder> clientRingbufferMap = new ConcurrentHashMap<String, RingBufferHolder>();

    /**
     * 代表空的Ringbuffer
     */
    private static RingBufferHolder nullHodler = new RingBufferHolder();

    /**
     * 接口对应的服务端Ringbuffer
     *
     * @param interfaceId
     *         接口名称
     * @return Ringbuffer
     */
    public static RingBufferHolder getServerRingbuffer(String interfaceId) {
        if (StringUtils.isNotEmpty(interfaceId)) {
            RingBufferHolder holder = serverRingbufferMap.get(interfaceId);
            if (holder == null) {
                // 服务端批量发送默认关闭
                int size = CommonUtils.parseInt(
                        JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_TRANSPORT_PROVIDER_BATCH, null), -1);
                if (size > 0) { // 主动开启
                    holder = RingBufferHolder.getServerRingBufferHolder(
                            RingBufferHolder.SERVER_SIDE_RINGBUFFER,
                            RingBufferHolder.SERVERSIDE_MODEL, size);
                }
                serverRingbufferMap.putIfAbsent(interfaceId, holder == null ? nullHodler : holder);
                return holder;
            } else {
                return holder == nullHodler ? null : holder;
            }
        }
        return null;
    }

    /**
     * 接口对应的客户端Ringbuffer
     *
     * @param interfaceId
     *         接口名称
     * @return Ringbuffer
     */
    public static RingBufferHolder getClientRingbuffer(String interfaceId) {

        if (StringUtils.isNotEmpty(interfaceId)) {
            RingBufferHolder holder = serverRingbufferMap.get(interfaceId);
            if (holder == null) {
                int size = CommonUtils.parseInt(
                        JSFContext.getInterfaceVal(interfaceId, Constants.SETTING_TRANSPORT_CONSUMER_BATCH, null), 60);
                if (size > 0) {
                    holder = RingBufferHolder.getServerRingBufferHolder(
                            RingBufferHolder.CLIENT_SIDE_RINGBUFFER,
                            RingBufferHolder.CLIENTSIDE_MODEL, size);
                    serverRingbufferMap.putIfAbsent(interfaceId, holder);
                } else {
                    serverRingbufferMap.putIfAbsent(interfaceId, nullHodler);
                }
                holder = serverRingbufferMap.get(interfaceId);
            }
            return holder == nullHodler ? null : holder;
        }
        return null;
    }

    public static void invalidateCache(String interfaceId) {
        if (interfaceId == null || Constants.GLOBAL_SETTING.equals(interfaceId)) {
            serverRingbufferMap.clear();
            clientRingbufferMap.clear();
        } else {
            serverRingbufferMap.remove(interfaceId);
            clientRingbufferMap.remove(interfaceId);
        }
    }

    /**
     * 销毁全部
     */
    public static void destroyAll() {
        serverRingbufferMap.clear();
        clientRingbufferMap.clear();
        RingBufferHolder holder = getServerRingBufferHolder(SERVER_SIDE_RINGBUFFER);
        if (holder != null) {
            holder.stop();
        }
        holder = getServerRingBufferHolder(CLIENT_SIDE_RINGBUFFER);
        if (holder != null) {
            holder.stop();
        }
    }
}