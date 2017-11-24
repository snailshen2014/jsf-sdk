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
package com.ipd.bk.saf;

/**
 * Title: SafJosException 增加了状态 中文错误描述 英文错误描述 <br>
 * Description: <br>
 */
public class SafJosException extends SafBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2991123028688172002L;
	/**
	 * 错误代码
	 */
	public String code;
	/**
	 * 中文错误描述
	 */
	public String zhMsg;
	/**
	 * 英文错误描述
	 */
	public String enMsg;

	/**
	 * 构造方法
	 * 
	 * @param code
	 *            错误代码
	 * @param zhMsg
	 *            中文错误描述
	 */
	public SafJosException(String code, String zhMsg) {
		super(zhMsg);
		this.code = code;
		this.zhMsg = zhMsg;
	}

	/**
	 * 构造方法
	 * 
	 * @param code
	 *            错误代码
	 * @param zhMsg
	 *            中文错误描述
	 * @param enMsg
	 *            英文错误描述
	 */
	public SafJosException(String code, String zhMsg, String enMsg) {
		super(zhMsg);
		this.code = code;
		this.zhMsg = zhMsg;
		this.enMsg = enMsg;
	}

	/**
	 * 构造方法
	 * 
	 * @param code
	 *            错误代码
	 * @param zhMsg
	 *            中文错误描述
	 * @param enMsg
	 *            英文错误描述
	 * @param cause
	 *            错误
	 */
	public SafJosException(String code, String zhMsg, String enMsg,
			Throwable cause) {
		super(zhMsg, cause);
		this.code = code;
		this.zhMsg = zhMsg;
		this.enMsg = enMsg;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the zhMsg
	 */
	public String getZhMsg() {
		return zhMsg;
	}

	/**
	 * @param zhMsg
	 *            the zhMsg to set
	 */
	public void setZhMsg(String zhMsg) {
		this.zhMsg = zhMsg;
	}

	/**
	 * @return the enMsg
	 */
	public String getEnMsg() {
		return enMsg;
	}

	/**
	 * @param enMsg
	 *            the enMsg to set
	 */
	public void setEnMsg(String enMsg) {
		this.enMsg = enMsg;
	}
}