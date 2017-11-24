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

/**
 * Title: MapDifference辅助类，表达一个Map.Entry里value的变化<br>
 * <p/>
 * Description: 包含变化前后的值<br>
 * <p/>
 * @see MapDifference
 */
public class ValueDifference<V> {

    /**
     * The Left value.
     */
    private V leftValue;
    /**
     * The Right value.
     */
    private V rightValue;

    /**
     * Instantiates a new Value difference.
     *
     * @param leftValue
     *         the left value
     * @param rightValue
     *         the right value
     */
    protected ValueDifference(V leftValue, V rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    /**
     * Left value.
     *
     * @return the v
     */
    public V leftValue() {
        return leftValue;
    }

    /**
     * Returns the value from the right map (possibly null).
     *
     * @return the v
     */
    public V rightValue() {
        return rightValue;
    }
}