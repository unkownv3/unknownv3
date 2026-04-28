package me.bill.fakePlayerPlugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FppLogger {
    private static final String RESET = "\u001b[0m";
    private static final String BOLD = "\u001b[1m";
    private static final String BLUE = "\u001b[38;2;0;121;255m";
    private static final String WHITE = "\u001b[97m";
    private static final String YELLOW = "\u001b[93m";
    private static final String GREEN = "\u001b[92m";
    private static final String GOLD = "\u001b[33m";
    private static final String RED = "\u001b[91m";
    private static final String GRAY = "\u001b[90m";
    private static final String CYAN = "\u001b[96m";
    private static final String DARK = "\u001b[38;5;240m";
    private static final String TAG = BOLD + BLUE + "[\ua730\u1d18\u1d18]" + RESET;
    private static final int RULE_WIDTH = 50;
    private static final int KEY_WIDTH = 18;
    private static Logger logger;
    private static boolean debugEnabled = false;

    private FppLogger() {}

    public static void init() {
        logger = LoggerFactory.getLogger("FPP");
    }

    public static void setDebug(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void info(String message) {
        logger.info(TAG + " " + WHITE + message + RESET);
    }

    public static void success(String message) {
        logger.info(TAG + " " + GREEN + message + RESET);
    }

    public static void warn(String message) {
        logger.warn(TAG + " " + YELLOW + message + RESET);
    }

    public static void error(String message) {
        logger.error(TAG + " " + RED + message + RESET);
    }

    public static void error(String message, Throwable t) {
        logger.error(TAG + " " + RED + message + RESET, t);
    }

    public static void debug(String message) {
        debug("GENERAL", debugEnabled, message);
    }

    public static void debug(String topic, boolean enabled, String message) {
        if (!enabled) return;
        String label = (topic == null || topic.isBlank()) ? "DEBUG" : topic.trim().toUpperCase();
        logger.info(TAG + " " + GRAY + "[" + YELLOW + "DEBUG" + GRAY + "/" + CYAN + label + GRAY + "] " + YELLOW + message + RESET);
    }

    public static void highlight(String message) {
        logger.info(TAG + " " + BOLD + CYAN + message + RESET);
    }

    public static void rule() {
        logger.info(TAG + " " + DARK + "\u2500".repeat(RULE_WIDTH) + RESET);
    }

    public static void boldRule() {
        logger.info(TAG + " " + GRAY + BOLD + "\u2550".repeat(RULE_WIDTH) + RESET);
    }

    public static void section(String label) {
        String dashes = "\u2500".repeat(Math.max(0, RULE_WIDTH - label.length() - 4));
        logger.info(TAG + " " + DARK + "\u2500\u2500 " + RESET + BOLD + WHITE + label + " " + DARK + dashes + RESET);
    }

    public static void kv(String key, Object value) {
        int dots = Math.max(1, KEY_WIDTH - key.length());
        String dotStr = DARK + ".".repeat(dots) + RESET;
        logger.info(TAG + " " + GRAY + "  " + WHITE + key + " " + dotStr + " " + BLUE + String.valueOf(value) + RESET);
    }

    public static void statusRow(boolean ok, String label, String detail) {
        String badge = ok ? GREEN + "[+]" + RESET : RED + "[\u2718]" + RESET;
        int dots = Math.max(1, KEY_WIDTH - label.length());
        String dotStr = DARK + ".".repeat(dots) + RESET;
        String valueColor = ok ? GREEN : GRAY;
        logger.info(TAG + " " + GRAY + "  " + badge + " " + WHITE + label + " " + dotStr + " " + valueColor + detail + RESET);
    }
}
