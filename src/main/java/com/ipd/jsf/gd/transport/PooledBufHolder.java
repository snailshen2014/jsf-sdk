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
package com.ipd.jsf.gd.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 *
 * Allocator Example: PooledBufHolder.getBuffer()
 *
 */
public class PooledBufHolder {

    private static ByteBufAllocator pooled = new UnpooledByteBufAllocator(false);

    public static ByteBufAllocator getInstance(){

        return pooled;
    }

    public static ByteBuf getBuffer(){

        return pooled.buffer();
    }

    public static ByteBuf getBuffer(int size){
        return pooled.buffer(size);
    }

}