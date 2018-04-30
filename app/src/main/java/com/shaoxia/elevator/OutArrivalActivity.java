package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.shaoxia.elevator.fastble.FastBleManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;

import java.util.List;

/**
 * Created by gonglt1 on 18-3-13.
 */

public class OutArrivalActivity extends BaseArrivalActivity {
    private static final String TAG = "OutArrivalActivity";
    private static final int SCANTIME = 20000;
    private MDevice mCopDevice;

    private FastBleManager mFastBleManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFastBleManager = FastBleManager.getInstance();
        mFastBleManager.init();
    }

    @Override
    protected int getLightPos() {
        return mDevice.getFloors().indexOf(mDevice.getFloor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        startScan();
    }

    private void startScan() {
        FastBleManager.getInstance().startScan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mFastBleManager.setState(FastBleManager.STATE.SCANNING);
            }

            @Override
            public void onScanning(final BleDevice result) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Logger.d(TAG, "run: device name is : " + result.getName());
                        MDevice mDev = new MDevice();
                        if (!mDev.isElevator()) {
                            Logger.d(TAG, "run: device is not elevator");
                            return;
                        }
                        Logger.d(TAG, "run: add device" + mDev.getDevName());

                        if (mDev.isInCall()) {
                            mCopDevice = mDev;
                            mFastBleManager.stopScan();
                            onFindCopDevice();
                        }
                    }
                });
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if (mFastBleManager.getState() == FastBleManager.STATE.SCANNING) {
                    mFastBleManager.setState(FastBleManager.STATE.IDLE);
                    onStopScan();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFastBleManager.stopScan();
        mCopDevice = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
    }

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
