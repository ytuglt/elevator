package com.shaoxia.elevator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.shaoxia.elevator.bluetoothle.BleHelper;
import com.shaoxia.elevator.bluetoothle.BleManger;
import com.shaoxia.elevator.bluetoothle.BlueToothLeService.BluetoothLeService;
import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.Configure;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecycleView;
    private static BluetoothAdapter mBluetoothAdapter;

    private Handler mHander;
    private BleManger mBleManger;

    private List<MDevice> mDevices = new ArrayList<>();
   private  DevicesAdapter mDevicesAdapter;

    /**
     * BroadcastReceiver for receiving the GATT communication status
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // Status received when connected to GATT Server
            //连接成功
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                System.out.println("--------------------->连接成功");
                //搜索服务
                BluetoothLeService.discoverServices();
            }
            // Services Discovered from GATT Server
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
//                mHander.removeCallbacks(dismssDialogRunnable);
//                progressDialog.dismiss();
//                prepareGattServices(BluetoothLeService.getSupportedGattServices());
            } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
//                progressDialog.dismiss();
                //connect break (连接断开)
//                showDialog(getString(R.string.conn_disconnected_home));
            }

        }
    };

    /**
     * 发现设备时 处理方法
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Logger.d(TAG, "run: device name is : " + device.getName());
                    MDevice mDev = new MDevice(device, rssi);
                    if (mDevices.contains(mDev))
                        return;
                    mDevices.add(mDev);
                    if (mDevicesAdapter != null) {
                        mDevicesAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };


    /**
     * 构造
     */
    public MainActivity() {
        mHander = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBleManger = BleManger.getInstance();
        mBluetoothAdapter = BleHelper.checkBleSupportAndInitialize(this);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BleHelper.initBle(this, mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScan();
    }

    private void initView() {
        initRecycleView();
    }

    private void initRecycleView() {
        mRecycleView = findViewById(R.id.out_call_list);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));

        mDevicesAdapter = new DevicesAdapter(mDevices);
        mRecycleView.setAdapter(mDevicesAdapter);
    }


    //停止扫描
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "stopScanRunnable run: ");
            stopScan();
        }
    };

    /**
     * 开始扫面入口
     */
    private void startScan() {
        Logger.d(TAG, "startScan: ");
        if (mBleManger.isScanning()) {
            stopScan();
        }
        BleHelper.disconnectDevice();
        BleManger.getInstance().setBleState(BleManger.State.SCANNING);
        scanPrevious21Version();
    }

    /**
     * 版本号21之前的调用该方法搜索
     */
    private void scanPrevious21Version() {
        //10秒后停止扫描
        mHander.postDelayed(stopScanRunnable, Configure.DEFAULT_SCAN_TIME);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    /**
     * ble 停止扫描
     */
    private void stopScan() {
        Logger.d(TAG, "stopScan: ");
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mHander.removeCallbacks(stopScanRunnable);
        mBleManger.setBleState(BleManger.State.IDLE);
    }
}
