package me.bill.fpp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FppLogger {
    private static Logger logger;

    private FppLogger() {}

    public static void init() {
        logger = LoggerFactory.getLogger("FPP");
    }

    public static void info(String msg) {
        if (logger != null) logger.info(msg);
    }

    public static void warn(String msg) {
        if (logger != null) logger.warn(msg);
    }

    public static void error(String msg) {
        if (logger != null) logger.error(msg);
    }

    public static void error(String msg, Throwable t) {
        if (logger != null) logger.error(msg, t);
    }

    public static void debug(String msg) {
        if (logger != null) logger.debug(msg);
    }
}
