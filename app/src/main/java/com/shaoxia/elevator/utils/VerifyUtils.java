package com.shaoxia.elevator.utils;

/**
 * Created by gonglt1 on 2018/3/10.
 */

public class VerifyUtils {
    public byte getCheckNum(byte[] bytes) {
        byte checkSum = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            checkSum += bytes[i];
        }
        return checkSum;
    }
}
