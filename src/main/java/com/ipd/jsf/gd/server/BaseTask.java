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
package com.ipd.jsf.gd.server;

import java.util.Comparator;

/**
 * Title: 业务线程任务<br>
 * <p/>
 * Description: 会丢到业务线程执行<br>
 * <p/>
 */
public abstract class BaseTask implements Runnable,Comparator<BaseTask> {

    private int priority;

    private long timeout;




    @Override
    public void run() {


        doRun();
    }

    abstract void doRun();

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    @Override
    public int compare(BaseTask o1, BaseTask o2) {
        return o1.getPriority() - o2.getPriority();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseTask)) return false;

        BaseTask that = (BaseTask) o;

        if (priority != that.priority) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return priority;
    }
}