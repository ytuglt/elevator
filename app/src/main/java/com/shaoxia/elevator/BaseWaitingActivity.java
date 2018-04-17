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

//    private TextView mTvOne;
//    private TextView mTvTwo;
//    private TextView mTvThree;
//    private TextView mTvFour;
//    private TextView mTvFive;

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

//        initFloosView();
        updateFloorsView();

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
        mDevice = (MDevice) getIntent().getSerializableExtra("device");
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

//    private void initFloosView() {
//        mTvOne = findViewById(R.id.floor_one);
//        mTvTwo = findViewById(R.id.floor_two);
//        mTvThree = findViewById(R.id.floor_three);
//        mTvFour = findViewById(R.id.floor_four);
//        mTvFive = findViewById(R.id.floor_five);
//    }

//    private void updateFloors() {
//        TextView tvOne = findViewById(R.id.floor_one);
//        TextView tvTwo = findViewById(R.id.floor_two);
//        TextView tvThree = findViewById(R.id.floor_three);
//        TextView tvFour = findViewById(R.id.floor_four);
//        TextView tvFive = findViewById(R.id.floor_five);
//
//        List<String> floors;
//        List<String> allFloors = mDevice.getFloors();
//        if (allFloors.size() >= 4) {
//            floors = getFloors();
//            mTvOne.setText(floors.get(0));
//            mTvTwo.setText(floors.get(1));
//            mTvThree.setText(floors.get(2));
//            mTvFour.setText(floors.get(3));
//        } else {
//            floors = allFloors;
//            switch (allFloors.size()) {
//                case 2:
//                    mTvOne.setText(floors.get(0));
//                    mTvTwo.setText(floors.get(1));
//                    mTvThree.setVisibility(View.GONE);
//                    mTvFour.setVisibility(View.GONE);
//                    break;
//                case 3:
//                    mTvOne.setText(floors.get(0));
//                    mTvTwo.setText(floors.get(1));
//                    mTvThree.setText(floors.get(2));
//                    mTvFour.setVisibility(View.GONE);
//                    break;
//                case 4:
//                    tvOne.setText(floors.get(0));
//                    tvTwo.setText(floors.get(1));
//                    tvThree.setText(floors.get(2));
//                    tvFour.setText(floors.get(3));
//                    tvFive.setVisibility(View.GONE);
//                    break;
//            }
//        }
//
//        int pos = floors.indexOf(mDevice.getFloors().get(getLightPos()));
//        switch (pos) {
//            case 0:
//                lightCurFloor(mTvOne);
//                break;
//            case 1:
//                lightCurFloor(mTvTwo);
//                break;
//            case 2:
//                lightCurFloor(mTvThree);
//                break;
//            case 3:
//                lightCurFloor(mTvFour);
//                break;
//        }
//    }

    private void updateFloorsView() {
        TextView tvOne = findViewById(R.id.floor_one);
        TextView tvTwo = findViewById(R.id.floor_two);
        TextView tvThree = findViewById(R.id.floor_three);
        TextView tvFour = findViewById(R.id.floor_four);
        TextView tvFive = findViewById(R.id.floor_five);

        List<String> floors;
        List<String> allFloors = mDevice.getFloors();
        if (allFloors.size() >= 5) {
            floors = getFloors();
            tvOne.setText(floors.get(0));
            tvTwo.setText(floors.get(1));
            tvThree.setText(floors.get(2));
            tvFour.setText(floors.get(3));
            tvFive.setText(floors.get(4));
        } else {
            floors = allFloors;
            switch (allFloors.size()) {
                case 2:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setVisibility(View.GONE);
                    tvFour.setVisibility(View.GONE);
                    tvFive.setVisibility(View.GONE);
                    break;
                case 3:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setText(floors.get(2));
                    tvFour.setVisibility(View.GONE);
                    tvFive.setVisibility(View.GONE);
                    break;
                case 4:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setText(floors.get(2));
                    tvFour.setText(floors.get(3));
                    tvFive.setVisibility(View.GONE);
                    break;
            }
        }

        int pos = floors.indexOf(mDevice.getFloors().get(getLightPos()));
        switch (pos) {
            case 0:
                lightCurFloor(tvOne);
                break;
            case 1:
                lightCurFloor(tvTwo);
                break;
            case 2:
                lightCurFloor(tvThree);
                break;
            case 3:
                lightCurFloor(tvFour);
                break;
            case 4:
                lightCurFloor(tvFive);
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

        int size = allFloors.size();
        if (curpos - 2 <= 0) {
            for (int i = 0; i < 5; i++) {
                floors.add(allFloors.get(i));
            }
        } else if (curpos + 2 >= (size - 1)) {
            for (int i = size - 5; i < size; i++) {
                floors.add(allFloors.get(i));
            }
        } else {
            for (int i = curpos - 2; i <= curpos + 2; i++) {
                floors.add(allFloors.get(i));
            }
        }

        return floors;
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
