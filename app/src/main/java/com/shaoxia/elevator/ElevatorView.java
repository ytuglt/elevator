package com.shaoxia.elevator;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shaoxia.elevator.model.MDevice;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;

/**
 * Created by gonglt1 on 18-3-8.
 */

public class ElevatorView extends LinearLayout {
    private MDevice mDevice;
    private WheelView mFloolWheelView;

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
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_elevator, this, true);

        mFloolWheelView = (WheelView) findViewById(R.id.floor_wheel);
        mFloolWheelView.setWheelSize(5);
        mFloolWheelView.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        mFloolWheelView.setSkin(WheelView.Skin.None);
        mFloolWheelView.setWheelData(createArrays());

        mFloolWheelView.setSelection(2);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.textColor = getContext().getResources().getColor(R.color.floor_unsel, null);
        style.selectedTextColor = getContext().getResources().getColor(R.color.floor_sel, null);
        style.textSize = 25;
        style.selectedTextSize = 36;
        mFloolWheelView.setStyle(style);

    }

    private ArrayList<String> createArrays() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            list.add("" + i);
        }
        return list;
    }

}
