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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 14-6-23.
 */

public class RingBufferService<T>  {
    // 等待处理完
    protected CountDownLatch latch = new CountDownLatch(1);
    // 派发线程
    protected Thread dispatchThread;
    // 最大容量
    protected int capacity;
    // 处理器
    protected EventHandler<T> handler;

    protected RingBuffer ringBuffer;   //BlockingRingBuffer
    // 是否启动

    protected AtomicBoolean started = new AtomicBoolean(false);
    // 线程名称
    protected String name;
    // 锁对象
    protected ReentrantLock lock;

    private static final Logger logger = LoggerFactory.getLogger(RingBufferService.class);




    /**
     * 构造函数
     *
     * @param capacity 最大容量
     * @param name     名称
     */
    public RingBufferService(int capacity, String name,EventHandler<T> handler) {
        this(capacity, name, null,handler);
    }

    public RingBufferService(int capacity, String name) {
        this(capacity, name, null,null);
    }

    /**
     * 构造函数
     *
     * @param capacity 最大容量
     * @param name     名称
     * @param lock     锁对象
     */
    public RingBufferService(int capacity, String name, ReentrantLock lock,EventHandler<T> handler) {
        this.capacity = capacity;
        this.name = name;
        this.lock = lock == null ? new ReentrantLock() : lock;
//        this.ringBuffer = new BlockingRingBuffer(capacity, "name goes here");//this.lock
        this.ringBuffer = new RingBuffer(capacity, this.lock);//this.lock
        this.handler = handler;
    }


    public void start() throws Exception {
        if (handler == null) {
            throw new IllegalStateException("handler can not be null");
        }
        lock.lock();
        try {
            if (started.compareAndSet(false, true)) {
                try {
                    dispatchThread = new Thread(new DispatchTask(), name == null ? "DoubleBuffer" : name);
                    dispatchThread.setDaemon(true);
                    dispatchThread.start();
                    logger.debug("dispatchThread has start in RingBuffer ");
                } catch (Exception e) {
                    stop();
                    throw e;
                }
            }
        } finally {
            lock.unlock();
        }
    }


    public void stop() {
        stop(5000);
    }

    /**
     * 停止，如果数据没用处理完，则等待一段时间直到完成或超时
     *
     * @param timeout 超时时间
     *                <li>>0 等待超时时间</li>
     *                <li>=0 等待处理完</li>
     *                <li><0 立即返回</li>
     */
    public void stop(long timeout) {
        lock.lock();
        try {
            if (started.compareAndSet(true, false)) {
               ringBuffer.stop();
                if (timeout < 0) {
                    return;
                }
                // 等待数据处理完
            }
        } finally {
            try {
                latch.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
            lock.unlock();
        }
    }


    public boolean isStarted() {
        return started.get();
    }

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    /**
     * 添加数据
     *
     * @param element 数据
     * @return 成功标示
     */
    public boolean add(final T element) {
        return ringBuffer.add(element,0);
    }

    /**
     * 添加数据
     *
     * @param element 数据
     * @param timeout 超时时间
     *                <li>>0 等待超时时间</li>
     *                <li>=0 无限等待</li>
     *                <li><0 立即返回</li>
     * @return 成功标示
     */
    public boolean add(final T element, final int timeout) {
        return ringBuffer.add(element, timeout);
    }

    /**
     * 是否还有未处理数据
     *
     * @return 是否还有未处理数据标示
     */
    public boolean hasRemaining() {
        return ringBuffer.hasRemaining();
    }

    /**
     * 处理请求
     *
     * @param elements 请求列表
     */
    protected void dispatch(final Object[] elements) {
        if (elements != null && elements.length > 0) {
            try {
                handler.onEvent(elements);
            } catch (Throwable e) {
                logger.error(e.getMessage(),e);
                handler.onException(e);
            }
        }
    }

    /**
     * 派发任务
     */
    protected class DispatchTask implements Runnable {
        @Override
        public void run() {
            try {
                Object[] elements;
                while (isStarted()) {
                    elements = ringBuffer.get(handler.getBatchSize(), 0);
                    // 处理数据
//                    logger.debug(" Elements to be Dispatch::{} ",elements.length);
                    dispatch(elements);
                }
                elements = ringBuffer.get(handler.getBatchSize(), -1);
                // 处理数据
                dispatch(elements);
                latch.countDown();
            } catch (Throwable e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    /**
     * 缓冲区处理器
     *
     * @param <T>
     */
    public interface EventHandler<T> {

        /**
         * 派发数据
         *
         * @param elements 消息列表
         * @throws Exception
         */
        void onEvent(Object[] elements) throws Exception;

        /**
         * 出现异常处理
         *
         * @param exception 异常
         */
        void onException(Throwable exception);

        /**
         * 返回批量大小
         *
         * @return 批量大小
         */
        int getBatchSize();
    }
}