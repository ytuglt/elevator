package com.shaoxia.elevator.logic;

import android.os.Handler;
import android.util.Log;

import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.shaoxia.elevator.fastble.FastBleManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.CoderUtils;
import com.shaoxia.elevator.utils.StringUtils;
import com.shaoxia.elevator.utils.VerifyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorsLogic {
    private static final String TAG = "FloorsLogic";
    private static FloorsLogic mInstance;

    private boolean mHasReceiveData = false;

    private byte[] mReceiveData;
    private int mDataLenth;
    private int mHasReceivelength;

    private MDevice mCurDevice;


    public static FloorsLogic getInstance() {
        if (mInstance == null) {
            mInstance = new FloorsLogic();
        }
        return mInstance;
    }

    public boolean addDevice(List<MDevice> deviceList, BleDevice device) {
        MDevice mDev = new MDevice(device);
        if (!mDev.isElevator()) {
//                        Logger.d(TAG, "run: device is not elevator");
            return false;
        }
        if (mDev.isInCall()) {
            Logger.d(TAG, "run: device is COP elevator");
            return false;
        }
        if (deviceList.contains(mDev)) {
            return false;
        }
        if (mDev.getDevName() == null) {
            Logger.d(TAG, "run: device name is null");
            return false;
        }
        Logger.d(TAG, "run: add device" + mDev.getDevName());
        deviceList.add(mDev);
        return true;
    }

    public void getFloorInfo(int position, List<MDevice> devices, BleGattCallback connectCallBack,
                             final BleNotifyCallback notifyCallback, BleWriteCallback bleWriteCallback) {
        Logger.d(TAG, "getFloorInfo: ");
        if (FastBleManager.getInstance().getState() != FastBleManager.STATE.IDLE) {
            Logger.d(TAG, "getFloorInfo: is comunicating");
            return;
        }
        if (position >= devices.size() || position < 0) {
            Logger.e(TAG, "getFloorInfo: position is illegal");
            return;
        }
        mCurDevice = devices.get(position);
        Logger.d(TAG, "getFloorInfo: " + mCurDevice.getDevice().getName() + "-- " + mCurDevice.getDevice().getAddress());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb4;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0xb4;
//        connect(mCurDevice);
        FastBleManager.getInstance().sendData(cmd, mCurDevice, connectCallBack,
                notifyCallback, bleWriteCallback);
    }


    public boolean onReceiveData(byte[] array, MDevice device) {
        mHasReceiveData = true;
        Logger.d(TAG, "onReceiveData: data:" + StringUtils.ByteArraytoHex(array));

        if (array == null && array.length < 2) {
            Logger.e(TAG, "onReceiveData: return data null or to short error");
            return false;
        }

        if (array[0] == (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head data");
            mDataLenth = getFloorDataLengh(array) + 3;
            mReceiveData = new byte[mDataLenth];
        }

        if (mHasReceivelength >= mDataLenth) {
            Logger.d(TAG, "onReceiveData: over receive data length");
            return false;
        }

        if (array.length + mHasReceivelength > mReceiveData.length) {
            Logger.d(TAG, "onReceiveData: receive data length has over the datalengh");
            return false;
        }

        System.arraycopy(array, 0, mReceiveData, mHasReceivelength, array.length);
        mHasReceivelength += array.length;

        if (mDataLenth == mHasReceivelength) {
//            BleManager.getInstance().disconnect(device);
            FastBleManager.getInstance().disConnect();
            if (!checkData(mReceiveData)) {
                Log.d(TAG, "onReceiveData: checkdata errror");
                return false;
            }
            return parseData(mReceiveData, device);
        }

        return false;
    }

    private int getFloorDataLengh(byte[] array) {
        return array[1] * 4;
    }

    private boolean parseData(byte[] array, MDevice device) {
        Logger.d(TAG, "parseData: ");
        List<String> floors = new ArrayList<>();
        List<Byte> reals = new ArrayList<>();
        Map<String, Byte> floorMap = new HashMap<>();
        byte[] tmp = new byte[3];
        for (int i = 2; i < getFloorDataLengh(array); i += 4) {
            reals.add(array[i]);
            tmp[0] = array[i + 1];
            tmp[1] = array[i + 2];
            tmp[2] = array[i + 3];
            String floor = CoderUtils.asciiToString(tmp).trim();
            Log.d(TAG, "parseData: floor:" + floor);
            floors.add(floor);
            floorMap.put(floor, array[i]);
        }

        if (reals.size() >= 2) {
            if (reals.get(1) > reals.get(0)) {
                Logger.d(TAG, "parseData: reverse floors");
                Collections.reverse(floors);
            }
        }
        device.setFloorsMap(floorMap);
        Logger.d(TAG, "parseData: floors size " + floors.size());
        device.setFloors(floors);
        return true;
    }

    private boolean checkData(byte[] array) {
        if (array == null && array.length < 3) {
            Logger.e(TAG, "onReceiveData: return data null or to short error");
            return false;
        }

        if (array[0] != (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head error");
            return false;
        }

        if ((array.length - 3) != getFloorDataLengh(array)) {
            Logger.e(TAG, "onReceiveData: return data length error");
            return false;
        }

        byte sum = VerifyUtils.getCheckNum(array);
        Logger.d(TAG, "checkData: sum is : " + StringUtils.ByteArraytoHex(new byte[]{sum}));
        if (sum != array[array.length - 1]) {
            Logger.e(TAG, "onReceiveData: checksum error");
            return false;
        }

        return true;
    }

    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        mReceiveData = null;
        mDataLenth = 0;
        mHasReceivelength = 0;
        if (!mHasReceiveData) {
            Logger.d(TAG, "onBleDisconnected: not receive data , reget");
            if (mHander == null) {
                mHander = new Handler();
            }
            mHander.postDelayed(reGetFloorsRunnable, 5000);

        }
    }

    private Handler mHander;
    private Runnable reGetFloorsRunnable = new Runnable() {

        @Override
        public void run() {
//            getFloorInfo(mCurPosition);
        }
    };

    public void onPageChanged() {
        if (mHander != null) {
            mHander.removeCallbacks(reGetFloorsRunnable);
        }
    }

}
