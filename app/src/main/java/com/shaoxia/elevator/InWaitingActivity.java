package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.VerifyUtils;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class InWaitingActivity extends BaseWaitingActivity {
    private static final String TAG = "InWaitingActivity";

    public static final String COP_DEVICE_KEY = "copdevice";
    public static final String ENTER_FROM_KEY = "enter_from";

    public static final int ENTER_FORM_SPLASH = 1;
    public static final int ENTER_FORM_OUTARRIVAL = 2;

    private MDevice mCopDevice;

    private byte mRealFloor = 0;

    private int mEnterFrom = ENTER_FORM_OUTARRIVAL;

    @Override
    protected void initIntentData() {
        super.initIntentData();
        mEnterFrom = getIntent().getIntExtra(ENTER_FROM_KEY, ENTER_FORM_OUTARRIVAL);
    }

    @Override
    protected void initTitleView() {
        String title = mCopDevice.getElevatorId() + getResources().getString(R.string.elevator_id_unit) +
                getResources().getString(R.string.in_elevator);
        mTitleView.setText(title);
    }

    @Override
    protected void getUpOrDown() {
        mIsUp = getIntent().getBooleanExtra("isUp", true);
        Logger.d(TAG, "getUpOrDown: mIsUp = " + mIsUp);
        mCopDevice = (MDevice) getIntent().getParcelableExtra(COP_DEVICE_KEY);
        if (mEnterFrom == ENTER_FORM_SPLASH) {
            mDevice = mCopDevice;
        }
    }

    @Override
    protected int getLightPos() {
        return mDesPos;
    }

    private void getFloor() {
        if (mRealFloor == 0) {
            mRealFloor = mDevice.getFloorsMap().get(mDevice.getFloors().get(mDesPos));
        }
    }

    @Override
    protected void updateAnimView() {
        if (mEnterFrom == ENTER_FORM_OUTARRIVAL) {
            super.updateAnimView();
        } else {
            startAnimView(R.drawable.up_down_anim);
        }
    }

    @Override
    protected byte[] getCallData() {
        getFloor();
        byte[] cmd = new byte[4];
        cmd[0] = (byte) 0xb5;
        cmd[1] = mRealFloor;
        cmd[2] = (byte) 0x00;
        cmd[3] = (byte) 0x00;
        cmd[3] = VerifyUtils.getCheckNum(cmd);
        return cmd;
    }

    @Override
    protected byte[] getQueryData() {
        getFloor();
        byte[] cmd = new byte[4];
        cmd[0] = (byte) 0xb5;
        cmd[1] = mRealFloor;
        cmd[2] = (byte) 0x80;
        cmd[3] = (byte) 0x00;
        cmd[3] = VerifyUtils.getCheckNum(cmd);
        return cmd;
    }

    @Override
    protected MDevice getComDevice() {
        return mCopDevice;
    }

    @Override
    protected boolean checkData(byte[] array) {
        if (array == null && array.length != 4) {
            Logger.e(TAG, "onReceiveData: return data null or to length error");
            return false;
        }

        if (array[0] != (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head error");
            return false;
        }

        byte sum = VerifyUtils.getCheckNum(array);
        if (sum != array[array.length - 1]) {
            Logger.e(TAG, "onReceiveData: checksum error");
            return false;
        }

        return true;
    }

    @Override
    protected void parseData(byte[] array) {
        if (isFinishing()) {
            Logger.d(TAG, "parseData: is finishing");
            return;
        }
        if (array[1] == (byte) 0x00) {
            MDevice device;

            if (mEnterFrom == ENTER_FORM_SPLASH) {
                device = mCopDevice;
            } else {
                device = mDevice;
            }

            Logger.d(TAG, "parseData: light turn off");
            Intent intent = new Intent(this, InArrivalActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("device", device);//序列化
            intent.putExtras(bundle);//发送数据
            intent.putExtra("despos", mDesPos);
            intent.putExtra("id", device.getElevatorId());
            intent.putExtra("isUp", mIsUp);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Logger.d(TAG, "onBackPressed: ");
    }
}
