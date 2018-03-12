package com.shaoxia.elevator.utils;

/**
 * Created by gonglt1 on 2018/3/11.
 */

public class CoderUtils {
    public static String asciiToString(byte[] values) {
        StringBuffer sbu = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            sbu.append((char) values[i]);
        }
        return sbu.toString();
    }

    public static String asciiToString(byte value) {
        return String.valueOf((char) value);
    }
}
