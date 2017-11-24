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
package com.ipd.jsf.gd.codec.json;

import com.ipd.jsf.gd.codec.Codec;
import com.ipd.jsf.gd.codec.Encoder;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.codec.Decoder;

/**
 * Title: 采用fastjson 支持 json 序列化协议<br>
 * <p/>
 * Description: <br>
 * <p/>
 *
 * @since 2015/01/09 16:06
 */
public class JsonCodec implements Codec {

    static {
        JsonUtils.registerTemplate();
    }

    private Encoder encoder;

    private Decoder decoder;

    public JsonCodec() {
        encoder = new FastjsonEncoder();
        decoder = new FastjsonDecoder();
    }

    @Override
    public Object decode(byte[] datas, Class clazz) {
        return decoder.decode(datas,clazz);
    }

    @Override
    public Object decode(byte[] datas, String className) {
        return decoder.decode(datas,className);
    }

    @Override
    public byte[] encode(Object obj) {
        return encoder.encode(obj);
    }

    @Override
    public byte[] encode(Object obj, String classTypeName) {
        return encoder.encode(obj,classTypeName);
    }
}