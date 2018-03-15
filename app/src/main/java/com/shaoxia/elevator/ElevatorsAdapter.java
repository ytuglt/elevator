package com.shaoxia.elevator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.shaoxia.elevator.log.Logger;
import com.shaoxia.elevator.model.MDevice;

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

    private List<ElevatorView> mViews = new ArrayList<>();

    private ElevatorView.OnRefreshClickListener mOnRefreshClickListener;

    public void setOnRefreshClickListener(ElevatorView.OnRefreshClickListener listener) {
        mOnRefreshClickListener = listener;
    }

    public ElevatorsAdapter(Context context, List<MDevice> devices) {
        mDevices = devices;
        mContext = context;
        Logger.d(TAG, "ElevatorsAdapter: mDevices size " + mDevices.size());
        for (int i = 0; i < mDevices.size(); i++) {
            ElevatorView view = new ElevatorView(mContext, mDevices.get(i));
            view.setOnRefreshClickListener(mOnRefreshClickListener);
            mViews.add(view);
        }
    }

    public void updateList() {
        Logger.d(TAG, "updateList: deviceList size " + mDevices.size());
        mViews.clear();
        for (int i = 0; i < mDevices.size(); i++) {
            ElevatorView view = new ElevatorView(mContext, mDevices.get(i));
            view.setOnRefreshClickListener(mOnRefreshClickListener);
            mViews.add(view);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    public ElevatorView getItem(int position) {
        if (position >= mViews.size()) {
            Logger.d(TAG, "getItem: out of index of views");
            return null;
        }
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem: " + position);
//        View convertView = null;
////        if (null != recycledViews && !recycledViews.isEmpty()) {
////            convertView = recycledViews.getFirst();
////            recycledViews.removeFirst();
////        }
//
//        if (convertView == null) {
//            convertView = new ElevatorView(mContext, mDevices.get(position));
//        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem: " + position);
        View view = (View) object;
        container.removeView(view);
//        recycledViews.add(view);
    }
}
