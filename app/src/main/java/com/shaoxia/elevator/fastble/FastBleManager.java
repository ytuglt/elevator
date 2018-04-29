package com.shaoxia.elevator.fastble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.shaoxia.elevator.MyApplication;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.logic.FloorsLogic;
import com.shaoxia.elevator.utils.Configure;

import java.util.UUID;

public class FastBleManager {
    private static final String TAG = "FastBleManager";
    private static FastBleManager mInstance;

    public enum STATE {
        IDLE, SCANNING, CONNECTING
    }

    private STATE mState;

    public static FastBleManager getInstance() {
        if (mInstance == null) {
            mInstance = new FastBleManager();
        }
        return mInstance;
    }

    public void init() {
        BleManager.getInstance().init(MyApplication.getInstance());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(1)
                .setOperateTimeout(5000);
    }

    public void startScan(BleScanCallback callback) {
        BleManager.getInstance().scan(callback);
    }

    public void stopScan() {
        Logger.d(TAG, "stopScan: ");
        BleManager.getInstance().cancelScan();
    }

    public void setState(STATE state) {
        mState = state;
    }

    public STATE getState() {
        return mState;
    }

    public void disConnect() {
        BleManager.getInstance().disconnectAllDevice();
    }

    public void destroy() {
        disConnect();
        BleManager.getInstance().destroy();
    }

    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    public void sendData(byte[] data, BleDevice bleDevice, BleGattCallback connectCallBack, BleNotifyCallback notifyCallback) {
        connect(data, bleDevice, connectCallBack, notifyCallback);
    }

    private void connect(final byte[] data, BleDevice bleDevice, final BleGattCallback connectCallBack, final BleNotifyCallback notifyCallback) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                @Override
                public void onStartConnect() {
                    Logger.d(TAG, "onStartConnect: ");
                    connectCallBack.onStartConnect();
                }

                @Override
                public void onConnectFail(BleException exception) {
                    Logger.d(TAG, "onConnectFail: ");
                    connectCallBack.onConnectFail(exception);
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Logger.d(TAG, "onConnectSuccess: ");

                    BluetoothGattService service = gatt.getService(UUID.fromString(Configure.SERVICE_UUID));
                    if (service == null) {
                        Logger.d(TAG, "onConnectSuccess: service is null");
                        return;
                    }
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        int charaProp = characteristic.getProperties();
                        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) ||
                                ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)) {
                            Logger.d(TAG, "onConnectSuccess: writeCharacteristic");
                            writeCharacteristic = characteristic;
                        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            Logger.d(TAG, "onConnectSuccess: notifyCharacteristic");
                            notifyCharacteristic = characteristic;
                        }
                    }
                    setMtu(data, bleDevice, 512, notifyCallback);
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    Logger.d(TAG, "onDisConnected: ");
                    FloorsLogic.getInstance().onBleDisconnected();
                    connectCallBack.onDisConnected(isActiveDisConnected, device, gatt, status);
                }
            });
        }

    }

    private void setMtu(final byte[] data, final BleDevice bleDevice, int mtu, final BleNotifyCallback notifyCallback) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Logger.i(TAG, "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Logger.i(TAG, "onMtuChanged: " + mtu);
                writeData(data, bleDevice, notifyCallback);
            }
        });
    }

    private void writeData(byte[] data, final BleDevice bleDevice, final BleNotifyCallback notifyCallback) {
        BleManager.getInstance().write(
                bleDevice,
                writeCharacteristic.getService().getUuid().toString(),
                writeCharacteristic.getUuid().toString(),
                data,
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        Logger.d(TAG, "onWriteSuccess: write success, current: " + current
                                + " total: " + total
                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                        notifyStart(bleDevice, notifyCallback);
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        Logger.d(TAG, "run: " + exception.toString());
                    }
                });

//        mHasReceiveData = false;
    }

    private void notifyStart(BleDevice bleDevice, BleNotifyCallback notifyCallback) {
        BleManager.getInstance().notify(
                bleDevice,
                notifyCharacteristic.getService().getUuid().toString(),
                notifyCharacteristic.getUuid().toString(),
                notifyCallback);
    }


}
