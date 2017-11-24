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
 * Title: 其实就是RuntimeException，方便其他异常继承此异常扩展<br>
 * Description: <br>
 */
public class SafBaseException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7952765455937049309L;

	public SafBaseException() {
		super();
	}

	public SafBaseException(String message) {
		super(message);
	}

	public SafBaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafBaseException(Throwable cause) {
		super(cause);
	}
}