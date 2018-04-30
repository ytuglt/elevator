package com.shaoxia.elevator;

import android.bluetooth.BluetoothGatt;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.shaoxia.elevator.bluetoothle.BleComManager;
import com.shaoxia.elevator.fastble.FastBleManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.logic.FloorsLogic;
import com.shaoxia.elevator.logic.LightFloorLogic;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-9.
 */

public abstract class BaseWaitingActivity extends BaseActivity {
    private static final String TAG = "BaseWaitingActivity";

    public static final String DEVICE_KEY = "device";

    private static final int QUERY_INTERVAL = 100;

    protected MDevice mDevice;
    protected boolean mIsUp;
    protected int mDesPos;
    private Handler mHandler;
    protected TextView mTitleView;

    private FastBleManager mFastBleManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_call_out);

        initIntentData();

        getUpOrDown();

        mTitleView = findViewById(R.id.elevator_title);
        initTitleView();

        updateAnimView();

        LightFloorLogic.updateFloorsView(this, mDevice, getLightPos());

        mFastBleManager = FastBleManager.getInstance();
        mFastBleManager.init();

        mHandler = new Handler();

        call();
    }

    protected void initTitleView() {
        String title = getIntent().getStringExtra("title");
        mTitleView.setText(title);
    }

    protected void initIntentData() {
        mDevice = (MDevice) getIntent().getParcelableExtra("device");
        Logger.d(TAG, "initIntentData: mDevice is " + mDevice);
        mDesPos = getIntent().getIntExtra("despos", -1);
    }

    protected abstract void getUpOrDown();

    protected void updateAnimView() {
        if (mIsUp) {
            startAnimView(R.drawable.up_anim);
        } else {
            startAnimView(R.drawable.down_anim);
        }
    }

    protected void startAnimView(@DrawableRes int resId) {
        ImageView imageView = findViewById(R.id.img_up);
        imageView.setImageResource(resId);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        animationDrawable.start();
    }

    protected abstract int getLightPos();

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        getStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause: ");
//        mFastBleManager.disConnect();
//        mFastBleManager.close();
        mFastBleManager.setState(FastBleManager.STATE.IDLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
    }

    protected void call(){
        sendData(getCallData());
    }

    protected void getStatus(){
        sendData(getQueryData());
    }

    private void sendData(byte[] cmd) {
        if (mFastBleManager.getState() != FastBleManager.STATE.IDLE) {
            Logger.d(TAG, "sendData: not idle");
            return;
        }
        FastBleManager.getInstance().sendData(cmd, getComDevice(), new BleGattCallback() {

            @Override
            public void onStartConnect() {
                mFastBleManager.setState(FastBleManager.STATE.CONNECTING);
                onCommunicating();
            }

            @Override
            public void onConnectFail(BleException exception) {
                onConnectFailed();
                mFastBleManager.setState(FastBleManager.STATE.IDLE);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                onBleDisconnected();
                mFastBleManager.setState(FastBleManager.STATE.IDLE);
            }
        }, new BleNotifyCallback() {

            @Override
            public void onNotifySuccess() {

            }

            @Override
            public void onNotifyFailure(BleException exception) {

            }

            @Override
            public void onCharacteristicChanged(byte[] data) {
                onReceiveData(data);
            }
        }, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {

            }

            @Override
            public void onWriteFailure(BleException exception) {

            }
        });
    }

    protected abstract byte[] getCallData();

    protected abstract byte[] getQueryData();

    protected abstract MDevice getComDevice();

    public void onCommunicating() {
        Logger.d(TAG, "onCommunicating: ");
    }

    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        if (this.isFinishing()) {
            Logger.d(TAG, "onBleDisconnected: isFinishing");
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getStatus();
            }
        }, QUERY_INTERVAL);
    }

    public void onConnectFailed() {
        Logger.d(TAG, "onConnectFailed: ");
    }

    public void onReceiveData(byte[] array) {
        Logger.d(TAG, "onReceiveData: " + StringUtils.ByteArraytoHex(array));
        mFastBleManager.disConnect();
        if (!checkData(array)) {
            Log.d(TAG, "onReceiveData: checkdata errror");
            return;
        }
        parseData(array);
    }

    protected abstract boolean checkData(byte[] array);

    protected abstract void parseData(byte[] array);
}
