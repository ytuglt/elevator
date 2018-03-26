package com.shaoxia.elevator;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.shaoxia.elevator.crash.AppUncaughtExceptionHandler;
import com.shaoxia.elevator.crash.SdcardConfig;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class MyApplication extends Application {
//    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
//        mContext = getApplicationContext();

        // TODO
        mInstance = this;
        // 初始化文件目录
        SdcardConfig.getInstance().initSdcard();
        // 捕捉异常
        AppUncaughtExceptionHandler crashHandler = AppUncaughtExceptionHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

//    public static Context getInstance() {
//        return mContext;
//    }

    private static MyApplication mInstance = null;

    public static MyApplication getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Application is not created.");
        }
        return mInstance;
    }

    /**
     * 获取自身App安装包信息
     *
     * @return
     */
    public PackageInfo getLocalPackageInfo() {
        return getPackageInfo(getPackageName());
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo(String packageName) {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
}
