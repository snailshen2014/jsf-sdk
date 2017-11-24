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
package com.ipd.jsf.gd.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Title: 自己实现的一种循环queue，先进先出，线程安全<br>
 * <p/>
 * Description: 特性：1.当队列满的时候，剔除队首最旧数据，再放入数据，2.支持批量添加和批量获取<br>
 * <p/>
 */
public class CircularQueue<E> {

    /**
     * The Queue.
     */
    private final LinkedBlockingQueue<E> queue;

    /**
     * The Max size.
     */
    private final int maxSize;

    /**
     * Instantiates a new Circular queue.
     *
     * @param maxSize
     *         the max size
     */
    public CircularQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<E>(maxSize);
    }


    /**
     * Add void.
     *
     * @param e
     *         the e
     * @return the boolean
     */
    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        //synchronized (this) {    remove synchronized..
            if (queue.size() == maxSize) {
                queue.poll(); // 满了先删一个
            }
            queue.add(e);
       // }
        return true;
    }

    /**
     * Add all.
     *
     * @param c
     *         the c
     * @return the boolean
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        boolean modified = false;
        Iterator<? extends E> e = c.iterator();
        while (e.hasNext()) {
            if (add(e.next()))
                modified = true;
        }
        return modified;
    }

    /**
     * Poll e.
     *
     * @return the e
     */
    public E poll() {
        return queue.poll();
    }

    /**
     * 返回期望大小的值
     *
     * @param desiredSize
     *         期望大小
     * @return 得到小于等于期望大小的List list
     */
    public List<E> batchPoll(int desiredSize) {
        if (desiredSize < 1 || desiredSize > maxSize) {
            throw new IllegalArgumentException("Desired size must greater than zero" +
                    " and not greater than max size");
        }
        int size = desiredSize > queue.size() ? queue.size() : desiredSize;
        List<E> list = new ArrayList<E>(size);
        for (int i = 0; i < size; i++) {
            E e = queue.poll();
            if (e == null) {
                break;
            } else {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Peek e.
     *
     * @return the e
     */
    public E peek() {
        return queue.peek();
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return queue.size();
    }

    /**
     * Clear void.
     */
    public void clear() {
        queue.clear();
    }
}