package com.verint.xm.sdk.adobeExtension.logging;

import android.util.Log;

import com.verint.xm.sdk.common.utils.Util;


public class Logging {

    public static final LogLevel TEST_LOG_LEVEL_MIN = LogLevel.WARN;

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3);

        public int value;
        LogLevel(int value) {
            this.value = value;
        }
    }

    /*
    Logging should follow the following rules:
    1. Initial setup of the SDK and any errors that prevent a major function of the SDK should
    always be output using alwaysLog
    2. Anything that should only be seen by internal should use the internalLog function
    3. The logging levels should be used as follows:
        DEBUG:  For showing events used specifically to debug certain scenarios
        INFO:   General day-to-day running of the app - stuff that happens a lot
        WARN:   Non-fatal errors
        ERROR:  Errors that might cause problems
     */

    /**
     * Log directly to logCat. These log messages will always be seen
     *
     * @param level   the logging level
     * @param handle  the handle applied to the log message
     * @param message the message to log
     */
    public static void alwaysLog(LogLevel level, String handle, String message) {
        logToLogcat(level, handle, message, null);
    }

    /**
     * Log directly to logCat with throwable. These log messages will always be seen
     *
     * @param level   the logging level
     * @param handle  the handle applied to the log message
     * @param message the message to log
     * @param t       the Throwable to include with the log
     */
    public static void alwaysLog(LogLevel level, String handle, String message, Throwable t) {
        logToLogcat(level, handle, message, t);
    }

    /**
     * Log debug information for internal only. These log messages will not be seen in release builds
     *
     * @param level   the logging level
     * @param handle  the handle applied to the log message
     * @param message the message to log
     */
    public static void internalLog(LogLevel level, String handle, String message) {
        internalLog(level, handle, message, null);
    }

    /**
     * Log debug information with throwable for internal only. These log messages will not be seen
     * in release builds
     *
     * @param level   the logging level
     * @param handle  the handle applied to the log message
     * @param message the message to log
     * @param t       the Throwable to include with the log
     */
    public static void internalLog(LogLevel level, String handle, String message, Throwable t) {

        if (t != null) {
            alwaysLog(level, handle, message, t);
        } else {
            alwaysLog(level, handle, message);
        }
    }

    private static void logToLogcat(LogLevel level, String handle, String message, Throwable t) {
        handle = normalizeHandleLength(handle);
        String msg = Util.sanitize(message);
        if (t != null) {
            if (level == LogLevel.DEBUG) {
                Log.d(handle, msg, t);
            } else if (level == LogLevel.INFO) {
                Log.i(handle, msg, t);
            } else if (level == LogLevel.WARN) {
                Log.w(handle, msg, t);
            } else if (level == LogLevel.ERROR) {
                Log.e(handle, msg, t);
            }
        } else {
            if (level == LogLevel.DEBUG) {
                Log.d(handle, msg);
            } else if (level == LogLevel.INFO) {
                Log.i(handle, msg);
            } else if (level == LogLevel.WARN) {
                Log.w(handle, msg);
            } else if (level == LogLevel.ERROR) {
                Log.e(handle, msg);
            }
        }
    }

    private static boolean shouldLogInTest(LogLevel level) {
        return level.value > TEST_LOG_LEVEL_MIN.value;
    }

    private static String normalizeHandleLength(String handle) {
        for (int index = handle.length(); index < 22; index++) {
            handle += "\u00A0";
        }

        return handle;
    }


    public static String trimClassName(String className) {
        int lastDotIndex = className.lastIndexOf(".");
        return className.substring(lastDotIndex + 1);
    }
}
