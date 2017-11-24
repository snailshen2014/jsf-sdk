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

import com.ipd.jsf.gd.codec.Encoder;

/**
 * Title: MsgpackEncode实现<br>
 * <p/>
 * Description: 包含Encoder方法<br>
 * <p/>
 */
public class MsgpackEncoder implements Encoder {
    @Override
    /*
     *encode the object to byte[]..
     */
    public byte[] encode(Object obj) {

        byte[] data = MsgpackUtil.getIns().write(obj);

        return data;
    }

    @Override
    public byte[] encode(Object obj, String classTypeName) {
        byte[] data =  MsgpackUtil.getIns().write(obj, classTypeName);
        return data;
    }
}