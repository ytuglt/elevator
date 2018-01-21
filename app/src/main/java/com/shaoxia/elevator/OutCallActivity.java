package com.shaoxia.elevator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.shaoxia.elevator.bluetoothle.BleHelper;
import com.shaoxia.elevator.bluetoothle.BlueToothLeService.BluetoothLeService;
import com.shaoxia.elevator.bluetoothle.utils.Constants;
import com.shaoxia.elevator.bluetoothle.utils.GattAttributes;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.utils.StringUtils;

import java.util.List;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class OutCallActivity extends BaseActivity {
    private static final String TAG = "OutCallActivity";
    private String mDevAddress;
    private String mDevName;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

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
                Logger.d(TAG, "doCommunicationReceiveLogic: data:" + StringUtils.ByteArraytoHex(array));
//                if (extras.containsKey(Constants.EXTRA_BYTE_UUID_VALUE)) {
//                    if (myApplication != null) {
//                        BluetoothGattCharacteristic requiredCharacteristic = myApplication.getCharacteristic();
//                        String uuidRequired = requiredCharacteristic.getUuid().toString();
//                        String receivedUUID = intent.getStringExtra(Constants.EXTRA_BYTE_UUID_VALUE);
//                        if (isDebugMode){
//                            byte[] array = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
//                            Message msg = new Message(Message.MESSAGE_TYPE.RECEIVE,formatMsgContent(array));
//                            notifyAdapter(msg);
//                        }else if (uuidRequired.equalsIgnoreCase(receivedUUID)) {
//                            byte[] array = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
//                            Message msg = new Message(Message.MESSAGE_TYPE.RECEIVE,formatMsgContent(array,MyApplication.serviceType));
//                            notifyAdapter(msg);
//                        }
//                    }
//                }
            }
//            if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE)) {
//                if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID)) {
//                    BluetoothGattCharacteristic requiredCharacteristic = myApplication.
//                            getCharacteristic();
//                    String uuidRequired = requiredCharacteristic.getUuid().toString();
//                    String receivedUUID = intent.getStringExtra(
//                            Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID);
//
//                    byte[] array = intent
//                            .getByteArrayExtra(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE);
//
////                        System.out.println("GattDetailActivity---------------------->descriptor:" + Utils.ByteArraytoHex(array));
//                    if (isDebugMode){
//                        updateButtonStatus(array);
//                    }else if (uuidRequired.equalsIgnoreCase(receivedUUID)) {
//                        updateButtonStatus(array);
//                    }
//
//                }
//            }
        }

        if (action.equals(BluetoothLeService.ACTION_GATT_DESCRIPTORWRITE_RESULT)) {
            if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT)) {
                int status = extras.getInt(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT);
                if (status != BluetoothGatt.GATT_SUCCESS) {
//                    Snackbar.make(rlContent,R.string.option_fail,Snackbar.LENGTH_LONG).show();
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
                Logger.e(TAG, "doCommunicationReceiveLogic: err: " + errorMessage );
//                showDialog(errorMessage);
            }

        }

        //write characteristics succcess
        if (action.equals(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS)) {
//            list.get(list.size() - 1).setDone(true);
//            adapter.notifyItemChanged(list.size() - 1);
            Logger.d(TAG, "doCommunicationReceiveLogic: write success");
            prepareBroadcastDataNotify(notifyCharacteristic);
        }

        if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
//                if (state == BluetoothDevice.BOND_BONDING) {}
//                else if (state == BluetoothDevice.BOND_BONDED) {}
//                else if (state == BluetoothDevice.BOND_NONE) {}
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
            Toast.makeText(OutCallActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
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
            String name = GattAttributes.lookup(gattService.getUuid().toString(), "UnkonwService");
            if (name.equals("UnkonwService")) {
                Logger.d(TAG, "unKnowService uuid = " + uuid);
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

                String s = "11";
                byte[] array = StringUtils.hexStringToByteArray(s);
                writeCharacteristic(writeCharacteristic, array);
//                prepareBroadcastDataNotify(notifyCharacteristic);
            }
//            MService mService = new MService(name, gattService);
//            list.add(mService);
        }
    }

    /**
     * Preparing Broadcast receiver to broadcast notify characteristics
     *
     * @param characteristic
     */
    void prepareBroadcastDataNotify(
            BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            BluetoothLeService.setCharacteristicNotification(characteristic, true);
        }

    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] bytes) {
        // Writing the hexValue to the characteristics
        try {
            Logger.d(TAG, "writeCharacteristic: " + bytes);
            BluetoothLeService.writeCharacteristicGattDb(characteristic,
                    bytes);
        } catch (NullPointerException e) {
            Logger.e(TAG, "writeCharacteristic: exception" + e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_out_call);

        Intent intent = getIntent();
        mDevAddress = intent.getStringExtra("dev_mac");
        mDevName = intent.getStringExtra("dev_name");
        Logger.d(TAG, "onCreate: mDevAddress = " + mDevAddress + ",devname = " + mDevName);
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
}
