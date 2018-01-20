package com.shaoxia.elevator.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class MDevice {
    private BluetoothDevice device;
    private int rssi;

    private String floor;
    private String elevatorId;

    public MDevice() {

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

    public MDevice(BluetoothDevice device, int rssi) {

        this.device = device;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDevice) {
            return device.equals(((MDevice) o).getDevice());
        }
        return false;
    }


}
