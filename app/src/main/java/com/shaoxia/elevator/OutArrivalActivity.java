package com.shaoxia.elevator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.shaoxia.elevator.bluetoothle.BleScanManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class OutArrivalActivity extends BaseArrivalActivity implements BleScanManager.OnStopScanListener {
    private static final String TAG = "OutArrivalActivity";
    private static final int SCANTIME = 20000;

    private BleScanManager mBleScanManager;

    private MDevice mCopDevice;

    /**
     * 发现设备时 处理方法
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Logger.d(TAG, "run: device name is : " + device.getName());
                    MDevice mDev = new MDevice();
                    if (!mDev.isElevator()) {
                        Logger.d(TAG, "run: device is not elevator");
                        return;
                    }
                    Logger.d(TAG, "run: add device" + mDev.getDevName());

                    if (mDev.isInCall()) {
                        mCopDevice = mDev;
                        mBleScanManager.stopScan();
                        onFindCopDevice();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBleScanManager = BleScanManager.getInstance(this);
        mBleScanManager.setOnStopScanListener(this);
        mBleScanManager.setLeScanCallback(mLeScanCallback);
        mBleScanManager.setScanTime(SCANTIME);

    }

    @Override
    protected int getLightPos() {
        return mDevice.getFloors().indexOf(mDevice.getFloor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        mBleScanManager.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBleScanManager != null) {
            mBleScanManager.stopScan();
        }
        mCopDevice = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        if (mBleScanManager != null) {
            mBleScanManager.destroy();
        }
    }

    @Override
    public void onStopScan() {
        if (mCopDevice == null) {
            Logger.d(TAG, "onStopScan: No Cop Device Find, finish...");
            Toast.makeText(this, "No Cop Device Find", Toast.LENGTH_SHORT);
            finish();
        }
    }

    private void onFindCopDevice() {
        Logger.d(TAG, "onFindCopDevice: ");
        Intent intent = new Intent(this, InWaitingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(InWaitingActivity.COP_DEVICE_KEY, mCopDevice);//序列化
        bundle.putParcelable("device", mDevice);//序列化
        intent.putExtras(bundle);//发送数据
        intent.putExtra("title", mTitleView.getText().toString());
        intent.putExtra("isUp", mIsUp);
        intent.putExtra("despos", mDesPos);
        intent.putExtra(InWaitingActivity.ENTER_FROM_KEY, InWaitingActivity.ENTER_FORM_OUTARRIVAL);
        startActivity(intent);
        finish();
    }
}
