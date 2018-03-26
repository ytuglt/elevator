package com.shaoxia.elevator.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.shaoxia.elevator.MyApplication;
import com.shaoxia.elevator.R;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class MDevice implements Serializable {
    public final static int IDLE = 0;
    public final static int COMUNICATING = 1;

    public static final String START = "WELM";
    public static final String INCALLID = "COP";

    //    private BluetoothDevice device;
    private int rssi;

    private String floor;
    private String elevatorId;

    private String devName;
    private String devAddress;

    private boolean isInCall;
    private boolean isElevator;

    private List<String> mFloors;

    private Map<String, Byte> mFloorsMap;

    public MDevice() {

    }

    public MDevice(BluetoothDevice device, int rssi) {
        this.rssi = rssi;
        parseName(device);
    }

    private void parseName(BluetoothDevice device) {
        devName = device.getName();
        devAddress = device.getAddress();

//        //TODO TEST
//        if (devName != null && devName.startsWith("Zero")) {
//            floor = devName.substring(4, 7);
//            elevatorId = devName.substring(7, 10);
//            reNameElevatorId();
//            floor = "2";
//            elevatorId = "00B";
//            isElevator = true;
//            isInCall = true;
//            return;
//        }
//        //TODO

        if (devName == null || !devName.startsWith(START) || devName.length() < 10) {
            isElevator = false;
            return;
        }

        floor = devName.substring(7, 10);
        elevatorId = devName.substring(4, 7);
        if (INCALLID.equals(elevatorId)) {
            isInCall = true;
            floor = MyApplication.getInstance().getResources().getString(R.string.in_elevator);
        } else {
            isInCall = false;
        }
        reNameElevatorId();
        isElevator = true;
    }

    private void reNameElevatorId() {
        elevatorId = MyApplication.getInstance().getResources().getString(R.string.elevator) + elevatorId;
    }

    public boolean isElevator() {
        return isElevator;
    }

    public boolean isInCall() {
        return isInCall;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }

    public String getDevAddress() {
        return devAddress;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevName() {
        return devName;
    }

    public List<String> getFloors() {
        return mFloors;
    }

    public void setFloors(List<String> mFloors) {
        this.mFloors = mFloors;
    }

    public Map<String, Byte> getFloorsMap() {
        return mFloorsMap;
    }

    public void setFloorsMap(Map<String, Byte> mFloorsMap) {
        this.mFloorsMap = mFloorsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDevice) {
            return getDevAddress().equals(((MDevice) o).getDevAddress());
        }
        return false;
    }

}
