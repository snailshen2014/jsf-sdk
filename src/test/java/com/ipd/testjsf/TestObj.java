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
package com.ipd.testjsf;

import java.io.Serializable;
import java.util.Set;

/**
 * Title: <br>
 * Description: <br>
 */
public class TestObj<T extends Serializable> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -200698852458623917L;
	private String name;
	private String desc;
    private int age;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	private T t;
	/**
	 * @return the t
	 */
	public T getT() {
		return t;
	}

	/**
	 * @param t the t to set
	 */
	public void setT(T t) {
		this.t = t;
	}
	
	private Set s;
	/**
	 * @return the s
	 */
	public Set getS() {
		return s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(Set s) {
		this.s = s;
	}

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}