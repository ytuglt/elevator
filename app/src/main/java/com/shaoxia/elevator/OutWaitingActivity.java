package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.VerifyUtils;

import java.util.List;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class OutWaitingActivity extends BaseWaitingActivity {
    private static final String TAG = "OutWaitingActivity";

    @Override
    protected void getUpOrDown() {
        if (mDevice == null) {
            Log.e(TAG, "getUpOrDown: mDevice is null");
            return;
        }
        List<String> floors = mDevice.getFloors();
        if (floors == null) {
            Log.e(TAG, "getUpOrDown: floors is null");
            return;
        }
        int curpos = floors.indexOf(mDevice.getFloor());
        Logger.d(TAG, "getUpOrDown: mDesPos = " + mDesPos + ",curpos =" + curpos
                + ",mDevice.getFloor() =" + mDevice.getFloor());
        mIsUp = (mDesPos < curpos);
        Logger.d(TAG, "getUpOrDown: mIsUp = " + mIsUp);
    }

    @Override
    protected int getLightPos() {
        return mDevice.getFloors().indexOf(mDevice.getFloor());
    }

    @Override
    protected byte[] getCallData() {
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        if (mIsUp) {
            cmd[1] = (byte) 0x01;
            cmd[2] = (byte) 0xb6;
        } else {
            cmd[1] = (byte) 0x02;
            cmd[2] = (byte) 0xb7;
        }
        return cmd;
    }

    @Override
    protected byte[] getQueryData() {
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0xb5;
        return cmd;
    }

    @Override
    protected MDevice getComDevice() {
        return mDevice;
    }

    @Override
    protected boolean checkData(byte[] array) {
        Logger.d(TAG, "checkData: ");
        if (array == null && array.length != 3) {
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
        Logger.d(TAG, "parseData: ");
        boolean isOff;
        if (mIsUp) {
            isOff = (array[1] & 0x01) == (byte) 0;
        } else {
            isOff = (array[1] & 0x02) == (byte) 0;
        }

        if (isOff) {
//            mBleComManager.disconnect();
            Intent intent = new Intent(this, OutArrivalActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("device", mDevice);//序列化
            intent.putExtras(bundle);//发送数据
            intent.putExtra("despos", mDesPos);
            intent.putExtra("id", mDevice.getElevatorId());
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
