package com.shaoxia.elevator.utils;

import android.os.Build;


/**
 * Created by gonglt1 on 2018/4/14.
 */

public class SystemUtils {

    public static boolean isLOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
