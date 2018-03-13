package com.shaoxia.elevator;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.shaoxia.elevator.bluetoothle.BleComManager;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.StringUtils;
import com.shaoxia.elevator.utils.VerifyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-9.
 */

public abstract class BaseWaitingActivity extends BaseActivity implements BleComManager.OnComListener {
    private static final String TAG = "BaseWaitingActivity";

    private static final int QUERY_INTERVAL = 5000;

    protected MDevice mDevice;
    protected boolean mIsUp;
    protected int mDesPos;
    protected BleComManager mBleComManager;

    private TextView mTvOne;
    private TextView mTvTwo;
    private TextView mTvThree;
    private TextView mTvFour;

    private Handler mHandler;

    protected boolean mIsComunicating;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_out);

        initIntentData();

        String title = getIntent().getStringExtra("title");
        TextView titleView = findViewById(R.id.elevator_title);
        titleView.setText(title);

        getUpOrDown();

        updateAnimView();

        initFloosView();
        updateFloors();

        mBleComManager = BleComManager.getInstance(this);
        mBleComManager.setOnComListener(this);
        mHandler = new Handler();

        call();
    }

    private void initIntentData() {
        mDevice = (MDevice) getIntent().getSerializableExtra("device");
        mDesPos = getIntent().getIntExtra("despos", -1);
    }

    protected abstract void getUpOrDown();

    private void updateAnimView() {
        ImageView imageView = findViewById(R.id.img_up);

        if (mIsUp) {
            imageView.setImageResource(R.drawable.up_anim);
        } else {
            imageView.setImageResource(R.drawable.down_anim);
        }

        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        animationDrawable.start();
    }

    private void initFloosView() {
        mTvOne = findViewById(R.id.floor_one);
        mTvTwo = findViewById(R.id.floor_two);
        mTvThree = findViewById(R.id.floor_three);
        mTvFour = findViewById(R.id.floor_four);
    }

    private void updateFloors() {
        List<String> floors = getFloors();
        mTvOne.setText(floors.get(0));
        mTvTwo.setText(floors.get(1));
        mTvThree.setText(floors.get(2));
        mTvFour.setText(floors.get(3));

        int pos = floors.indexOf(mDevice.getFloor());
        switch (pos) {
            case 0:
                lightCurFloor(mTvOne);
                break;
            case 1:
                lightCurFloor(mTvTwo);
                break;
            case 2:
                lightCurFloor(mTvThree);
                break;
            case 3:
                lightCurFloor(mTvFour);
                break;
        }
    }

    private void lightCurFloor(TextView textView) {
        textView.setBackgroundResource(R.drawable.floor_back_sel);
        textView.setTextColor(getResources().getColor(R.color.floor_light, null));
        textView.setShadowLayer(40, 0, 0, getResources().getColor(R.color.floor_light, null));
    }

    private List<String> getFloors() {
        List<String> floors = new ArrayList<>();
        List<String> allFloors = mDevice.getFloors();
        int curpos = getLightPos();

        if (mIsUp) {
            if (curpos - 3 >= 0) {
                for (int i = curpos - 3; i <= curpos; i++) {
                    floors.add(allFloors.get(i));
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    floors.add(allFloors.get(i));
                }
            }
        } else {
            int size = allFloors.size();
            if (curpos + 3 < size) {
                for (int i = curpos; i < size; i++) {
                    floors.add(allFloors.get(i));
                }
            } else {
                for (int i = size - 4; i < size; i++) {
                    floors.add(allFloors.get(i));
                }
            }
        }

        return floors;
    }

    protected abstract int getLightPos();

    @Override
    protected void onResume() {
        super.onResume();
        getStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy: ");
        mIsComunicating = false;
        if (mBleComManager != null) {
            mBleComManager.destroy();
        }
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getStatus();
            }
        }, QUERY_INTERVAL);
    }

    @Override
    public void onReceiveData(byte[] array) {
        Logger.d(TAG, "onReceiveData: " + StringUtils.ByteArraytoHex(array));

        if (!checkData(array)) {
            Log.d(TAG, "onReceiveData: checkdata errror");
            return;
        }
        parseData(array);
    }

    protected abstract boolean checkData(byte[] array);

    protected abstract void parseData(byte[] array);
}
