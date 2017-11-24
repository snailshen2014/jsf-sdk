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
package com.ipd.jsf.gd.codec.jsf;

import com.ipd.jsf.gd.codec.LengthFieldBasedFrameDecoder;
import com.ipd.jsf.gd.protocol.JSFProtocol;
import com.ipd.jsf.gd.protocol.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * Title: jsf解码类<br>
 * <p/>
 * Description: <br>
 * <p/>
 * @see JSFProtocol
 */
public class JSFDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JSFDecoder.class);

    public JSFDecoder(int maxFrameLength){
        /*
        int maxFrameLength,     最大值
        int lengthFieldOffset,  魔术位2B，然后是长度4B，所以偏移：2
        int lengthFieldLength,  总长度占4B，所以长度是：4
        int lengthAdjustment,   总长度的值包括自己，剩下的长度=总长度-4B 所以调整值是：-4
        int initialBytesToStrip 前面6位不用再读取了，可以跳过，所以跳过的值是：6
        */
        super(maxFrameLength, 2, 4, -4, 6);
    }

    @Override
    public Object decodeFrame(ByteBuf frame) {
        Object result = ProtocolUtil.decode(frame);
        if(logger.isTraceEnabled()) {
            logger.trace("decoder result:{}", result);
        }
        return result;
    }
}