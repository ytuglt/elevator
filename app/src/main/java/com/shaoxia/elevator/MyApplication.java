package com.shaoxia.elevator;

import android.app.Application;
import android.content.Context;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class MyApplication extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }
}
