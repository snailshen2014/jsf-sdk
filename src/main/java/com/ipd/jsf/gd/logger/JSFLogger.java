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

import org.slf4j.Marker;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFLogger {

    /**
     * 是否打印的开关
     */
    public static volatile boolean print = false;
    /**
     * The Slf 4j logger.
     */
    private org.slf4j.Logger slf4jLogger;

    /**
     * Instantiates a new JSF logger.
     *
     * @param slf4jLogger
     *         the slf 4 j logger
     */
    public JSFLogger(org.slf4j.Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    private org.slf4j.Logger getLogger() {
        return slf4jLogger;
    }

    public String getName() {
        return slf4jLogger.getName();
    }

    public boolean isTraceEnabled() {
        return print && slf4jLogger.isTraceEnabled();
    }

    public void trace(String msg) {
        if (print) {
            slf4jLogger.trace(msg);
        }
    }

    public void trace(String format, Object arg) {
        if (print) {
            slf4jLogger.trace(format, arg);
        }
    }

    public void trace(String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.trace(format, arg1, arg2);
        }
    }

    public void trace(String format, Object[] argArray) {
        if (print) {
            slf4jLogger.trace(format, argArray);
        }
    }

    public void trace(String msg, Throwable t) {
        if (print) {
            slf4jLogger.trace(msg, t);
        }
    }

    public boolean isTraceEnabled(Marker marker) {
        return print && slf4jLogger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        if (print) {
            slf4jLogger.trace(marker, msg);
        }
    }

    public void trace(Marker marker, String format, Object arg) {
        if (print) {
            slf4jLogger.trace(marker, format, arg);
        }
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.trace(marker, format, arg1, arg2);
        }
    }

    public void trace(Marker marker, String format, Object[] argArray) {
        if (print) {
            slf4jLogger.trace(marker, format, argArray);
        }
    }

    public void trace(Marker marker, String msg, Throwable t) {
        if (print) {
            slf4jLogger.trace(marker, msg, t);
        }
    }

    public boolean isDebugEnabled() {
        return print && slf4jLogger.isDebugEnabled();
    }

    public void debug(String msg) {
        if (print) {
            slf4jLogger.debug(msg);
        }
    }

    public void debug(String format, Object arg) {
        if (print) {
            slf4jLogger.debug(format, arg);
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.debug(format, arg1, arg2);
        }
    }

    public void debug(String format, Object[] argArray) {
        if (print) {
            slf4jLogger.debug(format, argArray);
        }
    }

    public void debug(String msg, Throwable t) {
        if (print) {
            slf4jLogger.debug(msg, t);
        }
    }

    public boolean isDebugEnabled(Marker marker) {
        return print && slf4jLogger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        if (print) {
            slf4jLogger.debug(marker, msg);
        }
    }

    public void debug(Marker marker, String format, Object arg) {
        if (print) {
            slf4jLogger.debug(marker, format, arg);
        }
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.debug(marker, format, arg1, arg2);
        }
    }

    public void debug(Marker marker, String format, Object[] argArray) {
        if (print) {
            slf4jLogger.debug(marker, format, argArray);
        }
    }

    public void debug(Marker marker, String msg, Throwable t) {
        if (print) {
            slf4jLogger.debug(marker, msg, t);
        }
    }

    public boolean isInfoEnabled() {
        return print && slf4jLogger.isInfoEnabled();
    }

    public void info(String msg) {
        if (print) {
            slf4jLogger.info(msg);
        }
    }

    public void info(String format, Object arg) {
        if (print) {
            slf4jLogger.info(format, arg);
        }
    }

    public void info(String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.info(format, arg1, arg2);
        }
    }

    public void info(String format, Object[] argArray) {
        if (print) {
            slf4jLogger.info(format, argArray);
        }
    }

    public void info(String msg, Throwable t) {
        if (print) {
            slf4jLogger.info(msg, t);
        }
    }

    public boolean isInfoEnabled(Marker marker) {
        return print && slf4jLogger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        if (print) {
            slf4jLogger.info(marker, msg);
        }
    }

    public void info(Marker marker, String format, Object arg) {
        if (print) {
            slf4jLogger.info(marker, format, arg);
        }
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.info(marker, format, arg1, arg2);
        }
    }

    public void info(Marker marker, String format, Object[] argArray) {
        if (print) {
            slf4jLogger.info(marker, format, argArray);
        }
    }

    public void info(Marker marker, String msg, Throwable t) {
        if (print) {
            slf4jLogger.info(marker, msg, t);
        }
    }

    public boolean isWarnEnabled() {
        return print && slf4jLogger.isWarnEnabled();
    }

    public void warn(String msg) {
        if (print) {
            slf4jLogger.warn(msg);
        }
    }

    public void warn(String format, Object arg) {
        if (print) {
            slf4jLogger.warn(format, arg);
        }
    }

    public void warn(String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.warn(format, arg1, arg2);
        }
    }

    public void warn(String format, Object[] argArray) {
        if (print) {
            slf4jLogger.warn(format, argArray);
        }
    }

    public void warn(String msg, Throwable t) {
        if (print) {
            slf4jLogger.warn(msg, t);
        }
    }

    public boolean isWarnEnabled(Marker marker) {
        return print && slf4jLogger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        if (print) {
            slf4jLogger.warn(marker, msg);
        }
    }

    public void warn(Marker marker, String format, Object arg) {
        if (print) {
            slf4jLogger.warn(marker, format, arg);
        }
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.warn(marker, format, arg1, arg2);
        }
    }

    public void warn(Marker marker, String format, Object[] argArray) {
        if (print) {
            slf4jLogger.warn(marker, format, argArray);
        }
    }

    public void warn(Marker marker, String msg, Throwable t) {
        if (print) {
            slf4jLogger.warn(marker, msg, t);
        }
    }

    public boolean isErrorEnabled() {
        return print && slf4jLogger.isErrorEnabled();
    }

    public void error(String msg) {
        if (print) {
            slf4jLogger.error(msg);
        }
    }

    public void error(String format, Object arg) {
        if (print) {
            slf4jLogger.error(format, arg);
        }
    }

    public void error(String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.error(format, arg1, arg2);
        }
    }

    public void error(String format, Object[] argArray) {
        if (print) {
            slf4jLogger.error(format, argArray);
        }
    }

    public void error(String msg, Throwable t) {
        if (print) {
            slf4jLogger.error(msg, t);
        }
    }

    public boolean isErrorEnabled(Marker marker) {
        return print && slf4jLogger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        if (print) {
            slf4jLogger.error(marker, msg);
        }
    }

    public void error(Marker marker, String format, Object arg) {
        if (print) {
            slf4jLogger.error(marker, format, arg);
        }
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (print) {
            slf4jLogger.error(marker, format, arg1, arg2);
        }
    }

    public void error(Marker marker, String format, Object[] argArray) {
        if (print) {
            slf4jLogger.error(marker, format, argArray);
        }
    }

    public void error(Marker marker, String msg, Throwable t) {
        if (print) {
            slf4jLogger.error(marker, msg, t);
        }
    }
}