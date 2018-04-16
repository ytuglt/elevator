package com.shaoxia.elevator.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.utils.Configure;

/**
 * Created by gonglt1 on 2018/3/10.
 */

public class BleScanManager {
    private static final String TAG = "BleScanManager";
    private static BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;
    private BleManger mBleManger;

    private static BleScanManager mInstance;

    private OnStopScanListener mOnStopScanListener;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private int mScanTime = Configure.DEFAULT_SCAN_TIME;

    //停止扫描
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "stopScanRunnable run: ");
            stopScan();
            if (mOnStopScanListener != null) {
                Logger.d(TAG, "run: mOnStopScanListener call");
                mOnStopScanListener.onStopScan();
            } else {
                Logger.d(TAG, "run: mOnStopScanListener is null");
            }
        }
    };

    public BleScanManager(Context context) {
        Logger.d(TAG, "BleScanManager: ");
        mHandler = new Handler();
        mBleManger = BleManger.getInstance();
        mBluetoothAdapter = BleHelper.checkBleSupportAndInitialize(context);
        BleHelper.initService(context);
    }

    public static BleScanManager getInstance(Context context) {
        Logger.d(TAG, "getInstance: ");
        if (mInstance == null) {
            Logger.d(TAG, "getInstance: create instance");
            mInstance = new BleScanManager(context);
        }

        return mInstance;
    }

    /**
     * 开始扫面入口
     */
    public void startScan() {
        Logger.d(TAG, "startScan: ");
        if (mBleManger != null) {
            if (mBleManger.isScanning()) {
                Logger.d(TAG, "startScan: mBleManger is scanning");
                stopScan();
            }
        }

        if (!BleHelper.isDisconnected()) {
            Logger.d(TAG, "startScan: ble is not disconnected");
            BleHelper.disconnectDevice();
        }
//        BleHelper.disconnectDevice();
        BleManger.getInstance().setBleState(BleManger.State.SCANNING);
        scanPrevious21Version();
    }

    /**
     * 版本号21之前的调用该方法搜索
     */
    private void scanPrevious21Version() {
        Logger.d(TAG, "scanPrevious21Version: ");
        if (mLeScanCallback == null) {
            Logger.d(TAG, "scanPrevious21Version: mLeScanCallback is null return");
            return;
        }
        //10秒后停止扫描
        mHandler.postDelayed(stopScanRunnable, mScanTime);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    /**
     * ble 停止扫描
     */
    public void stopScan() {
        Logger.d(TAG, "stopScan: ");
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(stopScanRunnable);
        }
        if (mBleManger != null) {
            mBleManger.setBleState(BleManger.State.IDLE);
        }
    }

    public void setScanTime(int time) {
        mScanTime = time;
    }

    public interface OnStopScanListener {
        void onStopScan();
    }

    public void setOnStopScanListener(OnStopScanListener listener) {
        mOnStopScanListener = listener;
    }

    public void setLeScanCallback(BluetoothAdapter.LeScanCallback leScanCallback) {
        this.mLeScanCallback = leScanCallback;
    }

    public void destroy() {
        mInstance = null;
        mHandler = null;
        mBleManger = null;
        mBluetoothAdapter = null;
    }
}
