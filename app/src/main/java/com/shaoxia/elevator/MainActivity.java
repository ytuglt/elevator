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
import android.view.View;
import android.widget.Toast;

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
    private RecyclerView mOutRecycleView;
    private static BluetoothAdapter mBluetoothAdapter;

    private Handler mHander;
    private BleManger mBleManger;

    private List<MDevice> mDevices = new ArrayList<>();
    private DevicesAdapter mDevicesAdapter;

    private RecyclerView mInRycycleView;
    private List<MDevice> mInDevices = new ArrayList<>();
    private DevicesAdapter mInDevicesAdapater;

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
                    if (mDevices.contains(mDev) || !mDev.isElevator()) {
                        Logger.d(TAG, "run: devices contains devices or not elevator");
                        return;
                    }
                    Logger.d(TAG, "run: add device" + mDev.getDevice().getName());
                    if (mDev.isInCall()) {
                        mInDevices.add(mDev);
                        if (mInDevicesAdapater != null) {
                            mInDevicesAdapater.notifyDataSetChanged();
                        }
                    } else {
                        mDevices.add(mDev);
                        if (mDevicesAdapter != null) {
                            mDevicesAdapter.notifyDataSetChanged();
                        }
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
        Logger.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        mBleManger = BleManger.getInstance();
        mBluetoothAdapter = BleHelper.checkBleSupportAndInitialize(this);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart: ");
        BleHelper.initService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: ");
        startScan();
    }

    private void initView() {
        initRecycleView();
        initInRecycleView();
    }

    private void initRecycleView() {
        mOutRecycleView = findViewById(R.id.out_call_list);
        mOutRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mOutRecycleView.addItemDecoration(new RecycleViewDivider(this));

        mDevicesAdapter = new DevicesAdapter(mDevices);
        mOutRecycleView.setAdapter(mDevicesAdapter);
        mDevicesAdapter.setOnItemClickListener(new DevicesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Intent intent = new Intent(MainActivity.this, OutCallActivity.class);
                String name = mDevices.get(position).getDevName();
                String address = mDevices.get(position).getDevAddress();
                intent.putExtra("dev_name",name);
                intent.putExtra("dev_mac", address);
                Logger.d(TAG, "onItemClick: name = " + name + ",adrress = " + address);
                startActivity(intent);
            }
        });
    }

    private void initInRecycleView() {
        mInRycycleView = findViewById(R.id.in_call_list);
        mInRycycleView.setLayoutManager(new LinearLayoutManager(this));
        mInRycycleView.addItemDecoration(new RecycleViewDivider(this));

        mInDevicesAdapater = new DevicesAdapter(mInDevices);
        mInRycycleView.setAdapter(mInDevicesAdapater);

        mInDevicesAdapater.setOnItemClickListener(new DevicesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Intent intent = new Intent(MainActivity.this, InCallActivity.class);
                String name = mInDevices.get(position).getDevName();
                String address = mInDevices.get(position).getDevAddress();
                intent.putExtra("dev_name",name);
                intent.putExtra("dev_mac", address);
                Logger.d(TAG, "onItemClick: name = " + name + ",adrress = " + address);
                startActivity(intent);
            }
        });
    }


    //停止扫描
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "stopScanRunnable run: ");
            stopScan();
            if (mInDevices.size() <= 0 && mDevices.size() <= 0) {
                Toast.makeText(MainActivity.this, "未发现设备", Toast.LENGTH_SHORT).show();
            }
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
        Logger.d(TAG, "scanPrevious21Version: ");
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

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause: ");
        stopScan();
    }
}
