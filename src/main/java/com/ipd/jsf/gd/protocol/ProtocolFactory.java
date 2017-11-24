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
package com.ipd.jsf.gd.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.error.InitErrorException;

/**
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolFactory {

    /**
     * The constant protocolMap.
     */
    private static Map<Integer, Protocol> protocolMap = new ConcurrentHashMap<Integer, Protocol>();


    static {
        Protocol protocol1 = initProtocol(Constants.ProtocolType.jsf, Constants.CodecType.msgpack);
        Protocol protocol2 = initProtocol(Constants.ProtocolType.dubbo, Constants.CodecType.hessian);
        protocolMap.put(buildKey(Constants.ProtocolType.jsf.value(), Constants.CodecType.msgpack.value()), protocol1);
        protocolMap.put(buildKey(Constants.ProtocolType.dubbo.value(), Constants.CodecType.hessian.value()), protocol2);
    }

    /**
     * Gets protocol.
     *
     * @param protocolType
     *         the protocol type
     * @param codecType
     *         the codec type
     * @return the protocol
     */
    public static Protocol getProtocol(int protocolType, int codecType) {
        int key = buildKey(protocolType, codecType);
        Protocol ins;
        ins = protocolMap.get(key);
        if (ins == null) {
            ins = initProtocol(Constants.ProtocolType.valueOf(protocolType), Constants.CodecType.valueOf(codecType));
            protocolMap.put(key, ins);
        }
        return ins;
    }

    /**
     * Gets protocol.
     *
     * @param protocolType
     *         the protocol type
     * @param codecType
     *         the codec type
     * @return protocol
     * @see
     */
    public static Protocol getProtocol(Constants.ProtocolType protocolType, Constants.CodecType codecType) {
        int key = buildKey(protocolType.value(), codecType.value());
        Protocol ins;
        ins = protocolMap.get(key);
        if (ins == null) {
            ins = initProtocol(protocolType, codecType);
            protocolMap.put(key, ins);
        }
        return ins;
    }

    /**
     * Build key.
     *
     * @param protocolType
     *         the protocol type
     * @param codecType
     *         the codec type
     * @return the int
     */
    private static int buildKey(int protocolType, int codecType) {
        return protocolType << 8 + codecType;
    }

    /**
     * Init protocol.
     *
     * @param protocolType
     *         the protocol type
     * @param codecType
     *         the codec type
     * @return the protocol
     */
    private static Protocol initProtocol(Constants.ProtocolType protocolType, Constants.CodecType codecType) {
        Protocol ins = null;
        switch (protocolType) {
            case jsf:
                ins = new JSFProtocol(codecType);
                break;
            case dubbo:
                ins = new DubboProtocol(codecType);
                break;
            default:
                throw new InitErrorException("Init protocol error by protocolType:" + protocolType
                        + " and codecType:" + codecType);
        }
        return ins;
    }

    /**
     * 检查协议和序列化是否匹配
     *
     * @param protocolType
     *         协议
     * @param codecType
     *         序列化方式
     * @throws InitErrorException 不匹配
     */
    public static void check(Constants.ProtocolType protocolType, Constants.CodecType codecType) {
        switch (protocolType) {
            case jsf:
                if (codecType == Constants.CodecType.msgpack
                        || codecType == Constants.CodecType.hessian
                        || codecType == Constants.CodecType.java
                        || codecType == Constants.CodecType.json
                        || codecType == Constants.CodecType.protobuf) {
                    break;
                } else {
                    throw new InitErrorException("[JSF-21306]Serialization of protocol jsf only support" +
                            " \"msgpack\", \"hessian\", \"java\", \"json\" and \"protobuf\"!");
                }
            case dubbo:
                if (codecType == Constants.CodecType.hessian
                        || codecType == Constants.CodecType.java) {
                    break;
                } else {
                    throw new InitErrorException("[JSF-21306]Serialization of protocol dubbo only support" +
                            " \"hessian\" and \"java\"!");
                }
            case rest: // 不使用序列化
                break;
            case webservice: // 不使用序列化
                break;
            case jaxws: // 不使用序列化
                break;
            default:
                throw new InitErrorException("[JSF-21306]Unsupported protocol group by protocol type:" + protocolType
                        + " and codec type:" + codecType);
        }
    }
}