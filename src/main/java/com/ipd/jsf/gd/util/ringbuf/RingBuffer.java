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

import com.ipd.jsf.gd.util.LockUtil;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 14-6-19.
 */
public class RingBuffer<T> {

    private Object[] buf;

    private int reader;

    private int writer;

    private final ReentrantLock lock;

    private final Condition buf_full;

    private final Condition data_readable;

    private int bufCap;

    private final int maxWriter ;

    private final static Logger logger = LoggerFactory.getLogger(RingBuffer.class);

   public RingBuffer(int capacity,ReentrantLock locks){

       if(capacity < 1 || capacity > 32768)
           throw new IllegalArgumentException("incorrect capacity of " + capacity);

       // Find a power of 2 >= buffer capacity
       int cap = 1;
       while (capacity > cap)
           cap <<= 1;
       this.lock = locks;
       this.buf=new Object[cap];
       this.reader = this.writer = -1;
       bufCap = cap;
       //maxWriter = 4 * (bufCap;
       maxWriter =  5*bufCap;
       buf_full = lock.newCondition();
       data_readable = lock.newCondition();
       logger.debug("bufCap:{}  maxWriter:{}",bufCap,maxWriter);

   }



    public void stop() {
        lock.lock();
        try {
               buf_full.signalAll();
               data_readable.signalAll();


        } finally {
            lock.unlock();
        }
    }

    public boolean add(T element, int timeout) {

        if (element == null) {
            return false;
        }
        long time = LockUtil.tryLock(lock, timeout);
        if (time == LockUtil.LOCK_FAIL) {
            return false;
        }
        try {
            if (!waitSpace(time)) {
                return false;
            }
            buf[index(++writer)] = element;
            if (lock.hasWaiters(data_readable)) {
                data_readable.signal();
            }
//            else{
//                logger.info("No waiters..no signalAll()..");
//            }
        } finally {
            lock.unlock();
        }
        return true;

    }

    /*
     * reduce the index to avid to overflow
     */
    private int readerIncrease(){
        if(reader == maxWriter){
            writer = writer - maxWriter;
            reader = reader -maxWriter;
            //logger.info("after reduce:writer:"+writer+" reader::"+reader);
        }

        ++reader;
        return reader;
    }

    /*
     * get function do not need a lock.
     */
    public Object[] get(int maxSize, int timeout) {
        Object[] tList = null;

        long time = LockUtil.tryLock(lock, timeout);
        if (time == LockUtil.LOCK_FAIL) {
            return new Object[0];
        }
        try {
            if (!waitData(timeout)) {
                return new Object[0];
            }

                int count = writer - reader;
                if (maxSize > 0 && count > maxSize) {
                    count = maxSize;
                }

                tList = new Object[count];
                int pos = 0;
                while (pos<count) {
                    int tmpPointer =  index(readerIncrease());
                    tList[pos++] = (buf[tmpPointer]);
                    buf[tmpPointer] = null;
                }
            if(tList.length > 0){
               if(lock.hasWaiters(buf_full)){
                   buf_full.signalAll();
               }
            }

        } finally {
            lock.unlock();
        }
         //logger.info(" after get() reader:{} writer:{}",reader,writer);
        return tList;

    }

    /**
     * 等待写入空间
     *
     * @param timeout 超时时间(毫秒)
     * @return 成功标示
     */
    protected boolean waitSpace(final long timeout) {
        if (hasSpace()) {
            return true;
        }
        if (timeout < 0) {
            return false;
        }
       try{
           long time = timeout;
           long startTime;
           long endTime;
        while (true) {
            if (timeout == 0) {
                buf_full.await();
            } else {
                startTime = JSFContext.systemClock.now();
                if (!buf_full.await(time, TimeUnit.MILLISECONDS)) {
                    return false;
                }
                endTime = JSFContext.systemClock.now();
                time = time - (endTime - startTime);
            }
            // 多个生产者，空间可能被其它线程占用了，需要循环等待
            if (hasSpace()) {
                return true;
            }
        }

        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 等待数据,为了尽可能处理完数据,不校验是否关闭
     *
     * @param timeout 超时时间
     * @return 成功标示
     */
    protected boolean waitData(final long timeout) {
        if (hasRemaining()) {
            return true;
        }
        if (timeout < 0) {
            return false;
        }
        try {

            if(timeout==0){
                data_readable.await();
            }else{
                data_readable.await(timeout, TimeUnit.MILLISECONDS);
            }
            return hasRemaining();
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 是否有空间
     *
     * @return 有空间标示
     */
    protected boolean hasSpace() {

        return (writer - reader) < bufCap;
    }

    /**
     * 计算数组中的索引
     *
     * @param position 全局位置
     * @return 位置
     */
    protected int index(int position) {
        return (position & (bufCap - 1));
    }

    /**
     * 是否有数据未处理
     *
     * @return 有数据未处理标示
     */
    public boolean hasRemaining() {
        return writer != reader;
    }

    public int getCapacity() {
        return bufCap;
    }




}