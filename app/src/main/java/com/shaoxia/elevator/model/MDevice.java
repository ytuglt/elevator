package com.shaoxia.elevator.model;

import android.bluetooth.BluetoothDevice;

import com.shaoxia.elevator.MyApplication;
import com.shaoxia.elevator.R;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class MDevice {
    public static final String START = "WELM";
    public static final String INCALLID = "COP";

    private BluetoothDevice device;
    private int rssi;

    private String floor;
    private String elevatorId;

    private String devName;
    private String devAddress;

    private boolean isInCall;
    private boolean isElevator;

    public MDevice(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
        parseName();
    }

    private void parseName() {
        devName = device.getName();
        devAddress = device.getAddress();

        //TODO TEST
        if (devName != null && devName.startsWith("Zero")) {
            floor = devName.substring(4,7);
            elevatorId = devName.substring(7, 10);
            reNameElevatorId();
            isElevator = true;
            isInCall = false;
            return;
        }
        //TODO

        if (devName == null || !devName.startsWith(START) || devName.length()<10) {
            isElevator = false;
            return;
        }

        floor = devName.substring(4, 7);
        elevatorId = devName.substring(7, 10);
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

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDevice) {
            return device.equals(((MDevice) o).getDevice());
        }
        return false;
    }


}
