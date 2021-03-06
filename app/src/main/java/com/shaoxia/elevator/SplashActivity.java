package com.shaoxia.elevator;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gonglt1 on 2018/3/7.
 */

public class SplashActivity extends BaseActivity implements BleScanManager.OnStopScanListener,
        BleComManager.OnComListener, ElevatorsAdapter.OnRefreshClickListener, View.OnClickListener {
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final String TAG = "SplashActivity";
    private ExtendViewPager mViewPager;
    private ElevatorsAdapter mAdapter;
    private List<MDevice> mDevices = new ArrayList<>();

    private View mSplashView;

    private BleScanManager mBleScanManager;
    private BleComManager mBleComManager;

    private int mCurPosition = 0;

    private View mScanning;

    private View mLeftChange;
    private View mRightChange;

    /**
     * 发现设备时 处理方法
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
//                    Logger.d(TAG, "run: device name is : " + device.getName());
                    MDevice mDev = new MDevice(device, rssi);
                    if (!mDev.isElevator()) {
//                        Logger.d(TAG, "run: device is not elevator");
                        return;
                    }

                    if (mDev.isInCall()) {
                        Logger.d(TAG, "run: device is COP elevator");
                        return;
                    }

                    //TODO
                    if (mDevices.contains(mDev)) {
                        return;
                    }
                    if (mDev.getDevName() == null) {
                        Logger.d(TAG, "run: device name is null");
                        return;
                    }

                    Logger.d(TAG, "run: add device" + mDev.getDevName());
                    //TODO
                    mScanning.setVisibility(View.GONE);
                    mDevices.add(mDev);
                    updateAdapter();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_splash);
        mSplashView = findViewById(R.id.splash);
        mSplashView.setVisibility(View.VISIBLE);

        mViewPager = findViewById(R.id.elevator_viewpager);
        mAdapter = new ElevatorsAdapter(this, mDevices);
        mAdapter.setOnRefreshClickListener(this);
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

        mLeftChange = findViewById(R.id.left_change);
        mRightChange = findViewById(R.id.right_change);
        mLeftChange.setOnClickListener(this);
        mRightChange.setOnClickListener(this);

        mScanning = findViewById(R.id.scanning);
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
//            onStopScan();
            bluetoothAdapter.enable();
//            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                startScan();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        clearDevices();
        mBleScanManager = BleScanManager.getInstance(this);
        mBleScanManager.setOnStopScanListener(this);
        mBleScanManager.setLeScanCallback(mLeScanCallback);

        mBleComManager = BleComManager.getInstance(this);
        mBleComManager.setOnComListener(this);

//        startScan();
        checkPermissions();
    }

    private void clearDevices() {
        mCurPosition = 0;
        mDevices.clear();
        updateAdapter();
        if (mSplashView.getVisibility() != View.VISIBLE) {
            mScanning.setVisibility(View.VISIBLE);
            mLeftChange.setVisibility(View.GONE);
            mRightChange.setVisibility(View.GONE);
        }
    }

    private boolean mIsSanning = false;

    private void startScan() {
        if (mBleScanManager != null) {
            mIsSanning = true;
            if (mViewPager != null) {
                Logger.d(TAG, "startScan:setPagingEnabled false ");
                mViewPager.setPagingEnabled(false);
            }
            Logger.d(TAG, "startScan: ");
            mBleScanManager.startScan();
        }
    }

    private void stopScan() {
        if (mBleScanManager != null) {
            Logger.d(TAG, "stopScan: ");
            mBleScanManager.stopScan();
            mIsSanning = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause: ");
        stopScan();
        mIsComunicating = false;
        if (mBleComManager != null) {
            mBleComManager.disconnect();
        }
        if (mBleComManager != null) {
            mBleComManager.destroy();
        }
    }

    @Override
    public void onStopScan() {
        Logger.d(TAG, "onStopScan: ");
        mIsSanning = false;
        if (mDevices.size() <= 0) {
            mScanning.setVisibility(View.GONE);
            Toast.makeText(this, "未发现设备", Toast.LENGTH_SHORT).show();
            MDevice device = new MDevice();
            mDevices.add(device);
            updateAdapter();
            mViewPager.setCurrentItem(0);
            mLeftChange.setVisibility(View.GONE);
            mRightChange.setVisibility(View.GONE);
        } else {
            mCurPosition = 0;
            mViewPager.setCurrentItem(0);
            getFloorInfo(0);
            mLeftChange.setVisibility(View.GONE);
            if (mDevices.size() >= 2) {
                mRightChange.setVisibility(View.VISIBLE);
            } else {
                mRightChange.setVisibility(View.GONE);
            }
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
            Logger.d(TAG, "updateAdapter: mDevices size " + mDevices.size() + ";adapter count " + mAdapter.getCount());
            mAdapter.reloadData(mDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        mIsComunicating = false;
        if (mBleScanManager != null) {
            mBleScanManager.destroy();
        }
//        if (mBleComManager != null) {
//            mBleComManager.destroy();
//        }
    }

    private boolean mHasReceiveData = false;

    private void getFloorInfo(int position) {
        Logger.d(TAG, "getFloorInfo: ");
        if (mIsComunicating) {
            Logger.d(TAG, "getFloorInfo: is comunicating");
            return;
        }
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
        mHasReceiveData = false;
    }

    private boolean mIsComunicating;

    @Override
    public void onCommunicating() {
        Logger.d(TAG, "onCommunicating: ");
        mIsComunicating = true;
        if (mViewPager != null) {
            Logger.d(TAG, "onCommunicating:setPagingEnabled false ");
            mViewPager.setPagingEnabled(false);
        }

        setViewState(MDevice.COMUNICATING);
    }

    private void setViewState(int state) {
        ElevatorsAdapter.ViewHolder viewHolder = mAdapter.getHolder(mCurPosition);
        if (viewHolder != null) {
            viewHolder.setState(state);
        }
    }

    @Override
    public void onWriteSuccess() {
        Logger.d(TAG, "onWriteSuccess: ");
    }

    @Override
    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        mIsComunicating = false;
        mReceiveData = null;
        mDataLenth = 0;
        mHasReceivelength = 0;

        setViewState(MDevice.IDLE);

//        if (mViewPager != null) {
//            Logger.d(TAG, "onBleDisconnected:setPagingEnabled true ");
//            mViewPager.setPagingEnabled(true);
//        }
//        Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
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
            getFloorInfo(mCurPosition);
        }
    };


    @Override
    public void onConnectFailed() {
        Logger.d(TAG, "onConnectFailed: ");
        mIsComunicating = false;
    }

    private byte[] mReceiveData;
    private int mDataLenth;
    private int mHasReceivelength;

    @Override
    public void onReceiveData(byte[] array) {
        mHasReceiveData = true;
        Logger.d(TAG, "onReceiveData: data:" + StringUtils.ByteArraytoHex(array));

        if (array == null && array.length < 2) {
            Logger.e(TAG, "onReceiveData: return data null or to short error");
            return;
        }

        if (array[0] == (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head data");
            mDataLenth = getFloorDataLengh(array) + 3;
            mReceiveData = new byte[mDataLenth];
        }

        if (mHasReceivelength >= mDataLenth) {
            Logger.d(TAG, "onReceiveData: over receive data length");
            return;
        }

        if (array.length + mHasReceivelength > mReceiveData.length) {
            Logger.d(TAG, "onReceiveData: receive data length has over the datalengh");
            return;
        }

        System.arraycopy(array, 0, mReceiveData, mHasReceivelength, array.length);
        mHasReceivelength += array.length;

        if (mDataLenth == mHasReceivelength) {
            mBleComManager.disconnect();
            if (!checkData(mReceiveData)) {
                Log.d(TAG, "onReceiveData: checkdata errror");
                return;
            }
            parseData(mReceiveData);
        }
    }

    private int getFloorDataLengh(byte[] array) {
        return array[1] * 4;
    }

    private void parseData(byte[] array) {
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
        mDevices.get(mCurPosition).setFloorsMap(floorMap);
        Logger.d(TAG, "parseData: floors size " + floors.size());
        mDevices.get(mCurPosition).setFloors(floors);
        ElevatorsAdapter.ViewHolder viewHolder = mAdapter.getHolder(mCurPosition);
        if (viewHolder != null) {
            viewHolder.updateWheelData();
        }
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

    private void onPageChanged(int position) {
        Logger.d(TAG, "onPageChanged: ");
        if (mCurPosition == position) {
            Logger.d(TAG, "onPageChanged: same position");
            return;
        }
        if (mHander != null) {
            mHander.removeCallbacks(reGetFloorsRunnable);
        }
        mCurPosition = position;

        if (mCurPosition == 0) {
            mLeftChange.setVisibility(View.GONE);
        } else {
            mLeftChange.setVisibility(View.VISIBLE);
        }

        if (mCurPosition == mDevices.size() - 1) {
            mRightChange.setVisibility(View.GONE);
        } else {
            mRightChange.setVisibility(View.VISIBLE);
        }
        getFloorInfo(position);
    }

    private boolean isFirstBack = true;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isFirstBack) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                isFirstBack = false;
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRerefsh() {
        Logger.d(TAG, "onRerefsh: ");
        if (mIsSanning) {
            Logger.d(TAG, "onRerefsh: is mIsSanning ");
            Toast.makeText(this, "Scanning,please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsComunicating) {
            Logger.d(TAG, "onRerefsh: is communicating ");
            Toast.makeText(this, "Communicating,please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBleComManager != null) {
            mBleComManager.disconnect();
        }

        stopScan();
        clearDevices();
        startScan();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_change:
                if (mIsComunicating) {
                    Toast.makeText(SplashActivity.this, "Communicating,please wait...", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mViewPager != null && mCurPosition >= 1) {
                    mViewPager.setCurrentItem(mCurPosition - 1);
                }
                break;
            case R.id.right_change:
                if (mIsComunicating) {
                    Toast.makeText(SplashActivity.this, "Communicating,please wait...", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mViewPager != null && mDevices != null && mCurPosition < (mDevices.size() - 1)) {
                    mViewPager.setCurrentItem(mCurPosition + 1);
                }
                break;
        }
    }
}
