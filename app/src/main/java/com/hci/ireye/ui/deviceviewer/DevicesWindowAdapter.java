package com.hci.ireye.ui.deviceviewer;

//
// Created by Lithops on 2020/6/14, 14:43.
//

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hci.ireye.R;

public class DevicesWindowAdapter extends RecyclerView.Adapter<DevicesWindowAdapter.DevicesWindowViewHolder> {

    private Context mContext;
    private IGetEntry mIGetEntry;

    public DevicesWindowAdapter(Context context, IGetEntry iGetEntry) {
        super();
        mContext = context;
        mIGetEntry = iGetEntry;
    }

    @NonNull
    @Override
    public DevicesWindowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DevicesWindowViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_devices_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesWindowViewHolder holder, int position) {
        holder.key.setText(mIGetEntry.getKey(position));
        holder.value.setText(mIGetEntry.getValue(position));
        if (mIGetEntry.getClickListener(position) != null) {
            holder.canEdit.setOnClickListener(mIGetEntry.getClickListener(position));
            holder.canEdit.setVisibility(View.VISIBLE);
        } else {
            holder.canEdit.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mIGetEntry.getSize();
    }


    class DevicesWindowViewHolder extends RecyclerView.ViewHolder {
        private TextView key, value;
        private ImageView canEdit;
        DevicesWindowViewHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.tv_devices_entry_key);
            value = itemView.findViewById(R.id.tv_devices_entry_value);
            canEdit = itemView.findViewById(R.id.tv_devices_can_edit);
        }
    }

    public interface IGetEntry {
        String getKey(int pos);
        String getValue(int pos);
        int getSize();
        View.OnClickListener getClickListener(int pos);
    }
}
