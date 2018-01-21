package com.shaoxia.elevator.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.shaoxia.elevator.R;
import com.shaoxia.elevator.bluetoothle.BlueToothLeService.BluetoothLeService;
import com.shaoxia.elevator.bluetoothle.utils.Utils;
import com.shaoxia.elevator.log.Logger;

/**
 * Created by gonglt1 on 18-1-20.
 */

public class BleHelper {
    private static final String TAG = "BleHelper";

    /**
     * 检查蓝牙是否可用
     */
    public static BluetoothAdapter checkBleSupportAndInitialize(Context context) {
        if (context == null) {
            return null;
        }
        // Use this check to determine whether BLE is supported on the device.
        if (!context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.device_ble_not_supported,
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        // Initializes a Blue tooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            // Device does not support Blue tooth
            Toast.makeText(context,
                    R.string.device_ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            return bluetoothAdapter;
        }
        //打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        return bluetoothAdapter;
    }

    public static void initBle(Context context, BroadcastReceiver receiver) {
        if (context == null) {
            return;
        }
        initService(context);
        initBroadcast(context, receiver);
    }

    /**
     * 初始化服务
     */
    public static void initService(Context context) {
        Intent gattServiceIntent = new Intent(context.getApplicationContext(),
                BluetoothLeService.class);
        context.startService(gattServiceIntent);
    }

    /**
     * 初始化广播
     */
    public static void initBroadcast(Context context, BroadcastReceiver receiver) {
        //注册广播接收者，接收消息
        context.registerReceiver(receiver, Utils.makeGattUpdateIntentFilter());
    }

    /**
     * ble 取消连接
     */
    public static void disconnectDevice() {
        Logger.d(TAG, "disconnectDevice: ");
        BluetoothLeService.disconnect();
        BleManger.getInstance().setBleState(BleManger.State.IDLE);
    }

    /**
     * ble 连接
     */
    public static void connectDevice(Context context, String address, String name) {
        Logger.d(TAG, "connectDevice: ");
        //如果是连接状态，断开，重新连接
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_DISCONNECTED) {
            BluetoothLeService.disconnect();
        }
        BleManger.getInstance().setBleState(BleManger.State.CONNECTING);
        BluetoothLeService.connect(address, name, context);
    }

    public static String getPorperties(Context context, BluetoothGattCharacteristic item) {
        return Utils.getPorperties(context, item);
    }

}

