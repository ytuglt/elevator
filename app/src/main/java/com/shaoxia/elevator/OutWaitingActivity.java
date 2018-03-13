package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;
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
        mIsUp = (mDesPos < curpos);
        Logger.d(TAG, "getUpOrDown: mIsUp = " + mIsUp);
    }

    @Override
    protected int getLightPos() {
        return mDevice.getFloors().indexOf(mDevice.getFloor());
    }

    @Override
    protected void call() {
        Logger.d(TAG, "call: ");
        mBleComManager.setDevAddress(mDevice.getDevAddress());
        mBleComManager.setDevName(mDevice.getDevName());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        if (mIsUp) {
            cmd[1] = (byte) 0x01;
            cmd[2] = (byte) 0xb6;
        } else {
            cmd[1] = (byte) 0x02;
            cmd[2] = (byte) 0xb7;
        }

        mBleComManager.sendData(cmd);
    }

    @Override
    protected void getStatus() {
        Logger.d(TAG, "getStatus: ");
        if (mIsComunicating) {
            Logger.d(TAG, "getStatus: is comunicating");
            return;
        }
        mBleComManager.setDevAddress(mDevice.getDevAddress());
        mBleComManager.setDevName(mDevice.getDevName());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0xb5;
        mBleComManager.sendData(cmd);
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
            mBleComManager.disconnect();
            mBleComManager.destroy();
            Intent intent = new Intent(this, OutArrivalActivity.class);
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
