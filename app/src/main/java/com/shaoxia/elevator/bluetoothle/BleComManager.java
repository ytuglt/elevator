package com.shaoxia.elevator.bluetoothle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.shaoxia.elevator.BaseBleComActivity;
import com.shaoxia.elevator.bluetoothle.BlueToothLeService.BluetoothLeService;
import com.shaoxia.elevator.bluetoothle.utils.Constants;
import com.shaoxia.elevator.bluetoothle.utils.GattAttributes;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.utils.Configure;
import com.shaoxia.elevator.utils.StringUtils;

import java.util.List;

/**
 * Created by gonglt1 on 2018/3/10.
 */

public class BleComManager {
    private static final String TAG = "BleComManager";
    private static BleComManager mInstance;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    private Handler mHander;

    private String mDevAddress;
    private String mDevName;
    private byte[] mSendData;

    private Context mContext;

    public BleComManager(Context context) {
        mContext = context;
        mHander = new Handler();
        BleHelper.initBroadcast(context, mGattUpdateReceiver);
    }

    public static BleComManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BleComManager(context);
        }
        return mInstance;
    }

    public void setDevAddress(String address) {
        this.mDevAddress = address;
    }

    public void setDevName(String name) {
        mDevName = name;
    }

    /**
     * BroadcastReceiver for receiving the GATT communication status
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Logger.d(TAG, "onReceive: action : " + action);
            doConnectReceiveLogic(action);
            doCommunicationReceiveLogic(intent);
        }
    };

    private void doCommunicationReceiveLogic(Intent intent) {
        String action = intent.getAction();
        //There are four basic operations for moving data in BLE: read, write, notify,
        // and indicate. The BLE protocol specification requires that the maximum data
        // payload size for these operations is 20 bytes, or in the case of read operations,
        // 22 bytes. BLE is built for low power consumption, for infrequent short-burst data transmissions.
        // Sending lots of data is possible, but usually ends up being less efficient than classic Bluetooth
        // when trying to achieve maximum throughput.  从google查找的，解释了为什么android下notify无法解释超过
        //20个字节的数据
        Bundle extras = intent.getExtras();
        if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            // Data Received
            if (extras.containsKey(Constants.EXTRA_BYTE_VALUE)) {
                byte[] array = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
                if (mOnComListener != null) {
                    mOnComListener.onReceiveData(array);
                } else {
                    Logger.e(TAG, "doConnectReceiveLogic: mOnComListener is null");
                }
//                onReceiveData(array);
            }
        }

        if (action.equals(BluetoothLeService.ACTION_GATT_DESCRIPTORWRITE_RESULT)) {
            if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT)) {
                int status = extras.getInt(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Logger.e(TAG, "doCommunicationReceiveLogic: operation failed");
                } else {
                    Logger.d(TAG, "doCommunicationReceiveLogic: operation success");
                }
            }
        }

        if (action.equals(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_ERROR)) {
            if (extras.containsKey(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE)) {
                String errorMessage = extras.
                        getString(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE);
                System.out.println("GattDetailActivity---------------------->err:" + errorMessage);
                Logger.e(TAG, "doCommunicationReceiveLogic: err: " + errorMessage);
            }

        }

        //write characteristics succcess
        if (action.equals(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS)) {
            Logger.d(TAG, "doCommunicationReceiveLogic: write success");
            BleHelper.prepareBroadcastDataNotify(notifyCharacteristic);
            if (mOnComListener != null) {
                mOnComListener.onWriteSuccess();
            } else {
                Logger.e(TAG, "doConnectReceiveLogic: mOnComListener is null");
            }
        }

        if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
        }
    }

    private void doConnectReceiveLogic(String action) {
        // Status received when connected to GATT Server
        //连接成功
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            Logger.d(TAG, "onReceive: connect success");
            //搜索服务
            BluetoothLeService.discoverServices();
        }
        // Services Discovered from GATT Server
        else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                .equals(action)) {
//                mHander.removeCallbacks(dismssDialogRunnable);
//                progressDialog.dismiss();
            prepareGattServices(BluetoothLeService.getSupportedGattServices());
        } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED) ||
                action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
//                progressDialog.dismiss();
            //connect break (连接断开)
//                showDialog(getString(R.string.conn_disconnected_home));
            Logger.d(TAG, "onReceive: disconnect : adress = " + mDevAddress + ",name = " + mDevName);
            if (mOnComListener != null) {
                mOnComListener.onBleDisconnected();
            } else {
                Logger.e(TAG, "doConnectReceiveLogic: mOnComListener is null");
            }
        }

    }

    private void prepareGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            Logger.d(TAG, "prepareGattServices: service uuid : " + uuid);
            if (uuid.equals(GattAttributes.GENERIC_ACCESS_SERVICE) || uuid.equals(GattAttributes.GENERIC_ATTRIBUTE_SERVICE))
                continue;
//            String name = GattAttributes.lookup(gattService.getUuid().toString(), "UnkonwService");
            if (uuid.equals(Configure.SERVICE_UUID)) {
                Logger.d(TAG, "Communication uuid = " + uuid);
                List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic c : characteristics) {
                    Logger.d(TAG, "prepareGattServices: property : " + BleHelper.getPorperties(mContext, c));
                    if (BleHelper.getPorperties(mContext, c).equals("Notify")) {
                        notifyCharacteristic = c;
//                        prepareBroadcastDataNotify(notifyCharacteristic);
                        continue;
                    }

                    if (BleHelper.getPorperties(mContext, c).equals("Write")) {
                        writeCharacteristic = c;
                        continue;
                    }
                }
//                sendQueryData();
                writeData();
            }
        }
    }

    private void writeData() {
        Logger.d(TAG, "writeData: " + StringUtils.ByteArraytoHex(mSendData));
        BleHelper.writeCharacteristic(writeCharacteristic, mSendData);
        mHander.postDelayed(stopConnectRunnable, Configure.DEFAULT_CONNECT_TIME);
    }

    //停止扫描
    private Runnable stopConnectRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "stopConnectRunnable run: ");
            disconnectBle();

            if (mOnComListener != null) {
                mOnComListener.onConnectFailed();
            }
        }
    };

    private void disconnectBle() {
        BleHelper.stopBroadcastDataNotify(notifyCharacteristic);
        BleHelper.disconnectDevice();
    }

    public void sendData(byte[] array) {
        Logger.d(TAG, "sendData: ");
        if (mOnComListener != null) {
            mOnComListener.onCommunicating();
        }
        mSendData = array;
        BleHelper.connectDevice(mContext, mDevAddress, mDevName);
    }

    public void disconnect() {
        Logger.d(TAG, "disconnect: ");
        if (mHander != null) {
            mHander.removeCallbacks(stopConnectRunnable);
        }
        BleHelper.stopBroadcastDataNotify(notifyCharacteristic);
        BleHelper.disconnectDevice();
    }

    public interface OnComListener {
        void onCommunicating();

        void onWriteSuccess();

        void onBleDisconnected();

        void onReceiveData(byte[] array);

        void onConnectFailed();
    }

    private OnComListener mOnComListener;

    public void setOnComListener(OnComListener listener) {
        mOnComListener = listener;
    }

    public void destroy() {
        Logger.d(TAG, "destroy: ");
        if (mContext != null) {
            mContext.unregisterReceiver(mGattUpdateReceiver);
        }
//        disconnect();
        mContext = null;
        mInstance = null;
        mHander = null;
    }
}

