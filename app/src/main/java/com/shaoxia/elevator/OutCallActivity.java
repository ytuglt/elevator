package com.shaoxia.elevator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.shaoxia.elevator.log.Logger;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class OutCallActivity extends BaseBleComActivity implements View.OnClickListener {
    private static final String TAG = "OutCallActivity";
    private TextView mTvFloor;
    private TextView mTvId;

    private View mUp;
    private View mDown;
    private View mLock;
    private View mFire;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_call);
        TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.call_title);

        Intent intent = getIntent();
        String floor = intent.getStringExtra("floor");
        String id = intent.getStringExtra("elevator_id");

        mTvFloor = findViewById(R.id.text_floor);
        mTvId = findViewById(R.id.elevator_id);
        mTvFloor.setText(floor);
        mTvId.setText(id);

        mUp = findViewById(R.id.up_check);
        mDown = findViewById(R.id.down_check);
        mLock = findViewById(R.id.elevator_lock);
        mFire = findViewById(R.id.fire_control);
        mUp.setOnClickListener(this);
        mDown.setOnClickListener(this);
        mFire.setOnClickListener(this);
        mLock.setOnClickListener(this);
    }

    @Override
    protected void onReceiveData(byte[] array) {
        super.onReceiveData(array);
        if (!checkData(array)) {
            return;
        }
        byte data = array[1];
        boolean up = (data & 0x01) == 0x01;
        mUp.setSelected(up);
        boolean down = (data & 0x02) == 0x02;
        mDown.setSelected(down);
        boolean fire = (data & 0x04) == 0x04;
        mFire.setSelected(fire);
        boolean lock = (data & 0x08) == 0x08;
        mLock.setSelected(lock);

    }

    private boolean checkData(byte[] array) {
        if (array == null || array.length != 3) {
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
    protected void sendQueryData() {
        super.sendQueryData();
    }

    @Override
    public void onClick(View view) {
        byte[] cmd = new byte[3];
        cmd[0] = (byte) 0xb5;
        switch (view.getId()) {
            case R.id.up_check:
                cmd[1] = 0x01;
                cmd[2] = (byte) (cmd[0] + cmd[1]);
                sendData(cmd);
                break;
            case R.id.down_check:
                cmd[1] = 0x02;
                cmd[2] = (byte) (cmd[0] + cmd[1]);
                sendData(cmd);
                break;
            case R.id.fire_control:
                cmd[1] = 0x04;
                cmd[2] = (byte) (cmd[0] + cmd[1]);
                sendData(cmd);
                break;
            case R.id.elevator_lock:
                cmd[1] = 0x08;
                cmd[2] = (byte) (cmd[0] + cmd[1]);
                sendData(cmd);
                break;
        }
    }
}
