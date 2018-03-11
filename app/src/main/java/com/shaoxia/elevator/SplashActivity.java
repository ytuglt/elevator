package com.shaoxia.elevator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shaoxia.elevator.bluetoothle.BleComManager;
import com.shaoxia.elevator.bluetoothle.BleScanManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.CoderUtils;
import com.shaoxia.elevator.utils.StringUtils;
import com.shaoxia.elevator.utils.VerifyUtils;
import com.shaoxia.elevator.widget.ExtendViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 2018/3/7.
 */

public class SplashActivity extends BaseActivity implements BleScanManager.OnStopScanListener,
        BleComManager.OnComListener {
    private static final String TAG = "SplashActivity";
    private ExtendViewPager mViewPager;
    private ElevatorsAdapter mAdapter;
    private List<MDevice> mDevices = new ArrayList<>();

    private View mSplashView;

    private BleScanManager mBleScanManager;
    private BleComManager mBleComManager;

    private int mCurPosition = 0;

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
                    Logger.d(TAG, "run: add device" + mDev.getDevice().getName());

                    //TODO
                    if (mDevices.contains(mDev)) {
                        return;
                    }
                    if (mDev.getDevName() == null) {
                        return;
                    }
                    //TODO
                    mDevices.add(mDev);
                    updateAdapter();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSplashView = findViewById(R.id.splash);
        mSplashView.setVisibility(View.VISIBLE);

        mViewPager = findViewById(R.id.elevator_viewpager);
        mAdapter = new ElevatorsAdapter(this, mDevices);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                onPageChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mBleScanManager = BleScanManager.getInstance(this);
        mBleScanManager.setOnStopScanListener(this);
        mBleScanManager.setLeScanCallback(mLeScanCallback);
        mBleScanManager.startScan();

        mBleComManager = BleComManager.getInstance(this);
        mBleComManager.setOnComListener(this);
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
        if (mBleScanManager != null) {
            mBleScanManager.stopScan();
        }
    }

    @Override
    public void onStopScan() {
        Logger.d(TAG, "onStopScan: ");
        if (mDevices.size() <= 0) {
            Toast.makeText(this, "未发现设备", Toast.LENGTH_SHORT).show();
            MDevice device = new MDevice();
            mDevices.add(device);
            updateAdapter();
        } else {
            mCurPosition = 0;
            mViewPager.setCurrentItem(0);
            getFloorInfo(0);
        }
        if (mSplashView != null) {
            Logger.d(TAG, "onStopScan: mSplashView start gone");
            mSplashView.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);
        }
    }

    private void updateAdapter() {
        Log.d(TAG, "updateAdapter: ");
        if (mAdapter != null) {
            Logger.d(TAG, "onStopScan: mDevices size " + mDevices.size() + ";adapter count " + mAdapter.getCount());
            mAdapter.updateList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        if (mBleScanManager != null) {
            mBleScanManager.destroy();
        }
        if (mBleComManager != null) {
            mBleComManager.destroy();
        }
    }

    private void getFloorInfo(int position) {
        if (position >= mDevices.size() || position < 0) {
            Logger.e(TAG, "getFloorInfo: position is illegal");
            return;
        }
        MDevice device = mDevices.get(position);
        mBleComManager.setDevAddress(device.getDevAddress());
        mBleComManager.setDevName(device.getDevName());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb4;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0xb4;
        mBleComManager.sendData(cmd);
    }

    @Override
    public void onCommunicating() {
        if (mViewPager != null) {
            mViewPager.setPagingEnabled(false);
        }

        ElevatorView elevatorView = mAdapter.getItem(mCurPosition);
        elevatorView.setState(MDevice.State.COMUNICATING);
    }

    @Override
    public void onWriteSuccess() {
        Logger.d(TAG, "onWriteSuccess: ");
    }

    @Override
    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        ElevatorView elevatorView = mAdapter.getItem(mCurPosition);
        elevatorView.setState(MDevice.State.IDLE);

        if (mViewPager != null) {
            mViewPager.setPagingEnabled(true);
        }
        Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveData(byte[] array) {
        Logger.d(TAG, "onReceiveData: data:" + StringUtils.ByteArraytoHex(array));
        mBleComManager.disconnect();
        if (!checkData(array)) {
            Log.d(TAG, "onReceiveData: checkdata errror");
            return;
        }
        parseData(array);

    }

    private void parseData(byte[] array) {
        Logger.d(TAG, "parseData: ");
        List<String> floors = new ArrayList<>();
        byte[] tmp = new byte[3];
        for (int i = 2; i < array[1]; i += 4) {
            tmp[0] = array[i + 1];
            tmp[1] = array[i + 2];
            tmp[2] = array[i + 3];
            String floor = CoderUtils.asciiToString(tmp).trim();
            Log.d(TAG, "parseData: floor:" + floor);
            floors.add(floor);
        }
        Logger.d(TAG, "parseData: floors size " + floors.size());
        mDevices.get(mCurPosition).setFloors(floors);
        ElevatorView elevatorView = mAdapter.getItem(mCurPosition);
        elevatorView.updateWheelData();
        mAdapter.notifyDataSetChanged();
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

        if ((array.length - 3) != array[1]) {
            Logger.e(TAG, "onReceiveData: return data length error");
            return false;
        }

        byte sum = VerifyUtils.getCheckNum(array);
        if (sum != array[array.length - 1]) {
            Logger.e(TAG, "onReceiveData: checksum error");
            return false;
        }

        return true;
    }

    private void onPageChanged(int position) {
        if (mCurPosition == position) {
            Logger.d(TAG, "onPageChanged: same position");
            return;
        }
    }

}
