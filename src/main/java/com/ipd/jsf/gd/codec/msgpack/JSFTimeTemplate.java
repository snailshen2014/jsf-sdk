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
import java.sql.Time;

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * Time序列化模板类
 *
 */
public class JSFTimeTemplate extends AbstractTemplate<Time> {

	@Override
	public void write(Packer pk, Time target, boolean required) throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write((long) target.getTime());
		
	}

	@Override
	public Time read(Unpacker u, Time to, boolean required) throws IOException {
		 if (!required && u.trySkipNil()) {
	            return null;
	        }
	        long temp = u.readLong();
	        return new Time(temp);
	}

	private JSFTimeTemplate(){}
	
	private static JSFTimeTemplate instance = new JSFTimeTemplate();
	
	public static JSFTimeTemplate getInstance(){
		return instance;
	}
}