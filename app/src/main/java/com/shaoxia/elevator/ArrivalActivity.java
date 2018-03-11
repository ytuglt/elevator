package com.shaoxia.elevator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.shaoxia.elevator.bluetoothle.BleScanManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;

/**
 * Created by gonglt1 on 18-3-11.
 */

public class ArrivalActivity extends BaseActivity implements BleScanManager.OnStopScanListener {
    private static final String TAG = "ArrivalActivity";
    private BleScanManager mBleScanManager;
    private int mDesPos;

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
                    MDevice mDev = new MDevice(device, rssi);
                    if (!mDev.isElevator()) {
                        Logger.d(TAG, "run: device is not elevator");
                        return;
                    }
                    Logger.d(TAG, "run: add device" + mDev.getDevName());

                    //TODO
//                    if (mDevices.contains(mDev)) {
//                        return;
//                    }
//                    if (mDev.getDevName() == null) {
//                        return;
//                    }
//                    //TODO
//                    mDevices.add(mDev);
//                    updateAdapter();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_arrival);

        initTitleView();

        updateFloorsView();

        mBleScanManager = BleScanManager.getInstance(this);
        mBleScanManager.setOnStopScanListener(this);
        mBleScanManager.setLeScanCallback(mLeScanCallback);
        mBleScanManager.startScan();
    }

    private void initTitleView() {
        mDesPos = getIntent().getIntExtra("despos", -1);
        String id = getIntent().getStringExtra("id");
        String title = id + getResources().getString(R.string.elevator_id_unit) +
                mDesPos + getResources().getString(R.string.floor_unit);
        TextView titleView = findViewById(R.id.elevator_title);
        titleView.setText(title);
    }

    private void updateFloorsView() {
        TextView tvOne = findViewById(R.id.floor_one);
        TextView tvTwo = findViewById(R.id.floor_two);
        TextView tvThree = findViewById(R.id.floor_three);
        TextView tvFour = findViewById(R.id.floor_four);
        TextView tvFive = findViewById(R.id.floor_five);
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

    }
}
