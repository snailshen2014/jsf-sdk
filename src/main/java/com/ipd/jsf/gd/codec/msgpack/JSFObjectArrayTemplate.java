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

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * 字符数组序列化模板
 *
 */
public class JSFObjectArrayTemplate extends AbstractTemplate<Object[]> {

	private JSFObjectTemplate objectTemplate = JSFObjectTemplate.getInstance();
	
	private JSFObjectArrayTemplate(){}

	@Override
	public Object[] read(Unpacker u, Object[] to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
        int n = u.readArrayBegin();
        if (to == null || to.length != n) {
        	to = new Object[n];
        }
        for(int i=0; i < n; i++){
        	to[i] = objectTemplate.read(u, null);
        }
        u.readArrayEnd();
		return to;
	}

	@Override
	public void write(Packer pk, Object[] target, boolean required)
			throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		pk.writeArrayBegin(target.length);
		for(Object c : target){
			objectTemplate.write(pk, c);
		}
		pk.writeArrayEnd();
	}

	public static JSFObjectArrayTemplate getInstance(){
		return instance;
	}
	
	private static JSFObjectArrayTemplate instance = new JSFObjectArrayTemplate();
}