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
package com.ipd.jsf.gd.codec.msgpack;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.template.IntegerTemplate;
import com.ipd.org.msgpack.template.StringTemplate;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * 异常堆栈序列化模板类
 *
 */
public class JSFStackTraceElementTemplate extends AbstractTemplate<StackTraceElement> {

	private static final Logger logger = LoggerFactory.getLogger(JSFStackTraceElementTemplate.class);
	
	private StringTemplate stringTemp = StringTemplate.getInstance();
	
	private IntegerTemplate intTemp = IntegerTemplate.getInstance();
	
	/* (non-Javadoc)
	 * @see org.msgpack.template.Template#write(org.msgpack.packer.Packer, java.lang.Object, boolean)
	 */
	@Override
	public void write(Packer pk, StackTraceElement v, boolean required)
			throws IOException {
		if (v == null) {
			logger.error("Attempted to write null");
            throw new MessageTypeException("Attempted to write null");
        }
		pk.writeArrayBegin(4);
		
		stringTemp.write(pk, v.getClassName());
		stringTemp.write(pk, v.getFileName());
		stringTemp.write(pk, v.getMethodName());
		intTemp.write(pk, v.getLineNumber());
		
		pk.writeArrayEnd();
	}

	/* (non-Javadoc)
	 * @see org.msgpack.template.Template#read(org.msgpack.unpacker.Unpacker, java.lang.Object, boolean)
	 */
	@Override
	public StackTraceElement read(Unpacker u, StackTraceElement to,
			boolean required) throws IOException {
		if(u.trySkipNil()){
			return null;
		}
		u.readArrayBegin();
		
		String className = stringTemp.read(u, null);
		String fileName = stringTemp.read(u, null);
		String methodName = stringTemp.read(u, null);
		Integer lineNumber = intTemp.read(u, null);
		
		u.readArrayEnd();
		to = new StackTraceElement(className, methodName, fileName, lineNumber);
		return to;
	}

	private static JSFStackTraceElementTemplate instance = new JSFStackTraceElementTemplate();
	
	private JSFStackTraceElementTemplate(){}
	
	public static JSFStackTraceElementTemplate getInstance(){
		return instance;
	}
}