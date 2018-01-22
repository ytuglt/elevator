package com.shaoxia.elevator.log;

/**
 * Created by tustar on 16-3-23.
 */
public class Logger extends L {
    public static boolean WRITE_FILE = true;

    public static void i(String tag, String... messages) {
        L.i(tag, WRITE_FILE, messages);
    }

    public static void v(String tag, String... messages) {
        L.v(tag, WRITE_FILE, messages);
    }

    public static void d(String tag, String... messages) {
        L.d(tag, WRITE_FILE, messages);
    }

    public static void w(String tag, String... messages) {
        L.w(tag, WRITE_FILE, messages);
    }

    public static void e(String tag, String... messages) {
        L.e(tag, WRITE_FILE, messages);
    }
}
