package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.VerifyUtils;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class InWaitingActivity extends BaseWaitingActivity {
    private static final String TAG = "InWaitingActivity";
    private MDevice mCopDevice;

    private byte mRealFloor = 0;

    @Override
    protected void getUpOrDown() {
        mIsUp = getIntent().getBooleanExtra("isUp", true);
        Logger.d(TAG, "getUpOrDown: mIsUp = " + mIsUp);
        mCopDevice = (MDevice) getIntent().getSerializableExtra("copdevice");
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
    protected void call() {
        Logger.d(TAG, "call: ");
        mBleComManager.setDevAddress(mCopDevice.getDevAddress());
        mBleComManager.setDevName(mCopDevice.getDevName());

        getFloor();
        byte[] cmd = new byte[4];
        cmd[0] = (byte) 0xb5;
        cmd[1] = mRealFloor;
        cmd[2] = (byte) 0x00;
        cmd[3] = (byte) 0x00;
        cmd[3] = VerifyUtils.getCheckNum(cmd);

        mBleComManager.sendData(cmd);
    }

    @Override
    protected void getStatus() {
        if (mIsComunicating) {
            Logger.d(TAG, "getStatus: is comunicating");
            return;
        }
        mBleComManager.setDevAddress(mCopDevice.getDevAddress());
        mBleComManager.setDevName(mCopDevice.getDevName());

        getFloor();
        byte[] cmd = new byte[4];
        cmd[0] = (byte) 0xb5;
        cmd[1] = mRealFloor;
        cmd[2] = (byte) 0x80;
        cmd[3] = (byte) 0x00;
        cmd[3] = VerifyUtils.getCheckNum(cmd);

        mBleComManager.sendData(cmd);
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
        if (array[1] == (byte) 0x00) {
            Logger.d(TAG, "parseData: light turn off");
            Intent intent = new Intent(this, InArrivalActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("device", mDevice);//序列化
            intent.putExtras(bundle);//发送数据
            intent.putExtra("despos", mDesPos);
            intent.putExtra("id", mDevice.getElevatorId());
            intent.putExtra("isUp", mIsUp);
            startActivity(intent);
            finish();
        }
    }


}
