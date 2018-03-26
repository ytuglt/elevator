package com.shaoxia.elevator.crash;

/**
 * Created by gonglt1 on 2018/3/26.
 */

import android.app.Activity;

public class PatchBaseActivity extends Activity {

    @Override
    final protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}