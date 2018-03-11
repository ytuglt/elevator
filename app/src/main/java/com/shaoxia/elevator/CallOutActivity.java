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
import com.shaoxia.elevator.utils.CoderUtils;
import com.shaoxia.elevator.utils.VerifyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-9.
 */

public class CallOutActivity extends BaseActivity implements BleComManager.OnComListener {
    private static final String TAG = "CallOutActivity";

    private MDevice mDevice;
    private boolean mIsUp;

    private TextView mTvOne;
    private TextView mTvTwo;
    private TextView mTvThree;
    private TextView mTvFour;

    private BleComManager mBleComManager;
    private Handler mHandler;

    private boolean mIsComunicating;

    private int mDesPos;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_out);

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

    private void getUpOrDown() {
        mDevice = (MDevice) getIntent().getSerializableExtra("device");
        mDesPos = getIntent().getIntExtra("despos", -1);


        if (mDevice == null) {
            Log.e(TAG, "getUpOrDown: mDevice is null");
            return;
        }
        List<String> floors = mDevice.getFloors();
        if (floors == null) {
            Log.e(TAG, "getUpOrDown: floors is null");
            return;
        }
        int curpos = floors.indexOf(mDevice.getFloor());
        mIsUp = (mDesPos < curpos);
    }

    private void updateAnimView() {
        ImageView imageView = findViewById(R.id.img_up);

        if (mIsUp) {
            imageView.setImageResource(R.drawable.up_anim);
        } else {
            imageView.setImageResource(R.drawable.up_anim);
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
        int curpos = mDevice.getFloors().indexOf(mDevice.getFloor());
        if (mIsUp) {
            if (curpos - 3 >= 0) {
                mTvOne.setText(mDevice.getFloors().get(curpos - 3));
                mTvTwo.setText(mDevice.getFloors().get(curpos - 2));
                mTvThree.setText(mDevice.getFloors().get(curpos - 1));
                mTvFour.setText(mDevice.getFloors().get(curpos));
            } else {
                switch (curpos) {
                    case 0:
                        mTvOne.setText(mDevice.getFloors().get(curpos));
                        mTvTwo.setText(mDevice.getFloors().get(curpos + 1));
                        mTvThree.setText(mDevice.getFloors().get(curpos + 2));
                        mTvFour.setText(mDevice.getFloors().get(curpos + 3));
                        break;
                    case 1:
                        mTvOne.setText(mDevice.getFloors().get(curpos - 1));
                        mTvTwo.setText(mDevice.getFloors().get(curpos));
                        mTvThree.setText(mDevice.getFloors().get(curpos + 1));
                        mTvFour.setText(mDevice.getFloors().get(curpos + 2));
                        break;
                    case 2:
                        mTvOne.setText(mDevice.getFloors().get(curpos - 2));
                        mTvTwo.setText(mDevice.getFloors().get(curpos - 1));
                        mTvThree.setText(mDevice.getFloors().get(curpos));
                        mTvFour.setText(mDevice.getFloors().get(curpos + 1));
                        break;
                }
            }
        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsComunicating = false;
        if (mBleComManager != null) {
            mBleComManager.destroy();
        }
    }

    private void call() {
        mBleComManager.setDevAddress(mDevice.getDevAddress());
        mBleComManager.setDevName(mDevice.getDevName());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        if (mIsUp) {
            cmd[1] = (byte) 0x01;
            cmd[2] = (byte) 0xb6;
        } else {
            cmd[1] = (byte) 0x02;
            cmd[2] = (byte) 0xb7;
        }

        mBleComManager.sendData(cmd);
    }

    private void getStatus() {
        if (mIsComunicating) {
            Logger.d(TAG, "getStatus: is comunicating");
            return;
        }
        mBleComManager.setDevAddress(mDevice.getDevAddress());
        mBleComManager.setDevName(mDevice.getDevName());
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) 0xb5;
        mBleComManager.sendData(cmd);
    }

    @Override
    public void onCommunicating() {
        mIsComunicating = true;
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onBleDisconnected() {
        mIsComunicating = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getStatus();
            }
        }, 5000);
    }

    @Override
    public void onReceiveData(byte[] array) {
        if (!checkData(array)) {
            Log.d(TAG, "onReceiveData: checkdata errror");
            return;
        }
        parseData(array);
    }

    private void parseData(byte[] array) {
        Logger.d(TAG, "parseData: ");
        boolean isOff;
        if (mIsUp) {
            isOff = (array[1] & 0x01) == (byte) 0;
        } else {
            isOff = (array[1] & 0x02) == (byte) 0;
        }

        if (isOff) {
            Intent intent = new Intent(this, ArrivalActivity.class);
            intent.putExtra("despos", mDesPos);
            intent.putExtra("id", mDevice.getElevatorId());
            startActivity(intent);
            finish();
        }
    }

    private boolean checkData(byte[] array) {
        if (array == null && array.length != 3) {
            Logger.e(TAG, "onReceiveData: return data null or to length error");
            return false;
        }

        if (array[0] != (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head error");
            return false;
        }

        byte sum = VerifyUtils.getCheckNum(array);
        if (sum != array[array.length - 1]) {
            Logger.e(TAG, "onReceiveData: checksum error");
            return false;
        }

        return true;
    }
}
