package com.shaoxia.elevator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-11.
 */

public abstract class BaseArrivalActivity extends BaseActivity {
    private static final String TAG = "BaseArrivalActivity";

    protected int mDesPos;
    protected String mElevatorId;

    protected MDevice mDevice;

    protected boolean mIsUp;

    TextView mTitleView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_arrival);
        initData();

        initTitleView();
        updateFloorsView();

    }

    private void initData() {
        mDevice = (MDevice) getIntent().getSerializableExtra("device");
        mDesPos = getIntent().getIntExtra("despos", -1);
        mElevatorId = getIntent().getStringExtra("id");
        mIsUp = getIntent().getBooleanExtra("isUp", true);
    }

    private void initTitleView() {
        String title = mElevatorId + getResources().getString(R.string.elevator_id_unit) +
                mDevice.getFloor() + getResources().getString(R.string.floor_unit);
        mTitleView = findViewById(R.id.elevator_title);
        mTitleView.setText(title);
    }

    private void updateFloorsView() {
        TextView tvOne = findViewById(R.id.floor_one);
        TextView tvTwo = findViewById(R.id.floor_two);
        TextView tvThree = findViewById(R.id.floor_three);
        TextView tvFour = findViewById(R.id.floor_four);
        TextView tvFive = findViewById(R.id.floor_five);

        List<String> floors = getFloors();
        tvOne.setText(floors.get(0));
        tvTwo.setText(floors.get(1));
        tvThree.setText(floors.get(2));
        tvFour.setText(floors.get(3));
        tvFive.setText(floors.get(4));

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
    protected abstract int getLightPos();

    private List<String> getFloors() {
        List<String> floors = new ArrayList<>();
        List<String> allFloors = mDevice.getFloors();
        int curpos = getLightPos();

        if (mIsUp) {
            if (curpos - 4 >= 0) {
                for (int i = curpos - 4; i <= curpos; i++) {
                    floors.add(allFloors.get(i));
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    floors.add(allFloors.get(i));
                }
            }
        } else {
            int size = allFloors.size();
            if (curpos + 4 < size) {
                for (int i = curpos; i < size; i++) {
                    floors.add(allFloors.get(i));
                }
            } else {
                for (int i = size - 5; i < size; i++) {
                    floors.add(allFloors.get(i));
                }
            }
        }

        return floors;
    }
}
