package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shaoxia.elevator.log.Logger;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class InCallActivity extends BaseBleComActivity implements View.OnClickListener{
    private static final String TAG = "InCallActivity";
    private TextView mTvFloor;
    private TextView mTvId;

    private View[]imags=new View[8];
    private View[] imags3=new View[3];

    private String mFloor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_in_call);
        TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.call_title);

        Intent intent = getIntent();
        mFloor = intent.getStringExtra("floor");
        String id = intent.getStringExtra("elevator_id");

        mTvFloor = findViewById(R.id.text_floor);
        mTvId = findViewById(R.id.elevator_id);
        mTvFloor.setText(mFloor);
        mTvId.setText(id);

        imags[0]=  findViewById(R.id.open);
        imags[1]=  findViewById(R.id.close);
        imags[2]=  findViewById(R.id.over_weight);
        imags[3]=  findViewById(R.id.fire);
        imags[4]=  findViewById(R.id.earthquake);
        imags[5]=  findViewById(R.id.check);
        imags[6]=  findViewById(R.id.error);
        imags[7]=  findViewById(R.id.stop_service);
        imags3[0]=  findViewById(R.id.fire_control);
        imags3[1]=  findViewById(R.id.emergency);
        imags3[2]=  findViewById(R.id.priority);

        imags[0].setOnClickListener(this);
        imags[1].setOnClickListener(this);
    }

    @Override
    protected void onReceiveData(byte[] array) {
        super.onReceiveData(array);
        checkData(array);

        for (int i=0;i<8;i++){
            if ((array[2]&(1<<i))!=0){
                imags[i].setSelected(true);
            }else {
                imags[i].setSelected(false);
            }
        }
        for (int i=0;i<3;i++){
            if ((array[3]&(1<<i))!=0){
                imags3[i].setSelected(true);
            }else {
                imags3[i].setSelected(false);
            }
        }
    }

    private boolean checkData(byte[] array) {
        if (array == null || array.length != 5) {
            Logger.e(TAG, "onReceiveData: return data length error");
            return false;
        }

        if (array[0] != (byte) 0xEC) {
            Logger.e(TAG, "onReceiveData: return data head error");
            return false;
        }

        byte sum = 0;
        for (int i = 0; i < array.length - 1; i++) {
            sum += array[i];
        }
        if (sum != array[array.length - 1]) {
            Logger.e(TAG, "onReceiveData: checksum error");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        byte[] cmd = new byte[4];
        cmd[0] = (byte) 0xb5;
        cmd[1] = Byte.parseByte(String.valueOf(mFloor));
        switch (view.getId()) {
            case R.id.open:
                cmd[2] = 0x01;
                cmd[3] = getCheckNum(cmd);
                break;
            case R.id.close:
                cmd[2] = 0x02;
                cmd[3] = getCheckNum(cmd);
                break;
        }
        sendData(cmd);
    }

    private byte getCheckNum( byte[] bytes){
        byte checkSum = 0;
        for(int i= 0;i<bytes.length-1;i++) {
            checkSum += bytes[i];
        }
        return checkSum;
    }
}
