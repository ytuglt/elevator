package com.shaoxia.elevator;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shaoxia.elevator.model.MDevice;

import java.util.List;

/**
 * Created by gonglt1 on 18-1-11.
 */

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder> implements View.OnClickListener {

    private List<MDevice> dataList;
    private OnItemClickListener onItemClickListener;


    public DevicesAdapter(List<MDevice> list) {
        dataList = list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_layout, null);
        MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.itemView.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);
        MDevice mDev = dataList.get(position);
        holder.tVFloor.setText(mDev.getFloor());
        holder.tVId.setText(mDev.getElevatorId());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, (Integer) view.getTag());
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tVFloor;
        public TextView tVId;

        public MyViewHolder(View itemView) {
            super(itemView);

            tVFloor = itemView.findViewById(R.id.floor);
            tVId = itemView.findViewById(R.id.elevator_id);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
