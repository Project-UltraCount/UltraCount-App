package com.hci.ireye.ui.eventviewer.eventcharts;

//
// Created by Lithops on 2020/6/26, 10:00.
//

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.Chart;
import com.hci.ireye.R;

public class EventChartsWindowAdapter extends RecyclerView.Adapter<EventChartsWindowAdapter.ChartsEntryViewHolder> {

    private Context mContext;
    private IGetEntry mIGetEntry;

    public EventChartsWindowAdapter(Context context, IGetEntry iGetEntry) {
        mContext = context;
        mIGetEntry = iGetEntry;
    }

    @NonNull
    @Override
    public ChartsEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChartsEntryViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_eventviewer_charts_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChartsEntryViewHolder holder, int position) {
        if (holder.chartFrame.getChildCount() > 0) {
            holder.chartFrame.removeAllViews();
        }
        holder.chartFrame.addView(mIGetEntry.getChart(position));
        holder.chartTitle.setText(mIGetEntry.getTitle(position));
    }

    @Override
    public int getItemCount() {
        return mIGetEntry.getSize();
    }

    class ChartsEntryViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout chartFrame;
        private TextView chartTitle;
        ChartsEntryViewHolder(View itemView) {
            super(itemView);
            chartFrame = itemView.findViewById(R.id.fl_charts_entry_frame);
            chartTitle = itemView.findViewById(R.id.tv_charts_entry_title);
        }
    }

    public interface IGetEntry {
        Chart getChart(int pos);
        String getTitle(int pos);
        int getSize();
    }
 }
