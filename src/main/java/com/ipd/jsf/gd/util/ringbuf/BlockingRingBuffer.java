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
package com.ipd.jsf.gd.util.ringbuf;

import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 14-7-4.
 */
public class BlockingRingBuffer {

    private final ArrayBlockingQueue queue;

    private String localName;

    private static final Logger logger = LoggerFactory.getLogger(BlockingRingBuffer.class);

    BlockingRingBuffer(int size,String name){
        queue = new ArrayBlockingQueue(size);
        this.localName = name;
    }

    public boolean add(Object obj){
        return queue.add(obj);
    }

    public void stop(){
        logger.info("BlockingRingBuffer close...");
    }

    public boolean add(Object obj,int timeout){

        return queue.add(obj);
    }


    public boolean hasRemaining(){
        return queue.size() > 0;
    }


    public Object[] get(int maxSize,int timeout){
       int queueSize = queue.size();
        int length = maxSize;
        if(maxSize > queueSize)length = queueSize;
        Object[] objs = new Object[length];

            for(int i=0; i<length; i++){
                try {
                objs[i] = queue.take();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(),e);
                }
            }

        return objs;
    }




}