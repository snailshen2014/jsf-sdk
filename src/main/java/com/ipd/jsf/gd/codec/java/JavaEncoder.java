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
import java.io.ObjectOutputStream;

import com.ipd.jsf.gd.codec.dubbo.DubboAdapter;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.codec.hessian.UnsafeByteArrayOutputStream;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.codec.Encoder;

/**
 * java原生序列化
 *
 */
public class JavaEncoder implements Encoder {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(JavaEncoder.class);

	/* (non-Javadoc)
	 * @see Encoder#encode(java.lang.Object)
	 */
	@Override
	public byte[] encode(Object obj) {
		UnsafeByteArrayOutputStream baos = new UnsafeByteArrayOutputStream(1024);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			if(obj instanceof RequestMessage){
				RequestMessage req = (RequestMessage)obj;
				encodeRequest(req, oos);
			} else if(obj instanceof ResponseMessage){
				ResponseMessage res = (ResponseMessage)obj;
				encodeResponse(res, oos);
			} else {
				writeObject(obj, oos);
			}
			oos.flush();
			byte data[] = baos.toByteArray();
			return data;
		} catch (IOException e) {
			LOGGER.error("Encoder error:", e);
		}  finally {
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					LOGGER.warn("", e);
				}
				oos = null;
			}
		}
		
		return new byte[0];
	}

	@Override
	public byte[] encode(Object obj, String classTypeName) {
		return this.encode(obj);
	}
	
	
	private void encodeRequest(RequestMessage req, ObjectOutputStream out) throws IOException{
		if(req.isHeartBeat()){
            out.writeObject(null);
        } else {
        	Invocation inv = req.getInvocationBody();
            MessageHeader msgHeader = req.getMsgHeader();
            String alias = req.getAlias();
            String[] groupversion = alias.split(":", -1);
            if (groupversion.length != 2) {
                throw new JSFCodecException("dubbo protocol need group and version, " +
                        "need config it at <jsf:consumer alials=\"group:version\" />");
            }
            String group = groupversion[0];
            String version = groupversion[1];
            
            writeUTF("2.4.10",out);
        	writeUTF(req.getClassName(), out);
        	writeUTF(version, out);
        	writeUTF(req.getMethodName(), out);
        	//参数类型
        	writeUTF(ReflectUtils.getDesc(inv.getArgClasses()), out);
        	//参数实例
        	Object args[] = inv.getArgs();
        	if(args != null && args.length != 0){
        		for (int i = 0; i < args.length; i++) {
                    writeObject(args[i], out);
                }
        	}
        	inv.addAttachment("group", group);
            inv.addAttachment("path", inv.getClazzName());
            inv.addAttachment("interface", inv.getClazzName());
            inv.addAttachment("version", version);
            inv.addAttachment("timeout", msgHeader.getAttrByKey(Constants.HeadKey.timeout));
            writeObject(inv.getAttachments(), out);
        }
	}
	
	private void encodeResponse(ResponseMessage res, ObjectOutputStream out) throws IOException{
		if(res.isHeartBeat()){
            out.writeObject(null);
        } else {
            // encode response data or error message.
            Throwable th = res.getException();
            if (th == null) {
                Object ret = res.getResponse();
                if (ret == null) {
                    out.writeByte(DubboAdapter.RESPONSE_NULL_VALUE);
                } else {
                    out.writeByte(DubboAdapter.RESPONSE_VALUE);
                    writeObject(ret, out);
                }
            } else {
                out.writeByte(DubboAdapter.RESPONSE_WITH_EXCEPTION);
                writeObject(th, out);
            }
        }
	}
	

	private void writeUTF(String v, ObjectOutputStream oos) throws IOException	{
		if( v == null ) {
			oos.writeInt(-1);
		} else {
			oos.writeInt(v.length());
			oos.writeUTF(v);
		}
	}
	
	
	private void writeObject(Object obj, ObjectOutputStream oos) throws IOException {
		if( obj == null ) {
			oos.writeByte(0);
		} else {
			oos.writeByte(1);
			oos.writeObject(obj);
		}
	}

}