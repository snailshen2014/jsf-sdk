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
package com.ipd.jsf.gd.msg;

import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: Message构造公共类<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class MessageBuilder {

    /**
     * Build request.
     *
     * @param clazz
     *         the class
     * @param methodName
     *         the method name
     * @param methodParamTypes
     *         the method param types
     * @param args
     *         the args
     * @return the request message
     */
    public static RequestMessage buildRequest(Class clazz, String methodName, Class[] methodParamTypes, Object[] args) {
        Invocation invocationBody = new Invocation();
        invocationBody.setArgs(args == null ? new Object[0] : args);
        invocationBody.setArgsType(methodParamTypes);
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setInvocationBody(invocationBody);
        requestMessage.setClassName(ClassTypeUtils.getTypeStr(clazz));
        requestMessage.setMethodName(methodName);

        requestMessage.getMsgHeader().setMsgType(Constants.REQUEST_MSG);
        return requestMessage;

    }

    /**
     * Build response.
     *
     * @param request
     *         the request
     * @return the response message
     */
    public static ResponseMessage buildResponse(RequestMessage request) {
        ResponseMessage responseMessage = new ResponseMessage(false);
        responseMessage.setMsgHeader(request.getMsgHeader().clone());
        // clone后不可以再修改header的map里的值，会影响到原来对象
        responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
        //responseMessage.getMsgHeader().setCodecType(request.getMsgHeader().getCodecType());
        //responseMessage.getMsgHeader().setProtocolType(request.getMsgHeader().getProtocolType());

        return responseMessage;
    }


    /**
     * Build response.
     *
     * @param header
     *         the MessageHeader
     * @return the response message
     */
    public static ResponseMessage buildResponse(MessageHeader header) {
        ResponseMessage responseMessage = new ResponseMessage(false);
        responseMessage.setMsgHeader(header.clone());
        // clone后不可以再修改header的map里的值，会影响到原来对象
        responseMessage.getMsgHeader().setMsgType(Constants.RESPONSE_MSG);
        return responseMessage;
    }

    /**
     * Build heartbeat request.
     *
     * @return request message
     */
    public static RequestMessage buildHeartbeatRequest() {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.getMsgHeader().setMsgType(Constants.HEARTBEAT_REQUEST_MSG);
        return requestMessage;
    }

    /**
     * Build heartbeat response.
     *
     * @param heartbeat
     *         the heartbeat
     * @return the response message
     */
    public static ResponseMessage buildHeartbeatResponse(RequestMessage heartbeat) {
        ResponseMessage responseMessage = new ResponseMessage();
        MessageHeader header = responseMessage.getMsgHeader();
        header.setMsgType(Constants.HEARTBEAT_RESPONSE_MSG);
        header.setMsgId(heartbeat.getRequestId());
        header.setProtocolType(heartbeat.getProtocolType());
        header.setCodecType(heartbeat.getMsgHeader().getCodecType());
        //ResponseMessage responseMessage = new ResponseMessage(false);
        //responseMessage.setMsgHeader(heartbeat.getMsgHeader().clone());
        //responseMessage.getMsgHeader().setMsgType(Constants.HEARTBEAT_RESPONSE_MSG);
        return responseMessage;
    }
}