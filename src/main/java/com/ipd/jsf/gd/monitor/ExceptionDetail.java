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
package com.ipd.jsf.gd.monitor;

import java.io.Serializable;

/**
 * 错误明细
 */
public class ExceptionDetail implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -1692502618172613840L;

    /**
     * The Exception name.
     */
    private String exceptionName;

    /**
     * The Num.
     */
    private int num;

    /**
     * Instantiates a new Exception detail.
     */
    public ExceptionDetail() {

    }

    /**
     * Instantiates a new Exception detail.
     *
     * @param exceptionName
     *         the exception name
     * @param num
     *         the num
     */
    public ExceptionDetail(String exceptionName, int num) {
        this.exceptionName = exceptionName;
        this.num = num;

    }

    /**
     * Gets exception name.
     *
     * @return the exception name
     */
    public String getExceptionName() {
        return exceptionName;
    }

    /**
     * Sets exception name.
     *
     * @param exceptionName
     *         the exception name
     */
    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    /**
     * Gets num.
     *
     * @return the num
     */
    public int getNum() {
        return num;
    }

    /**
     * Sets num.
     *
     * @param num
     *         the num
     */
    public void setNum(int num) {
        this.num = num;
    }

}