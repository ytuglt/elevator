package com.shaoxia.elevator;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.shaoxia.elevator.bluetoothle.BleHelper;
import com.shaoxia.elevator.bluetoothle.BlueToothLeService.BluetoothLeService;
import com.shaoxia.elevator.bluetoothle.utils.Constants;
import com.shaoxia.elevator.bluetoothle.utils.GattAttributes;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.utils.Configure;
import com.shaoxia.elevator.utils.StringUtils;

import java.util.List;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class BaseBleComActivity extends BaseActivity {
    private static final String TAG = "BaseBleComActivity";
    private String mDevAddress;
    private String mDevName;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    private Handler mHander;


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
                onReceiveData(array);
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
            onWriteSuccess();
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
        } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
//                progressDialog.dismiss();
            //connect break (连接断开)
//                showDialog(getString(R.string.conn_disconnected_home));
            Logger.d(TAG, "onReceive: disconnect : adress = " + mDevAddress + ",name = " + mDevName);
            onBleDisconnected();
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
                    Logger.d(TAG, "prepareGattServices: property : " + BleHelper.getPorperties(this, c));
                    if (BleHelper.getPorperties(this, c).equals("Notify")) {
                        notifyCharacteristic = c;
//                        prepareBroadcastDataNotify(notifyCharacteristic);
                        continue;
                    }

                    if (BleHelper.getPorperties(this, c).equals("Write")) {
                        writeCharacteristic = c;
                        continue;
                    }
                }
//                sendQueryData();
                writeData();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        mHander = new Handler();
        Intent intent = getIntent();
        mDevAddress = intent.getStringExtra("dev_mac");
        mDevName = intent.getStringExtra("dev_name");
        Logger.d(TAG, "onCreate: mDevAddress = " + mDevAddress + ",devname = " + mDevName);
        showDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart: ");
        BleHelper.initBroadcast(this, mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        BleHelper.connectDevice(this, mDevAddress, mDevName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHander != null) {
            mHander.removeCallbacks(stopConnectRunnable);
        }
        BleHelper.stopBroadcastDataNotify(notifyCharacteristic);
        BleHelper.disconnectDevice();
    }

    private ProgressDialog mProgressDialog;

    private void showDialog() {
        Logger.d(TAG, "showDialog: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(BaseBleComActivity.this);
                    mProgressDialog.setCancelable(false);
                }
                if (!mProgressDialog.isShowing()) {
                    Logger.d(TAG, "showDialog: show dialog");
                    mProgressDialog.show();
//                    mProgressDialog.show(BaseBleComActivity.this, null, "正在通信...");
                }
            }
        });
    }

    private void dismissDialog() {
        Logger.d(TAG, "dismissDialog: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private byte[] mSendData;

    protected void setQueryData(byte[] bytes) {
        mSendData = bytes;
    }

    protected void sendData(byte[] array) {
        mSendData = array;
        showDialog();
        BleHelper.connectDevice(this, mDevAddress, mDevName);
    }

//    protected void sendQueryData() {
//        byte[] cmd = {(byte) 0xb5, 0x00, (byte) 0xb5};
//        sendData(cmd);
//    }

    //停止扫描
    private Runnable stopConnectRunnable = new Runnable() {

        @Override
        public void run() {
            disconnectBle();
        }
    };

    private void writeData() {
        Logger.d(TAG, "sendData: " + StringUtils.ByteArraytoHex(mSendData));
        BleHelper.writeCharacteristic(writeCharacteristic, mSendData);
        mHander.postDelayed(stopConnectRunnable, Configure.DEFAULT_CONNECT_TIME);

    }

    protected void onWriteSuccess() {

    }

    protected void onReceiveData(byte[] array) {
        Logger.d(TAG, "onReceiveData: data:" + StringUtils.ByteArraytoHex(array));
        if (mHander != null) {
            mHander.removeCallbacks(stopConnectRunnable);
        }
        disconnectBle();
    }

    private void disconnectBle() {
        dismissDialog();
        BleHelper.stopBroadcastDataNotify(notifyCharacteristic);
        BleHelper.disconnectDevice();
    }

    protected void onBleDisconnected() {
        Toast.makeText(BaseBleComActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
    }
}
