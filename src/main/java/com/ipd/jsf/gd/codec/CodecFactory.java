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
package com.ipd.jsf.gd.codec;

import com.ipd.jsf.gd.codec.hessian.HessianCodec;
import com.ipd.jsf.gd.codec.java.JavaCodec;
import com.ipd.jsf.gd.codec.json.JsonCodec;
import com.ipd.jsf.gd.codec.msgpack.MsgpackCodec;
import com.ipd.jsf.gd.codec.protobuf.ProtobufCodec;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: Coderc接口<br>
 * <p/>
 * Description: 包含Encoder和Decoder<br>
 * <p/>
 */
public class CodecFactory {

    /**
     * 得到Codec实现
     *
     * @param codecType
     *         codec type
     * @return Codec实现
     */
    public static Codec getInstance(int codecType) {
        Constants.CodecType ct = Constants.CodecType.valueOf(codecType);
        return getInstance(ct);
    }

    /**
     * 得到Codec实现
     *
     * @param codecType
     *         CodecType枚举
     * @return Codec实现
     */
    public static Codec getInstance(Constants.CodecType codecType) {
        Codec ins;
        switch (codecType) {
            case msgpack:
                ins = new MsgpackCodec();
                break;
            case hessian:
                ins = new HessianCodec();
                break;
            case java:
            	ins = new JavaCodec();
            	break;
            case json:
                ins = new JsonCodec();
                break;
            case protobuf:
                ins = new ProtobufCodec();
                break;
            default:
                throw new InitErrorException("Unsupported codec type : " + codecType);
        }

        return ins;
    }
}