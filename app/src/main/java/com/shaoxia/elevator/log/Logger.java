package com.shaoxia.elevator.log;

/**
 * Created by tustar on 16-3-23.
 */
public class Logger extends L {
    private boolean WRITE_FILE = true;

    public static void i(String tag, String... messages) {
        L.i(tag, false, messages);
    }

    public static void v(String tag, String... messages) {
        L.v(tag, false, messages);
    }

    public static void d(String tag, String... messages) {
        L.d(tag, false, messages);
    }

    public static void w(String tag, String... messages) {
        L.w(tag, false, messages);
    }

    public static void e(String tag, String... messages) {
        L.e(tag, false, messages);
    }
}
