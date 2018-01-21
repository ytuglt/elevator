package com.shaoxia.elevator.utils;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class StringUtils {
    public static byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 != 0) {
            StringBuilder stringBuilder = new StringBuilder(s);
            stringBuilder.insert(s.length() - 1, "0");
            s = stringBuilder.toString();
        }


        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * bytes to hex string
     *
     * @param
     * @return
     */

    public static String ByteArraytoHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String bs = String.format("%02X ", b);
            sb.append(bs);
        }
        return sb.toString();
    }
}
