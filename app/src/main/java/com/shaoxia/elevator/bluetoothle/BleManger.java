package com.shaoxia.elevator.bluetoothle;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class BleManger {
    private static BleManger mInstance;

    public enum State {
        SCANNING, CONNECTING, IDLE
    }

    private State mBleState = State.IDLE;

    public static BleManger getInstance() {
        if (mInstance == null) {
            mInstance = new BleManger();
        }

        return mInstance;
    }

    public void setBleState(State state) {
        mBleState = state;
    }

    public State getBleState() {
        return mBleState;
    }

    public boolean isScanning() {
        return mBleState == State.SCANNING;
    }
}
