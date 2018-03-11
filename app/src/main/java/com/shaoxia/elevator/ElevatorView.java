package com.shaoxia.elevator;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-8.
 */

public class ElevatorView extends LinearLayout implements WheelView.OnWheelItemSelectedListener<String> {
    private static final String TAG = "ElevatorView";
    private MDevice mDevice;
    private WheelView mFloolWheelView;
    private ArrayWheelAdapter mWheelAdapter;

    private TextView mBtnCall;

    private boolean mEnableCall;

    public ElevatorView(Context context, MDevice device) {
        super(context);
        mDevice = device;
        init();
    }

    public ElevatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElevatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.d(TAG, "init: ");
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_elevator, this, true);
        mBtnCall = findViewById(R.id.btn_call);
        initWheelView();
    }

    private void initWheelView() {
        Logger.d(TAG, "initWheelView: ");
        mFloolWheelView = (WheelView) findViewById(R.id.floor_wheel);
        mFloolWheelView.setWheelSize(5);
        mWheelAdapter = new ArrayWheelAdapter(getContext());
        mFloolWheelView.setWheelAdapter(mWheelAdapter);
        mFloolWheelView.setSkin(WheelView.Skin.None);
        initWheelData();
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.textColor = getContext().getResources().getColor(R.color.floor_unsel, null);
        style.selectedTextColor = getContext().getResources().getColor(R.color.floor_sel, null);
        style.textSize = 25;
        style.selectedTextSize = 36;
        mFloolWheelView.setStyle(style);
        mFloolWheelView.setOnWheelItemSelectedListener(this);
    }

    private void initWheelData() {
        Logger.d(TAG, "initWheelData: ");
        List<String> floors = mDevice.getFloors();
        if (floors != null && floors.size() > 0) {
            Logger.d(TAG, "initWheelData: real wheel data");
            mFloolWheelView.setWheelData(floors);
            mFloolWheelView.setSelection(floors.indexOf(mDevice.getFloor()));
            mEnableCall = true;
        } else {
            Logger.d(TAG, "initWheelData: init wheel data");
            mFloolWheelView.setWheelData(createArrays());
            mFloolWheelView.setSelection(2);
            mEnableCall = false;
        }
    }

    private List<String> createArrays() {
        Logger.d(TAG, "createArrays: ");
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 3; i >= -2; i--) {
            if (i == 0) {
                continue;
            }
            list.add("" + i);
        }
        return list;
    }

    @Override
    public void onItemSelected(int position, String s) {
        Logger.d(TAG, "onItemSelected: position = " + position + ";s = " + s);
        updateCallView(!s.equals(mDevice.getFloor()));
    }

    private void updateCallView(boolean enable) {
        Logger.d(TAG, "updateCallView: enable = " + enable + ";mEnableCall " + mEnableCall);
        mBtnCall.setEnabled(enable && mEnableCall);
    }

    public void setState(MDevice.State state) {
        switch (state) {
            case IDLE:

                break;
            case COMUNICATING:
                break;
        }
    }
}
