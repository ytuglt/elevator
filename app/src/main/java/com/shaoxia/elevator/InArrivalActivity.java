package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class InArrivalActivity extends BaseArrivalActivity {
    private static final String TAG = "InArrivalActivity";
    private final int WAITINGTIME = 5000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "run: ");
                finish();
            }
        }, WAITINGTIME);
    }

    @Override
    protected int getLightPos() {
        return mDesPos;
    }
}
