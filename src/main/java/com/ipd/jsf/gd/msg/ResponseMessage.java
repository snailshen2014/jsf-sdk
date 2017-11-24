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

public class ResponseMessage extends BaseMessage {

	private Object response;
    private Throwable exception; //error when the error has been declare in the interface

    public ResponseMessage(boolean initHeader) {
        super(initHeader);
    }

    public ResponseMessage() {
        super(true);
    }

    public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

	/**
	 * @return the error
	 */
	public boolean isError() {
		return exception != null;
	}

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "header="+ getMsgHeader() +
                "response=" + response +
                ", exception=" + exception +
                '}';
    }
}