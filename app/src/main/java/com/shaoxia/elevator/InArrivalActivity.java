package com.shaoxia.elevator;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class InArrivalActivity extends BaseArrivalActivity {
    private final int WAITINGTIME = 5000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, WAITINGTIME);
    }
}
