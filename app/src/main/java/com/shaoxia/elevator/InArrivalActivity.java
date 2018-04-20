package com.shaoxia.elevator;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.shaoxia.elevator.log.Logger;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class InArrivalActivity extends BaseArrivalActivity {
    private static final String TAG = "InArrivalActivity";
    private final int WAITINGTIME = 2000;

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
    protected void initTitleView() {
        String floor = "";
        if (mDesPos < mDevice.getFloors().size() && mDesPos >= 0) {
            floor = mDevice.getFloors().get(mDesPos);
        } else {
            Logger.d(TAG, "initTitleView: mDesPos is out of floors");
        }
        String title = mElevatorId + getResources().getString(R.string.elevator_id_unit) +
                floor + getResources().getString(R.string.floor_unit);
        mTitleView.setText(title);
    }

    @Override
    protected void setLightFloorBg(TextView textView) {
        textView.setBackgroundResource(R.drawable.in_arrival_light_floor_bg);
        textView.setTextColor(getResources().getColor(R.color.call_enable, null));
    }

    @Override
    protected int getLightPos() {
        return mDesPos;
    }
}
