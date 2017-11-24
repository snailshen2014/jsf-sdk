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
package com.ipd.jsf.gd.codec.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.com.caucho.hessian.HessianObjectMapping;
import com.ipd.com.caucho.hessian.io.AbstractHessianInput;
import com.ipd.com.caucho.hessian.io.AbstractHessianOutput;
import com.ipd.com.caucho.hessian.io.Hessian2Input;
import com.ipd.com.caucho.hessian.io.Hessian2Output;
import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.dubbo.DubboAdapter;
import com.ipd.jsf.gd.msg.MessageHeader;
import com.ipd.jsf.gd.msg.RequestMessage;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.ReflectUtils;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class HessianCodec implements Codec {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HessianCodec.class);


    /**
     * Instantiates a new Hessian codec.
     */
    public HessianCodec() {
        HessianObjectMapping.registryMapping("com.alibaba.dubbo.rpc.service.GenericException"
                                            ,"GenericException");
        HessianObjectMapping.registryMapping("com.alibaba.dubbo.rpc.RpcException"
                                            ,"RpcException");
    }

    /**
     * Encode byte [ ].
     *
     * @param obj the obj
     * @return the byte [ ]
     */
    @Override
    public byte[] encode(Object obj) {
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
        Hessian2Output mH2o = new Hessian2Output(bos);
        mH2o.setSerializerFactory(HessianSerializerFactory.SERIALIZER_FACTORY);
        if (obj instanceof RequestMessage) {
            try {
                encodeRequest((RequestMessage) obj, mH2o);
                mH2o.flushBuffer();

                byte[] data = bos.toByteArray();
                return data;
            } catch (JSFCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new JSFCodecException("Encode request error", e);
            }
        } else if (obj instanceof ResponseMessage) {
            try {
                encodeResponse((ResponseMessage) obj, mH2o);
                mH2o.flushBuffer();

                byte[] data = bos.toByteArray();
                return data;
            } catch (JSFCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new JSFCodecException("Encode response error", e);
            }
        } else {
            try {
                mH2o.writeObject(obj);
                mH2o.flushBuffer();
            } catch (JSFCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new JSFCodecException("Encode object error", e);
            }
        }
        byte[] data = bos.toByteArray();
        return data;
    }

    @Override
    public byte[] encode(Object obj, String classTypeName) {
        return encode(obj);
    }

    /**
     * 编码发送给服务端的Request
     *
     * @param req the RequestMessage
     * @param out the Hessian2Output
     */
    private void encodeRequest(RequestMessage req, Hessian2Output out) throws IOException {
        if(req.isHeartBeat()){
            out.writeObject(null);
        } else {
            Invocation inv = req.getInvocationBody();
            MessageHeader msgHeader = req.getMsgHeader();
            String alias = req.getAlias();
            String[] groupversion = alias.split(":", -1);
            if (groupversion.length != 2) {
                throw new JSFCodecException("dubbo protocol need group and version, " +
                        "need config it at <jsf:consumer alias=\"group:version\" />");
            }
            String group = groupversion[0];
            String version = groupversion[1];

            out.writeString("2.4.10");
            out.writeString(req.getClassName());
            out.writeString(version);
            out.writeString(inv.getMethodName());

            Object[] args = inv.getArgs();
            Class<?>[] pts = inv.getArgClasses();
            out.writeString(ReflectUtils.getDesc(pts));
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    out.writeObject(args[i]);
                }
            }
            inv.addAttachment("group", group);
            inv.addAttachment("path", inv.getClazzName());
            inv.addAttachment("interface", inv.getClazzName());
            inv.addAttachment("version", version);
            inv.addAttachment("timeout", msgHeader.getAttrByKey(Constants.HeadKey.timeout));
            out.writeObject(inv.getAttachments()); // dubbo只支持 Map<String,String>
        }
    }

    /**
     * 编码返回给客户端的response
     *
     * @param res the res
     * @param out the out
     * @throws IOException the iO exception
     */
    private void encodeResponse(ResponseMessage res, AbstractHessianOutput out) throws IOException {
        if(res.isHeartBeat()){
            out.writeObject(null);
        } else {
            // encode response data or error message.
            Throwable th = res.getException();
            if (th == null) {
                Object ret = res.getResponse();
                if (ret == null) {
                    out.writeInt(DubboAdapter.RESPONSE_NULL_VALUE);
                } else {
                    out.writeInt(DubboAdapter.RESPONSE_VALUE);
                    out.writeObject(ret);
                }
            } else {
                out.writeInt(DubboAdapter.RESPONSE_WITH_EXCEPTION);
                out.writeObject(th);
            }
        }
    }

    /**
     * Decode object.
     *
     * @param buf the buf
     * @param clazz the clazz
     * @return the object
     */
    @Override
    public Object decode(byte[] buf, Class clazz) {
        InputStream is = new UnsafeByteArrayInputStream(buf);
        AbstractHessianInput mH2i = new Hessian2Input(is);
        mH2i.setSerializerFactory(HessianSerializerFactory.SERIALIZER_FACTORY);

        Object obj = null;
        if (clazz == null) {
            try {
                obj = mH2i.readObject(); // 无需依赖class
            } catch (Exception e) {
                throw new JSFCodecException("Decode request error", e);
            }
        } else if (clazz == RequestMessage.class) {
            try {
                RequestMessage req = decodeRequest(buf, mH2i);
                obj = req;
            } catch (Exception e) {
                throw new JSFCodecException("Decode request error", e);
            }
        } else if (clazz == ResponseMessage.class) {
            try {
                ResponseMessage res = decodeResponse(buf, mH2i);
                obj = res;
            } catch (Exception e) {
                throw new JSFCodecException("Decode response error", e);
            }
        } else {
            try {
                obj = mH2i.readObject(); // 无需依赖class
            } catch (Exception e) {
                throw new JSFCodecException("Decode object error", e);
            }
        }

        return obj;
    }

    /**
     * 解码客户端发来的RequestMessage.
     *
     * @param databs the databs
     * @param input the input
     * @return the request message
     * @throws IOException the iO exception
     */
    private RequestMessage decodeRequest(byte[] databs, AbstractHessianInput input) throws IOException {

        RequestMessage req = new RequestMessage();
        Invocation invocation = new Invocation();

        String dubboVersion = input.readString();
        String path = input.readString();
        String version = input.readString();
        String methodName = input.readString();
        try {
            Object[] args;
            Class<?>[] pts;
            String desc = input.readString();
            if (desc.length() == 0) {
                pts = Codec.EMPTY_CLASS_ARRAY;
                args = Codec.EMPTY_OBJECT_ARRAY;
            } else {
                pts = ReflectUtils.desc2classArray(desc);
                args = new Object[pts.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        args[i] = input.readObject(pts[i]);
                    } catch (Exception e) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Decode argument failed: " + e.getMessage(), e);
                        }
                    }
                }
            }
            // dubbo只支持 Map<String,String>
            Map<String, Object> map = (Map<String, Object>) input.readObject(Map.class);
            if (CommonUtils.isNotEmpty(map)) {
                Object generic = map.get(Constants.CONFIG_KEY_GENERIC);
                if (generic != null && generic instanceof String) {
                    map.put(Constants.CONFIG_KEY_GENERIC, Boolean.valueOf((String) generic));
                }
                String token = (String) map.get("jdtoken");
                if (StringUtils.isNotEmpty(token)) {
                    map.put(".token", token);
                    map.remove("jdtoken");
                }
                invocation.addAttachments(map);
            }
            invocation.addAttachment("dubboVersion", dubboVersion);
            //decode argument ,may be callback
//            for (int i = 0; i < args.length; i++) {
//                args[i] = decodeInvocationArgument(channel, this, pts, i, args[i]);
//            }

            invocation.setArgsType(pts);
            invocation.setMethodName(methodName);
            invocation.setArgs(args);

            req.setInvocationBody(invocation);

            String interfaceId = null;
            String group = null;
            if (map != null) {
                interfaceId = (String) map.get("interface");
                group = (String) map.get("group");
                // version = (String) map.get("version");
            }
            req.setClassName(interfaceId == null ? path : interfaceId);
            req.setAlias(group == null ? version : group + ":" + version);

        } catch (ClassNotFoundException e) {
            throw new IOException("Read invocation data failed.", e);
        }

        return req;
    }


    /**
     * 解码服务端返回的Response
     *
     * @param buf the buf
     * @param in the in
     * @return the response message
     * @throws IOException the iO exception
     */
    private ResponseMessage decodeResponse(byte[] buf, AbstractHessianInput in) throws IOException {
        ResponseMessage res = new ResponseMessage();
        byte staus = (byte) in.readInt();
        switch (staus) {
            case DubboAdapter.RESPONSE_NULL_VALUE:
                break;
            case DubboAdapter.RESPONSE_VALUE:
                res.setResponse(in.readObject());
                break;
            case DubboAdapter.RESPONSE_WITH_EXCEPTION:
                res.setException((Throwable) in.readObject());
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * Decode object.
     *
     * @param buf the buf
     * @param classTypeName the class type name
     * @return the object
     */
    @Override
    public Object decode(byte[] buf, String classTypeName) {
        InputStream is = new UnsafeByteArrayInputStream(buf);
        AbstractHessianInput mH2i = new Hessian2Input(is);
        mH2i.setSerializerFactory(HessianSerializerFactory.SERIALIZER_FACTORY);

        Object obj = null;
        if (classTypeName == null) {
            try {
                obj = mH2i.readObject(); // 无需依赖class
            } catch (Exception e) {
                throw new JSFCodecException("Decode object error", e);
            }
        } else if (RequestMessage.class.getCanonicalName().equals(classTypeName)) {
            try {
                RequestMessage req = decodeRequest(buf, mH2i);
                obj = req;
            } catch (Exception e) {
                throw new JSFCodecException("Decode request error", e);
            }
        } else if (ResponseMessage.class.getCanonicalName().equals(classTypeName)) {
            try {
                ResponseMessage res = decodeResponse(buf, mH2i);
                obj = res;
            } catch (Exception e) {
                throw new JSFCodecException("Decode response error", e);
            }
        } else {
            try {
                obj = mH2i.readObject(); // 无需依赖class
            } catch (Exception e) {
                throw new JSFCodecException("Decode object error", e);
            }
        }

        return obj;
    }
}