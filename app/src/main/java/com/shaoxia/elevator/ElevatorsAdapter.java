package com.shaoxia.elevator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;
import com.shaoxia.elevator.utils.ViewUtils;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gonglt1 on 18-3-8.
 */

public class ElevatorsAdapter extends PagerAdapter {
    private static final String TAG = "ElevatorsAdapter";
    private List<MDevice> mDevices;
    private Context mContext;

    private LinkedList<View> recycledViews = new LinkedList<>();
    private LayoutInflater inflater;
    private SparseArray<ViewHolder> storeHolders = new SparseArray<>();

    public ElevatorsAdapter(Context context, List<MDevice> devices) {
        mDevices = devices;
        mContext = context;
        this.inflater = LayoutInflater.from(context);
        Logger.d(TAG, "ElevatorsAdapter: mDevices size " + mDevices.size());
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Logger.d(TAG, "instantiateItem: " + position);

        View convertView = null;
        if (null != recycledViews && !recycledViews.isEmpty()) {
            convertView = recycledViews.getFirst();
            recycledViews.removeFirst();
        }

        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.view_elevator, null, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MDevice device = mDevices.get(position);

        holder.updateDeviceInfo(device);
        container.addView(convertView);
        storeHolders.put(position, holder);
        return convertView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Logger.d(TAG, "destroyItem: " + position);
        View view = (View) object;
        container.removeView(view);
        recycledViews.add(view);
        storeHolders.remove(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void reloadData(List<MDevice> devices) {
        Logger.d(TAG, "reloadData: devices size : " + devices.size());
        mDevices = devices;
        notifyDataSetChanged();
    }

    public ViewHolder getHolder(int position) {
        if (null != storeHolders) {
            return storeHolders.get(position);
        }

        return null;
    }

    class ViewHolder implements View.OnClickListener, WheelView.OnWheelItemSelectedListener<String> {
        private WheelView mFloolWheelView;
        private ArrayWheelAdapter mWheelAdapter;
        private TextView mBtnCall;
        private TextView mTitle;
        private ImageView mIvRefresh;

        private boolean mEnableCall;
        private List<String> mFloors;
        private int mSelPosition;
        private MDevice mDevice;

        public ViewHolder(View view) {
            mBtnCall = view.findViewById(R.id.btn_call);
            mTitle = view.findViewById(R.id.elevator_title);
            mIvRefresh = view.findViewById(R.id.refresh);
            mFloolWheelView = (WheelView) view.findViewById(R.id.floor_wheel);
            mIvRefresh.setOnClickListener(this);
            mBtnCall.setOnClickListener(this);
            ViewUtils.setViewtint(mBtnCall);
            ViewUtils.setViewtint(mIvRefresh);
            initWheelView();
        }

        private void initWheelView() {
            Logger.d(TAG, "initWheelView: ");
            mFloolWheelView.setWheelSize(5);
            mWheelAdapter = new ArrayWheelAdapter(mContext);
            mFloolWheelView.setWheelAdapter(mWheelAdapter);
            mFloolWheelView.setSkin(WheelView.Skin.None);
//            updateWheelData();
            WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
            style.backgroundColor = Color.TRANSPARENT;
            style.textColor = mContext.getResources().getColor(R.color.floor_unsel, null);
            style.selectedTextColor = mContext.getResources().getColor(R.color.floor_sel, null);
            style.textSize = 25;
            style.selectedTextSize = 36;
            mFloolWheelView.setStyle(style);
            mFloolWheelView.setOnWheelItemSelectedListener(this);
        }

        public void updateDeviceInfo(MDevice device) {
            mDevice = device;
            updateTilte();
            updateWheelData();
        }

        private void updateTilte() {
            String text;
            if (mDevice.getElevatorId() == null || mDevice.getFloor() == null) {
                text = "A梯1层";
            } else {
                text = mDevice.getElevatorId()
                        + mContext.getResources().getString(R.string.elevator_id_unit)
                        + mDevice.getFloor()
                        + mContext.getResources().getString(R.string.floor_unit);
            }
            mTitle.setText(text);
        }

        public void updateWheelData() {
            Logger.d(TAG, "initWheelData: ");
            mFloors = mDevice.getFloors();
            if (mFloors != null && mFloors.size() > 0) {
                Logger.d(TAG, "initWheelData: real wheel data");
                mFloolWheelView.setWheelData(mFloors);
                int index = mFloors.indexOf(mDevice.getFloor());
                if (index < 0) {
                    index = 0;
                }
                mFloolWheelView.setSelection(index);
                mWheelAdapter.notifyDataSetChanged();
                mEnableCall = true;
            } else {
                Logger.d(TAG, "initWheelData: init wheel data");
                mFloors = createArrays();
                mFloolWheelView.setWheelData(mFloors);
                mFloolWheelView.setSelection(2);
                mEnableCall = false;
            }
        }

        private List<String> createArrays() {
            Logger.d(TAG, "createArrays: ");
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 3; i >= -2; i--) {
                if (i == 0) {
                    continue;
                }
                list.add("" + i);
            }
            return list;
        }

        @Override
        public void onItemSelected(int position, String s) {
            Logger.d(TAG, "onItemSelected: position = " + position + ";s = " + s);
            mSelPosition = position;
            if (mFloors.contains(mDevice.getFloor())) {
                Logger.d(TAG, "onItemSelected: contains device floor");
                updateCallView(!s.equals(mDevice.getFloor()));
            } else {
                Logger.d(TAG, "onItemSelected: not contains device floor");
                updateCallView(false);
            }
        }

        private void updateCallView(boolean enable) {
            Logger.d(TAG, "updateCallView: enable = " + enable + ";mEnableCall " + mEnableCall);
            mBtnCall.setEnabled(enable && mEnableCall);
        }

        private void updateCallView() {
            boolean enable = false;
            if (mFloolWheelView != null && mFloors != null && mDevice != null) {
                int selection = mFloolWheelView.getSelection();
                int foorIndex = mFloors.indexOf(mDevice.getFloor());
                enable = (selection == foorIndex);
            }
            Logger.d(TAG, "updateCallView no param: enable = " + enable + ";mEnableCall " + mEnableCall);
            enable = (enable && mEnableCall);
            Logger.d(TAG, "updateCallView: enable " + enable);
            mBtnCall.setEnabled(enable);
        }

        public void setState(int state) {
            Logger.d(TAG, "setState: state " + state);
            switch (state) {
                case MDevice.IDLE:
                    mBtnCall.setText(mContext.getResources().getString(R.string.call));
                    updateCallView();
                    break;
                case MDevice.COMUNICATING:
                    mBtnCall.setText(mContext.getResources().getString(R.string.comunicating));
                    updateCallView(false);
                    break;
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_call:
                    if (mDevice == null) {
                        Logger.e(TAG, "onClick: devices is null");
                        return;
                    }
                    if (mDevice.getFloors().size() < 2) {
                        Logger.e(TAG, "onClick: Floors is less then 4");
                        Toast.makeText(mContext, "Floors is less then 4", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(mContext, OutWaitingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("device", mDevice);//序列化
                    intent.putExtras(bundle);//发送数据
//                intent.putExtra("device", mDevice);
                    intent.putExtra("title", mTitle.getText());
                    intent.putExtra("despos", mSelPosition);
                    mContext.startActivity(intent);
                    break;
                case R.id.refresh:
                    if (mOnRefreshClickListener != null) {
                        mOnRefreshClickListener.onRerefsh();
                    }
                    break;
            }
        }
    }

    private OnRefreshClickListener mOnRefreshClickListener;

    public void setOnRefreshClickListener(OnRefreshClickListener listener) {
        mOnRefreshClickListener = listener;
    }

    public interface OnRefreshClickListener {
        void onRerefsh();
    }
}
