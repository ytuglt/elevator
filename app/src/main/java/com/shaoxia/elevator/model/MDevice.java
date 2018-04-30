package com.shaoxia.elevator.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.clj.fastble.data.BleDevice;
import com.shaoxia.elevator.MyApplication;
import com.shaoxia.elevator.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class MDevice extends BleDevice {
    private static final String TAG = "MDevice";
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
        super();
    }

    public MDevice(Parcel in) {
        super(in);
        rssi = in.readInt();
        floor = in.readString();
        elevatorId = in.readString();
        devName = in.readString();
        devAddress = in.readString();
        isInCall = in.readByte() != 0;
        isElevator = in.readByte() != 0;
        mFloors = in.createStringArrayList();

        int size = in.readInt();
        if (mFloorsMap == null) {
            mFloorsMap = new HashMap<>();
        }
        for(int i = 0; i < size; i++){
            String key = in.readString();
            Byte value = in.readByte();
            mFloorsMap.put(key,value);
        }
    }

    public MDevice(BleDevice device) {
        super(device);
        parseName(device);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(rssi);
        dest.writeString(floor);
        dest.writeString(elevatorId);
        dest.writeString(devName);
        dest.writeString(devAddress);
        dest.writeByte((byte) (isInCall ? 1 : 0));
        dest.writeByte((byte) (isElevator ? 1 : 0));
        dest.writeStringList(mFloors);

        if(mFloorsMap != null) {
            dest.writeInt(mFloorsMap.size());
            for (Map.Entry<String, Byte> entry : mFloorsMap.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeByte(entry.getValue());
            }
        }
    }

    public static final Parcelable.Creator<MDevice> CREATOR = new Parcelable.Creator<MDevice>() {
        @Override
        public MDevice createFromParcel(Parcel in) {
            return new MDevice(in);
        }

        @Override
        public MDevice[] newArray(int size) {
            return new MDevice[size];
        }
    };

    private void parseName(BleDevice device) {
        devName = device.getName();
        devAddress = device.getMac();

//        //TODO TEST
//        if (devName != null && devName.startsWith("Zero")) {
//            floor = devName.substring(4, 7);
//            elevatorId = devName.substring(7, 10);
////            reNameElevatorId();
//            floor ="  1".trim();
//            Logger.d(TAG, "parseName:floor =" + floor);
//            elevatorId = "XXX";
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
        floor = floor.trim();
//        floor = String.valueOf(Integer.parseInt(floor));

        elevatorId = devName.substring(4, 7);
        if (INCALLID.equals(floor)) {
            isInCall = true;
            floor = MyApplication.getInstance().getResources().getString(R.string.in_elevator);
        } else {
            isInCall = false;
        }
//        reNameElevatorId();
        isElevator = true;
    }

//    private void reNameElevatorId() {
//        elevatorId = MyApplication.getInstance().getResources().getString(R.string.elevator) + elevatorId;
//    }

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

    public String getFloorTitle(Context context) {
        String text;
        if (isInCall) {
            text = getElevatorId()
                    + context.getResources().getString(R.string.elevator_id_unit)
                    + context.getResources().getString(R.string.in_elevator);
        } else {
            text = getElevatorId()
                    + context.getResources().getString(R.string.elevator_id_unit)
                    + getFloor()
                    + context.getResources().getString(R.string.floor_unit);
        }
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDevice) {
            return getDevAddress().equals(((MDevice) o).getDevAddress());
        }
        return false;
    }

}
