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

import com.ipd.jsf.gd.util.Constants;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class RequestMessage extends BaseMessage {

    private Invocation invocationBody;


    private long receiveTime;   //temp Property for Request receive time

    private String targetAddress; // Remote address




    public RequestMessage(boolean initHeader) {
        super(initHeader);
    }

    public RequestMessage() {
        super(true);
    }

    public Invocation getInvocationBody() {

        if(invocationBody == null) return null;

        if(invocationBody.getClazzName() == null){
            invocationBody.setClazzName(this.getClassName());
        }
        if(invocationBody.getMethodName() == null){
            invocationBody.setMethodName(this.getMethodName());
        }
        return invocationBody;
    }

    public Integer getClientTimeout(){
    	Integer timeout = (Integer)this.getMsgHeader().getAttrByKey(Constants.HeadKey.timeout);
    	if(timeout==null) return null;
        if(timeout<=0) timeout = 1000;
        return timeout;
    }

    public void setClientTimeout(long timeout){
        this.getMsgHeader().addHeadKey(Constants.HeadKey.timeout,timeout);
    }

    public void setInvocationBody(Invocation invocationBody) {
        this.invocationBody = invocationBody;
    }

    public String getClassName(){
        return invocationBody.getClazzName();
    }

    public String getMethodName(){
        return invocationBody.getMethodName();
    }

    public String getAlias(){
        return invocationBody.getAlias();
    }

    public void setClassName(String className){
        invocationBody.setClazzName(className);
    }

    public void setMethodName(String methodName){
        invocationBody.setMethodName(methodName);

    }

    public void setAlias(String alias){
        invocationBody.setAlias(alias);
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }
}