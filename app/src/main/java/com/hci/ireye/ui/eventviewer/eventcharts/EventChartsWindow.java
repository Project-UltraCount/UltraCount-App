package com.hci.ireye.ui.eventviewer.eventcharts;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.Chart;

import java.util.LinkedHashMap;
import java.util.Map;


//
// Created by Lithops on 2020/6/26, 9:57.
//

public class EventChartsWindow extends RecyclerView implements EventChartsWindowAdapter.IGetEntry {

    EventChartsWindowAdapter adapter;
    private Context mContext;

    private Map<String, ChartsEntryInfo> mEntries = new LinkedHashMap<>();

    public EventChartsWindow(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        adapter = new EventChartsWindowAdapter(mContext, this);
        setLayoutManager(new LinearLayoutManager(mContext));
        setAdapter(adapter);
        setNestedScrollingEnabled(false);
    }

    public EventChartsWindow addEntry(String entryId, Chart chart, String title) {
        ChartsEntryInfo info = new ChartsEntryInfo(chart, title);
        mEntries.put(entryId, info);
        adapter.notifyDataSetChanged();
        return this;
    }

    public ChartsEntryInfo getEntry(String entryId) {
        return mEntries.get(entryId);
    }

    public EventChartsWindow removeEntry(String entryId) {
        mEntries.remove(entryId);
        adapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public Chart getChart(int pos) {
        return ((ChartsEntryInfo)mEntries.values().toArray()[pos]).chart;
    }

    @Override
    public String getTitle(int pos) {
        return ((ChartsEntryInfo) mEntries.values().toArray()[pos]).title;
    }

    @Override
    public int getSize() {
        return mEntries.size();
    }

    public class ChartsEntryInfo {
        Chart chart;
        String title;

        public ChartsEntryInfo(Chart chart, String title) {
            this.chart = chart;
            this.title = title;
        }

        public void setChart(Chart chart) {
            this.chart = chart;
            EventChartsWindow.this.adapter.notifyDataSetChanged();
        }

        public void setTitle(String title) {
            this.title = title;
            EventChartsWindow.this.adapter.notifyDataSetChanged();
        }

        public Chart getChart() {
            return chart;
        }

        public String getTitle() {
            return title;
        }
    }

}
