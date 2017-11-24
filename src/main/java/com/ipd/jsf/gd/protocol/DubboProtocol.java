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
package com.ipd.jsf.gd.protocol;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.CodecFactory;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.Constants;
import io.netty.buffer.ByteBuf;

/**
 * Title: dubbo协议兼容<br>
 * <p/>
 * Description: 支持hessian序列化<br>
 * <p/>
 */
public class DubboProtocol implements Protocol {

    private final Codec codec;

    public DubboProtocol() {
        //默认hessian
        this.codec = CodecFactory.getInstance(Constants.CodecType.hessian);
    }

    public DubboProtocol(Constants.CodecType codecType){
        if (codecType != Constants.CodecType.java &&
                codecType != Constants.CodecType.hessian) {
            throw new InitErrorException("Serialization of protocol dubbo only support \"hessian\" and \"java\"!");
        }
        this.codec = CodecFactory.getInstance(codecType);
    }

    @Override
    public Object decode(ByteBuf datas, Class clazz) {
        byte[] databs = new byte[datas.readableBytes()];
        datas.readBytes(databs);
        return codec.decode(databs, clazz);
    }


    @Override
    public Object decode(ByteBuf datas, String classTypeName) {
        try {
            Class clazz = ClassLoaderUtils.forName(classTypeName);
            return decode(datas, clazz);
        } catch (Exception e) {
            throw new JSFCodecException("decode by dubbo protocol error!");
        }
    }

    @Override
    public ByteBuf encode(Object obj, ByteBuf buffer) {
        byte[] bs = codec.encode(obj);
        buffer.writeBytes(bs);
        return buffer;
    }

}