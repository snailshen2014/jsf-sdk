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
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * JSF自定义Object序列化模板
 *
 */
public class JSFObjectTemplate extends JSFAbstractTemplate<Object> {
	
	private static final Logger logger = LoggerFactory.getLogger(JSFObjectTemplate.class);
	
	@Override
	public void write(Packer pk, Object value, boolean required) throws IOException {
		if (value == null) {
            if (required) {
            	logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		writeValue(pk, value);
	}

	@Override
	public Object read(Unpacker u, Object to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		to = readValue(u);
    	return to;
	}

	private static JSFObjectTemplate instance = new JSFObjectTemplate();
	
	private JSFObjectTemplate(){}
	
	public static JSFObjectTemplate getInstance(){
		return instance;
	}
}