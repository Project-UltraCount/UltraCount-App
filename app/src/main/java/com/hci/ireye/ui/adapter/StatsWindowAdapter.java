package com.hci.ireye.ui.adapter;

//
// Created by Lithops on 2020/6/5, 22:45.
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

public class StatsWindowAdapter extends RecyclerView.Adapter<StatsWindowAdapter.StatsEntryViewHolder> {

    private Context mContext;
    private IGetEntry mIGetEntry;

    public StatsWindowAdapter(Context context, IGetEntry iGetEntry) {
        mContext = context;
        mIGetEntry = iGetEntry;
    }

    @NonNull
    @Override
    public StatsEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatsEntryViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stats_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatsEntryViewHolder holder, int position) {
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

    class StatsEntryViewHolder extends RecyclerView.ViewHolder {
        private TextView key, value;
        private ImageView canEdit;
        StatsEntryViewHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.tv_stats_entry_key);
            value = itemView.findViewById(R.id.tv_stats_entry_value);
            canEdit = itemView.findViewById(R.id.tv_stats_can_edit);
        }
    }

    public interface IGetEntry {
        String getKey(int pos);
        String getValue(int pos);
        int getSize();
        View.OnClickListener getClickListener(int pos);
    }

}
