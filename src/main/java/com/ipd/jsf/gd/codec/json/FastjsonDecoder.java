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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.gd.codec.Decoder;
import com.ipd.jsf.gd.util.ClassLoaderUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 * @since 2015/01/09 13:29
 */
public class FastjsonDecoder implements Decoder {

    private final static Logger logger = LoggerFactory.getLogger(FastjsonDecoder.class);

    @Override
    public Object decode(byte[] datas, Class clazz) {
        return JSON.parseObject(datas,clazz);
    }

    @Override
    public Object decode(byte[] datas, String className) {
        try {
            return decode(datas, ClassLoaderUtils.forName(className));
        } catch (ClassNotFoundException e) {
            logger.error("decode error",e);
            return null;
        }
    }
}