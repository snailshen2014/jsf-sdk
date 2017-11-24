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

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.template.AbstractTemplate;
import com.ipd.org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * 字符数组序列化模板
 *
 */
public class CharacterArrayTemplate extends AbstractTemplate<Character[]> {
	
	private CharacterArrayTemplate(){}

	@Override
	public Character[] read(Unpacker u, Character[] to, boolean required)
			throws IOException {
		if (!required && u.trySkipNil()) {
            return null;
        }
		
//        int n = u.readArrayBegin();
//        if (to == null || to.length != n) {
//        	to = new Character[n];
//        }
//        for(int i=0; i < n; i++){
//        	to[i] = (char) u.readInt();
//        }
//        u.readArrayEnd();      `
		String content = u.readString();
		char cs[] = content.toCharArray();
		int csLen = cs.length;
		 if (to == null || to.length != csLen) {
	        	to = new Character[csLen];
	        }
		for(int i=0; i < csLen;i++){
			to[i] = cs[i];
		}
		return to;
	}

	@Override
	public void write(Packer pk, Character[] target, boolean required)
			throws IOException {
		if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
		char cs[] = new char[target.length];
		for(int i=0; i < target.length; i++){
            if(target[i] != null) {
                cs[i] = target[i];
            }
		}
		String content = new String(cs);
		pk.write(content);
//		pk.writeArrayBegin(target.length);
//		for(Character c : target){
//			pk.write(c.charValue());
//		}
//		pk.writeArrayEnd();
	}

	public static CharacterArrayTemplate getInstance(){
		return instance;
	}
	
	private static CharacterArrayTemplate instance = new CharacterArrayTemplate();

}