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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.dubbo.DubboAdapter;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.codec.Decoder;
import com.ipd.jsf.gd.codec.hessian.UnsafeByteArrayInputStream;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java原生反序列化
 *
 */
public class JavaDecoder implements Decoder {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(JavaDecoder.class);

	/* (non-Javadoc)
	 * @see Decoder#decode(byte[], java.lang.Class)
	 */
	@Override
	public Object decode(byte[] datas, Class clazz) {
		String className = null;
		if(clazz != null){
			className = clazz.getCanonicalName();
		}
		Object result = this.decode(datas, className);
		return result;
	}

	/* (non-Javadoc)
	 * @see Decoder#decode(byte[], java.lang.String)
	 */
	@Override
	public Object decode(byte[] datas, String clazzTypeName) {
		UnsafeByteArrayInputStream bais = new UnsafeByteArrayInputStream(datas);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			if(RequestMessage.class.getCanonicalName().equals(clazzTypeName)){
				return this.decodeRequest(ois);
			} else if(ResponseMessage.class.getCanonicalName().equals(clazzTypeName)) {
				return this.decodeResonse(ois);
			} else {
				return this.readObject(ois);
			}
		} catch (Exception e) {
			LOGGER.error("Decode error", e);
		} finally {
			if(ois != null){
				try {
					ois.close();
				} catch (IOException e) {
					LOGGER.warn("", e);
				}
				ois = null;
			}
		}
		return null;
	}
	
	private RequestMessage decodeRequest(ObjectInputStream input) throws IOException {
		RequestMessage req = new RequestMessage();
		Invocation inv = new Invocation();
		 MessageHeader msgHeader = new MessageHeader();
		
         req.setInvocationBody(inv);
         req.setMsgHeader(msgHeader);
		 
		String dubboVersion = readUTF(input);
        String path = readUTF(input);
        req.setClassName(path);
        String version = readUTF(input);
        String methodName = readUTF(input);
        req.setMethodName(methodName);
        try {
	        Object[] args;
	        Class<?>[] pts;
	        String desc = readUTF(input);;
	        if (desc.length() == 0) {
	            pts = Codec.EMPTY_CLASS_ARRAY;
	            args = Codec.EMPTY_OBJECT_ARRAY;
	        } else {
	            pts = ReflectUtils.desc2classArray(desc);
	            args = new Object[pts.length];
	            for(int i = 0; i < args.length; i++){
	            	args[i] = readObject(input);
	            }
	        }
	        
	        inv.setArgs(args);
	        inv.setArgsType(pts);

			Map<String, Object> attachments = (Map<String, Object>) readObject(input);
			if (CommonUtils.isNotEmpty(attachments)) {
				Object generic = attachments.get(Constants.CONFIG_KEY_GENERIC);
				if (generic != null && generic instanceof String) {
					attachments.put(Constants.CONFIG_KEY_GENERIC, Boolean.valueOf((String) generic));
				}
				String token = (String) attachments.get("jdtoken");
				if (StringUtils.isNotEmpty(token)) {
					attachments.put(".token", token);
					attachments.remove("jdtoken");
				}
				inv.addAttachments(attachments);
			}
			inv.addAttachment("dubboVersion", dubboVersion);

			String group = (String)attachments.get("group");
            req.setAlias(group == null ? version : group + ":" + version);
            
            inv.setClazzName((String)attachments.get("interface"));
            msgHeader.addHeadKey(Constants.HeadKey.timeout, attachments.get("timeout"));
            

        } catch (ClassNotFoundException e) {
            throw new IOException("Read invocation data failed.", e);
        }
		return req;
	}
	
	private ResponseMessage decodeResonse(ObjectInputStream input) throws IOException, ClassNotFoundException{
		ResponseMessage res = new ResponseMessage();
        byte staus = input.readByte();
        switch (staus) {
            case DubboAdapter.RESPONSE_NULL_VALUE:
                break;
            case DubboAdapter.RESPONSE_VALUE:
                res.setResponse(readObject(input));
                break;
            case DubboAdapter.RESPONSE_WITH_EXCEPTION:
                res.setException((Throwable)readObject(input));
                break;
            default:
                break;
        }
        return res;
	}
	
	private String readUTF(ObjectInputStream ois) throws IOException {
		int len = ois.readInt();
		if( len < 0 )
			return null;

		return ois.readUTF();
	}

	private Object readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException	{
		byte b = ois.readByte();
		if( b == 0 )
			return null;

		return ois.readObject();
	}

}