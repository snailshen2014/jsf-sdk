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
package com.ipd.jsf.gd.codec.http;

import com.ipd.fastjson.JSON;
import com.ipd.fastjson.JSONReader;
import com.ipd.fastjson.parser.DefaultJSONParser;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.server.BaseServerHandler;
import com.ipd.jsf.gd.transport.ServerTransportConfig;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Title: HTTP json解析处理器<br>
 * <p/>
 * Description: 解析httppost过来的请求，从body中拿到请求参数进行调用，最后将结果封装为请求返回客户端<br>
 * <p/>
 */
public class HttpJsonHandler extends MessageToMessageDecoder<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpJsonHandler.class);

    private final BaseServerHandler baseServerHandler;

    /**
     * 服务端是否容许支持keep-alive
     */
    private final boolean serverKeepAlive;

    public HttpJsonHandler(BaseServerHandler baseServerHandler) {
        this.baseServerHandler = baseServerHandler;
        ServerTransportConfig serverTransportConfig = baseServerHandler.getServerTransportConfig();
        Map<String, String> sps = serverTransportConfig.getParameters();
        this.serverKeepAlive = sps != null && CommonUtils.isTrue(sps.get(Constants.SETTING_HTTP_KEEP_ALIVE));
    }

    /**
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        // 如果服务端开启容许keep-alive并且客户端是keep-alive方式调用
        boolean isKeepAlive = serverKeepAlive && HttpHeaders.isKeepAlive(msg);
        HttpMethod reqMethod = msg.getMethod();
        if (reqMethod != HttpMethod.POST
                && reqMethod != HttpMethod.PUT && reqMethod != HttpMethod.GET) {
            writeBack(ctx.channel(), false, "Only allow GET POST and PUT", isKeepAlive);
            return;
        }

        String uri = msg.getUri();
        if ("/favicon.ico".equals(uri)) {
            writeBack(ctx.channel(), true, "", isKeepAlive);
            return;
        }
        String jsonbody = null;
        Invocation invocation = null;
        try {
            // 解析uri
            String[] strArr = getInterfaceIdAndMethod(uri);
            String interfaceId = strArr[0];
            String alias = strArr[1];
            String methodName = strArr[2];

            // 构建请求
            RequestMessage requestMessage = new RequestMessage();
            requestMessage.getMsgHeader().setCodecType(Constants.CodecType.json.value());
            requestMessage.getMsgHeader().setMsgType(Constants.REQUEST_MSG);
            requestMessage.getMsgHeader().setProtocolType(Constants.ProtocolType.http.value());
            invocation = new Invocation();
            invocation.setClazzName(interfaceId);
            invocation.setMethodName(methodName);
            invocation.setAlias(alias);
            //是否keepalive
            invocation.addAttachment(Constants.INTERNAL_KEY_KEEPALIVE, isKeepAlive);
            requestMessage.setInvocationBody(invocation);

            // 解析一些自定义字段，丢到invocation中
            parseHeaders(ctx, msg, requestMessage);

            Class[] classArray = ReflectUtils.getMethodArgsType(interfaceId, methodName);
            if (classArray == null) {
                throw new JSFCodecException("params type list can NOT be NULL, please check the" +
                        " interfaceId/methodName. interfaceId:" + interfaceId + " method:" + methodName);
            }

            Object[] paramList = null;
            if(reqMethod != HttpMethod.GET) {
                // 解析请求body
                ByteBuf buf1 = msg.content();
                int size = buf1.readableBytes();
                byte[] s1 = new byte[size];
                buf1.readBytes(s1);
                //if(buf1.refCnt() >= 1) buf1.release();     NO NEED TO REALASE
                jsonbody = new String(s1, Constants.DEFAULT_CHARSET);
                //logger.info("charset:{} content json:{}", Constants.DEFAULT_CHARSET, json);
                paramList = streamParseJson(classArray, jsonbody);
            } else {
                String params = null;
                int length = classArray.length;
                paramList = new Object[length];
                if (uri.contains("?")) {
                    params = uri.substring(uri.indexOf("?") + 1);
                    paramList = this.parseParamArg(classArray, params);
                } else {
                    if (length != 0) {
                        throw new JSFCodecException("The number of parameter is wrong.");
                    }
                }
            }

            requestMessage.getInvocationBody().setArgsType(classArray);
            requestMessage.getInvocationBody().setArgs(paramList);

            baseServerHandler.handlerRequest(ctx.channel(), requestMessage);
        } catch (Throwable e) {
            logger.error("Failed to parse http request for uri " + uri +
                    (invocation != null ? " from " + NetUtils.toAddressString((InetSocketAddress)
                            invocation.getAttachment(Constants.INTERNAL_KEY_REMOTE)) : "")
                    + (jsonbody != null ? ", body is " + jsonbody : "")
                    + ".", e);
            //write error msg back
            writeExceptionBack(ctx.channel(), e, isKeepAlive);
        }
    }

    /*
     *
     *  another implement:
     *  Object[] paramsList = reader.readObject(Object[].class);
     *  Object[] realParamsList = PojoUtils.realize(paramsList,classArray);
     *
     */
    public static Object[] streamParseJson(Class[] typeClass, String json) {
        if(typeClass.length == 0) return new Object[0];
        Object[] result = new Object[typeClass.length];
        JSONReader reader = new JSONReader(new DefaultJSONParser(json));
        try {
            reader.startArray();
            int i = 0;
            while (reader.hasNext()) {
                result[i] = reader.readObject(typeClass[i]);
                i++;
            }
            reader.endArray();
        } finally {
            reader.close();
        }

        return result;
    }


    public static int writeExceptionBack(Channel channel, Throwable e, boolean isKeepAlive) {
        String message = e.getMessage();
        message = message == null? "UNKNOW": message.replace("\"", "'");//双引号转单引号
        String str = "{\"code\":500,\"error\":\"" + message + "\"}";
        return writeBack(channel, false, str,isKeepAlive);
    }


    public static int writeBack(Channel channel, boolean isSuccess, String resultStr, boolean isKeepAlive) {
        ByteBuf content = Unpooled.copiedBuffer(resultStr, Constants.DEFAULT_CHARSET);
        HttpResponseStatus status;
        if (isSuccess) {
            status = HttpResponseStatus.OK;
        } else {
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        //logger.info("result str:{}", resultStr);
        res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setContentLength(res, content.readableBytes());
        try {
            ChannelFuture f = channel.writeAndFlush(res);
            if (isKeepAlive) {
                HttpHeaders.setKeepAlive(res, true);
            } else {
                HttpHeaders.setKeepAlive(res, false);//set keepalive closed
                f.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e2) {
            logger.warn("Failed to send HTTP response to remote, cause by:", e2);
        }

        return content.readableBytes();
    }

    /*
     * parse example:   http://127.0.0.1:7070/interfaceId/alias/methodName  alias must exist!
     */
    protected static String[] getInterfaceIdAndMethod(String uri) {
        String[] result;
        if(uri.indexOf("?") != -1){
            uri = uri.substring(0, uri.indexOf("?"));
        }
        String[] end = uri.split("/");
        int resultLength = 3;
        if (end.length < 4) {
            throw new JSFCodecException("HTTP uri format : http://ip:port/interfaceId/alias/methodName");
        }
        result = new String[resultLength];
        //从第二个元素开始copy 第一个是空字符串
        System.arraycopy(end, 1, result, 0, resultLength);
        return result;
    }

    /**
     * 解析head里面的部分值，作为附加参数
     *
     * @param ctx
     *         ChannelHandlerContext
     * @param req
     *         FullHttpRequest
     * @param message
     *         RequestMessage
     */
    protected static void parseHeaders(ChannelHandlerContext ctx, FullHttpRequest req, RequestMessage message) {
        //for commit
        Invocation invocation = message.getInvocationBody();
        HttpHeaders httpHeaders = req.headers();
        // 解析远程地址
        String remoteIp = httpHeaders.get("X-Forwarded-For");
        if (remoteIp == null) {
            remoteIp = httpHeaders.get("X-Real-IP");
        }
        InetSocketAddress remoteAddress;
        if (remoteIp != null) { // 可能是vip nginx等转发后的ip
            remoteAddress = InetSocketAddress.createUnresolved(remoteIp, 0);
        } else {
            remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        }
        invocation.addAttachment(Constants.INTERNAL_KEY_REMOTE, remoteAddress);
        // 解析token
        parseHeader(httpHeaders, invocation, "token", Constants.HIDDEN_KEY_TOKEN);
        // 解析app相关
        parseHeader(httpHeaders, invocation, "appId", Constants.INTERNAL_KEY_APPID);
        parseHeader(httpHeaders, invocation, "appName", Constants.INTERNAL_KEY_APPNAME);
        parseHeader(httpHeaders, invocation, "appInsId", Constants.INTERNAL_KEY_APPINSID);
        // 解析长度
        String length = httpHeaders.get("Content-Length");
        if (StringUtils.isNotEmpty(length)) {
            message.getMsgHeader().setLength(CommonUtils.parseInt(length, 0));
        }
    }

    private static void parseHeader(HttpHeaders httpHeaders, Invocation invocation, String headerKey, String attachKey) {
        String value = httpHeaders.get(headerKey);
        if (StringUtils.isNotEmpty(value)) {
            invocation.addAttachment(attachKey, value);
        }
    }

    /**
     * TODO 暂时不用
     * @param params
     * @param classTypes
     * @return
     */
    private Object[] parseParameter(String params, Class classTypes[]){
        String paramList[] = params.split("&");
        if(classTypes.length != paramList.length){
            throw new JSFCodecException("The number of parameter is wrong.");
        }
        Object resultList[] = new Object[paramList.length];
        for(int i=0; i < classTypes.length; i++){
            Class type = classTypes[i];
            String value = paramList[i].substring(paramList[i].indexOf("=")+1).trim();
            try {
                resultList[i] = JSON.parseObject(value, type);
            } catch (Exception e){
                value = "\"" + value + "\"";
                resultList[i] = JSON.parseObject(value, type);
            }
        }

        return resultList;
    }

    public Object[] parseParamArg(Class classTypes[], String params) {
        String paramList[] = params.split("&");
        if(classTypes.length != paramList.length){
            throw new JSFCodecException("The number of parameter is wrong.");
        }
        Object resultList[] = new Object[paramList.length];
        for(int i=0; i<classTypes.length; i++){
            Class cl = classTypes[i];
            String value = paramList[i].substring(paramList[i].indexOf("=")+1).trim();
            if (String.class.equals(cl))
                try {
                    resultList[i] = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    resultList[i] = value;
                }
            else if (boolean.class.equals(cl) || Boolean.class.equals(cl))
                resultList[i] = Boolean.parseBoolean(value);
            else if (byte.class.equals(cl) || Byte.class.equals(cl))
                resultList[i] = Byte.decode(value);
            else if (short.class.equals(cl) || Short.class.equals(cl))
                resultList[i] = Short.decode(value);
            else if (char.class.equals(cl) || Character.class.equals(cl))
                resultList[i] = Character.valueOf(value.charAt(0));
            else if (int.class.equals(cl) || Integer.class.equals(cl))
                resultList[i] = Integer.decode(value);
            else if (long.class.equals(cl) || Long.class.equals(cl))
                resultList[i] = Long.decode(value);
            else if (float.class.equals(cl) || Float.class.equals(cl))
                resultList[i] = Float.valueOf(value);
            else if (double.class.equals(cl) || Double.class.equals(cl))
                resultList[i] = Double.valueOf(value);
            else
                throw new UnsupportedOperationException();
        }
        return resultList;
    }
}