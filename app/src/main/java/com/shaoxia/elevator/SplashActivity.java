package com.shaoxia.elevator;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.shaoxia.elevator.fastble.FastBleManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.logic.FloorsLogic;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.PermissionUtils;
import com.shaoxia.elevator.widget.ExtendViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 2018/3/7.
 */

public class SplashActivity extends BaseActivity implements ElevatorsAdapter.OnRefreshClickListener, View.OnClickListener
        , PermissionUtils.PermissionCheckCallback {

    private static final String TAG = "SplashActivity";
    private ExtendViewPager mViewPager;
    private ElevatorsAdapter mAdapter;
    private List<MDevice> mDevices = new ArrayList<>();

    private View mSplashView;
    private int mCurPosition = 0;
    private View mScanning;
    private View mLeftChange;
    private View mRightChange;

    private FastBleManager mFastBleManager;

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
        mFastBleManager = FastBleManager.getInstance();
        mFastBleManager.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        clearDevices();
        PermissionUtils.checkPermissions(this, this);
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

    private void startScan() {
        Logger.d(TAG, "startScan: ");
        FastBleManager.getInstance().startScan(new BleScanCallback() {

            @Override
            public void onScanStarted(boolean success) {
                mFastBleManager.setState(FastBleManager.STATE.SCANNING);
                if (mViewPager != null) {
                    Logger.d(TAG, "startScan:setPagingEnabled false ");
                    mViewPager.setPagingEnabled(false);
                }
            }

            @Override
            public void onScanning(BleDevice result) {
                if (FloorsLogic.getInstance().addDevice(mDevices, result)) {
                    mScanning.setVisibility(View.GONE);
                    updateAdapter();
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Logger.d(TAG, "onScanFinished: ");
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
        Logger.d(TAG, "onPause: ");
        mFastBleManager.stopScan();
        mFastBleManager.disConnect();
        mFastBleManager.setState(FastBleManager.STATE.IDLE);
    }

    public void onStopScan() {
        Logger.d(TAG, "onStopScan: ");
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

    private void getFloorInfo(int position) {
        FloorsLogic.getInstance().getFloorInfo(position, mDevices, new BleGattCallback() {

                    @Override
                    public void onStartConnect() {
                        onCommunicating();
                    }

                    @Override
                    public void onConnectFail(BleException exception) {
                        onConnectFailed();

                    }

                    @Override
                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                    }

                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                        onBleDisconnected();
                    }
                },
                new BleNotifyCallback() {
                    //
                    @Override
                    public void onNotifySuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Logger.d(TAG, "onNotifySuccess: notify success");
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Logger.d(TAG, "onNotifyFailure: " + exception.toString());
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Logger.d(TAG, "onCharacteristicChanged: " + HexUtil.formatHexString(data));
                                if (FloorsLogic.getInstance().onReceiveData(
                                        data,
                                        mDevices.get(mCurPosition))) {
                                    updateWheel();
                                }
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        mIsComunicating = false;
        mFastBleManager.destroy();
    }

    private void updateWheel() {
        ElevatorsAdapter.ViewHolder viewHolder = mAdapter.getHolder(mCurPosition);
        if (viewHolder != null) {
            viewHolder.updateWheelData();
        }
        mAdapter.notifyDataSetChanged();
    }


    private boolean mIsComunicating;

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

    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        mIsComunicating = false;
        setViewState(MDevice.IDLE);
    }

    public void onConnectFailed() {
        Logger.d(TAG, "onConnectFailed: ");
        mIsComunicating = false;
    }

    private void onPageChanged(int position) {
        Logger.d(TAG, "onPageChanged: ");
        if (mCurPosition == position) {
            Logger.d(TAG, "onPageChanged: same position");
            return;
        }
        FloorsLogic.getInstance().onPageChanged();
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
        if (mFastBleManager.getState() == FastBleManager.STATE.SCANNING) {
            Logger.d(TAG, "onRerefsh: is mIsSanning ");
            Toast.makeText(this, "Scanning,please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsComunicating) {
            Logger.d(TAG, "onRerefsh: is communicating ");
            Toast.makeText(this, "Communicating,please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        mFastBleManager.stopScan();
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

    @Override
    public void onGranted() {
        startScan();
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            PermissionUtils.onPermissionGranted(this, permissions[i], this);
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtils.REQUEST_CODE_OPEN_GPS) {
            if (PermissionUtils.checkGPSIsOpen(this)) {
                startScan();
            }
        }
    }
}
