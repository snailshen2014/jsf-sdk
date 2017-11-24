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
package com.ipd.jsf.gd.codec.java;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.Encoder;
import com.ipd.jsf.gd.codec.Decoder;

/**
 * java原生序列化和反序列化
 *
 */
public class JavaCodec implements Codec {
	
	private Encoder encoder;

    private Decoder decoder;
    
    public JavaCodec(){
    	encoder = new JavaEncoder();
    	decoder = new JavaDecoder();
    }

	/* (non-Javadoc)
	 * @see Codec#encode(java.lang.Object)
	 */
	@Override
	public byte[] encode(Object obj) {
		
		return this.encoder.encode(obj);
	}

	/* (non-Javadoc)
	 * @see Codec#decode(byte[], java.lang.Class)
	 */
	@Override
	public Object decode(byte[] buf, Class clazz) {
		return this.decoder.decode(buf, clazz);
	}

	/* (non-Javadoc)
	 * @see Codec#decode(byte[], java.lang.String)
	 */
	@Override
	public Object decode(byte[] buf, String classTypeName) {
		return this.decoder.decode(buf, classTypeName);
	}

	@Override
	public byte[] encode(Object obj, String classTypeName) {
		return this.encode(obj);
	}

}