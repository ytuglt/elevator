package com.shaoxia.elevator;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shaoxia.elevator.bluetoothle.BleComManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.logic.LightFloorLogic;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-9.
 */

public abstract class BaseWaitingActivity extends BaseActivity implements BleComManager.OnComListener {
    private static final String TAG = "BaseWaitingActivity";

    public static final String DEVICE_KEY = "device";

    private static final int QUERY_INTERVAL = 100;

    protected MDevice mDevice;
    protected boolean mIsUp;
    protected int mDesPos;
    protected BleComManager mBleComManager;
    private Handler mHandler;

    protected boolean mIsComunicating;

    protected TextView mTitleView;

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

        mBleComManager = BleComManager.getInstance(this);
        mBleComManager.setOnComListener(this);
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
        mBleComManager = BleComManager.getInstance(this);
        mBleComManager.setOnComListener(this);
        getStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause: ");
        if (mBleComManager != null) {
            mIsComunicating = false;
            mBleComManager.disconnect();
            mBleComManager.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        mIsComunicating = false;
//        if (mBleComManager != null) {
//            mBleComManager.destroy();
//        }
    }

    protected abstract void call();

    protected abstract void getStatus();

    @Override
    public void onCommunicating() {
        Logger.d(TAG, "onCommunicating: ");
        mIsComunicating = true;
    }

    @Override
    public void onWriteSuccess() {
        Logger.d(TAG, "onWriteSuccess: ");
    }

    @Override
    public void onBleDisconnected() {
        Logger.d(TAG, "onBleDisconnected: ");
        mIsComunicating = false;
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

    @Override
    public void onConnectFailed() {
        Logger.d(TAG, "onConnectFailed: ");
    }

    @Override
    public void onReceiveData(byte[] array) {
        Logger.d(TAG, "onReceiveData: " + StringUtils.ByteArraytoHex(array));
        if (mBleComManager != null) {
            mBleComManager.disconnect();
        }
        if (!checkData(array)) {
            Log.d(TAG, "onReceiveData: checkdata errror");
            return;
        }
        parseData(array);
    }

    protected abstract boolean checkData(byte[] array);

    protected abstract void parseData(byte[] array);
}
