package com.shaoxia.elevator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

public class ElevatorView extends LinearLayout implements WheelView.OnWheelItemSelectedListener<String>,
        View.OnClickListener {
    private static final String TAG = "ElevatorView";
    private MDevice mDevice;
    private WheelView mFloolWheelView;
    private ArrayWheelAdapter mWheelAdapter;

    private TextView mBtnCall;

    private boolean mEnableCall;

    private List<String> mFloors;

    private TextView mTitle;

    private int mSelPosition;

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
        Logger.d(TAG, "init: ");
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_elevator, this, true);
        mBtnCall = findViewById(R.id.btn_call);
        mBtnCall.setOnClickListener(this);
        mTitle = findViewById(R.id.elevator_title);
        String text;
        if (mDevice.getElevatorId() == null || mDevice.getFloor() == null) {
            text = "A梯1层";
        } else {
            text = mDevice.getElevatorId()
                    + getContext().getResources().getString(R.string.elevator_id_unit)
                    + mDevice.getFloor()
                    + getContext().getResources().getString(R.string.floor_unit);
        }

        mTitle.setText(text);
        initWheelView();
    }

    private void initWheelView() {
        Logger.d(TAG, "initWheelView: ");
        mFloolWheelView = (WheelView) findViewById(R.id.floor_wheel);
        mFloolWheelView.setWheelSize(5);
        mWheelAdapter = new ArrayWheelAdapter(getContext());
        mFloolWheelView.setWheelAdapter(mWheelAdapter);
        mFloolWheelView.setSkin(WheelView.Skin.None);
        updateWheelData();
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.textColor = getContext().getResources().getColor(R.color.floor_unsel, null);
        style.selectedTextColor = getContext().getResources().getColor(R.color.floor_sel, null);
        style.textSize = 25;
        style.selectedTextSize = 36;
        mFloolWheelView.setStyle(style);
        mFloolWheelView.setOnWheelItemSelectedListener(this);
    }

    public void updateWheelData() {
        Logger.d(TAG, "initWheelData: ");
        mFloors = mDevice.getFloors();
        if (mFloors != null && mFloors.size() > 0) {
            Logger.d(TAG, "initWheelData: real wheel data");
            mFloolWheelView.setWheelData(mFloors);
            int index = mFloors.indexOf(mDevice.getFloor());
            if (index < 0) {
                index = 0;
            }
            mFloolWheelView.setSelection(index);
            mWheelAdapter.notifyDataSetChanged();
            mEnableCall = true;
        } else {
            Logger.d(TAG, "initWheelData: init wheel data");
            mFloors = createArrays();
            mFloolWheelView.setWheelData(mFloors);
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
        mSelPosition = position;
        if (mFloors.contains(mDevice.getFloor())) {
            updateCallView(!s.equals(mDevice.getFloor()));
        } else {
            updateCallView(false);
        }
    }

    private void updateCallView(boolean enable) {
        Logger.d(TAG, "updateCallView: enable = " + enable + ";mEnableCall " + mEnableCall);
        mBtnCall.setEnabled(enable && mEnableCall);
    }

    private void updateCallView() {
        boolean enable = false;
        if (mFloolWheelView != null && mFloors != null && mDevice != null) {
            int selection = mFloolWheelView.getSelection();
            int foorIndex = mFloors.indexOf(mDevice.getFloor());
            enable = (selection == foorIndex);
        }
        Logger.d(TAG, "updateCallView no param: enable = " + enable + ";mEnableCall " + mEnableCall);
        enable = (enable && mEnableCall);
        Logger.d(TAG, "updateCallView: enable " + enable);
        mBtnCall.setEnabled(enable);
    }

    public void setState(int state) {
        Logger.d(TAG, "setState: state " + state);
        switch (state) {
            case MDevice.IDLE:
                mBtnCall.setText(getContext().getResources().getString(R.string.call));
                updateCallView();
                break;
            case MDevice.COMUNICATING:
                mBtnCall.setText(getContext().getResources().getString(R.string.comunicating));
                updateCallView(false);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_call:
                Intent intent = new Intent(getContext(), OutWaitingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", mDevice);//序列化
                intent.putExtras(bundle);//发送数据
//                intent.putExtra("device", mDevice);
                intent.putExtra("title", mTitle.getText());
                intent.putExtra("despos", mSelPosition);
                getContext().startActivity(intent);
                break;
        }
    }
}
