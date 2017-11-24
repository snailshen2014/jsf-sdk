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

import com.ipd.jsf.gd.codec.Decoder;
import com.ipd.org.msgpack.template.Template;

/**
 *
 * To change this template use File | Settings | File Templates.
 */
public class MsgpackDecoder implements Decoder {

    @Override
    public Object decode(byte[] datas,Class clazz) {

        Object result = MsgpackUtil.getIns().read(datas,clazz);

        return result;
    }

    @Override
    public Object decode(byte[] datas, String clazzTypeName) {
        Template template = MsgpackUtil.getIns().getTemplate(clazzTypeName);
        Object result = MsgpackUtil.getIns().read(datas,template);
        return result;
    }
}