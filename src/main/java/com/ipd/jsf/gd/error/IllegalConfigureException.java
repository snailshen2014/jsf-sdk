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
package com.ipd.jsf.gd.error;

/**
 * Title: 非法配置异常<br>
 * 
 * Description: 初始化时候就抛出<br>
 */
public class IllegalConfigureException extends InitErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253895783140745954L;

	/**
	 * 错误的配置项，例如refernce.loadblance
	 */
	private String configKey;

	/**
	 * 错误的配置值，例如ramdom（正确的是random）
	 */
	private String configValue;

    protected IllegalConfigureException() {
    }

	/**
	 * @param configKey
	 * @param configValue
	 */
	@Deprecated
	public IllegalConfigureException(String configKey, String configValue) {
		super("[JSF-21000]The value of config " + configKey + " [" + configValue + "] is illegal, please check it");
		this.configKey = configKey;
		this.configValue = configValue;
	}

	/**
	 *
	 * @param configKey
	 * @param configValue
	 * @param message
	 */
	@Deprecated
	public IllegalConfigureException(String configKey, String configValue, String message) {
		super("[JSF-21000]The value of config " + configKey + " [" + configValue + "] is illegal, " + message);
		this.configKey = configKey;
		this.configValue = configValue;
	}

	/**
	 * @param configKey
	 * @param configValue
	 */
	public IllegalConfigureException(int code, String configKey, String configValue) {
		super("[JSF-" + code + "]The value of config " + configKey + " [" + configValue + "] is illegal, please check it");
		this.configKey = configKey;
		this.configValue = configValue;
	}

    /**
     *
     * @param configKey
     * @param configValue
     * @param message
     */
    public IllegalConfigureException(int code, String configKey, String configValue, String message) {
        super("[JSF-" + code + "]The value of config " + configKey + " [" + configValue + "] is illegal, " + message);
        this.configKey = configKey;
        this.configValue = configValue;
    }

	/**
	 * @return the configKey
	 */
	public String getConfigKey() {
		return configKey;
	}

	/**
	 * @return the configValue
	 */
	public String getConfigValue() {
		return configValue;
	}

}