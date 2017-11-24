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
package com.ipd.jsf.gd.logger;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public final class JSFLoggerFactory {

    private final static ConcurrentHashMap<String, JSFLogger> JSFLOGGER_MAP = new ConcurrentHashMap<String, JSFLogger>();

    public static JSFLogger getLogger(Class clazz) {
        String key = clazz.getName();
        JSFLogger jsfLogger = JSFLOGGER_MAP.get(clazz.getName());
        if (jsfLogger == null) {
            Logger slflogger = LoggerFactory.getLogger(clazz);
            jsfLogger = CommonUtils.putToConcurrentMap(JSFLOGGER_MAP, key, new JSFLogger(slflogger));
        }
        return jsfLogger;
    }
}