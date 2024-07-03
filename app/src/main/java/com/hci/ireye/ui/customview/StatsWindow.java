package com.hci.ireye.ui.customview;

//
// Created by Lithops on 2020/6/11, 11:22.
//

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hci.ireye.ui.adapter.StatsWindowAdapter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsWindow extends RecyclerView implements StatsWindowAdapter.IGetEntry {

    StatsWindowAdapter adapter;
    private Context mContext;

    private Map<String, StatsEntryInfo> mEntries = new LinkedHashMap<>();


    public StatsWindow(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        adapter = new StatsWindowAdapter(mContext, this);
        setLayoutManager(new GridLayoutManager(mContext, 2)/*new LinearLayoutManager(mContext)*/);
        setAdapter(adapter);
        setNestedScrollingEnabled(false);
    }

    public StatsWindow addEntry(String entryId, String key, int value) {
        return addEntry(entryId, key, value + "");
    }

    public StatsWindow addEntry(String entryId, String key, String value) {
        return addEntry(entryId, key, value, null);
    }

    public StatsWindow addEntry(String entryId, String key, String value, OnClickListener listener) {
        StatsEntryInfo info = new StatsEntryInfo(key, value, listener);
        mEntries.put(entryId, info);
        adapter.notifyDataSetChanged();
        return this;
    }

    public StatsEntryInfo getEntry(String entryId) {
        return mEntries.get(entryId);
    }

    public StatsWindow removeEntry(String entryId) {
        mEntries.remove(entryId);
        adapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public String getKey(int pos) {
        return ((StatsEntryInfo)mEntries.values().toArray()[pos]).key;
    }

    @Override
    public String getValue(int pos) {
        return ((StatsEntryInfo)mEntries.values().toArray()[pos]).value;
    }

    @Override
    public int getSize() {
        return mEntries.size();
    }

    @Override
    public OnClickListener getClickListener(int pos) {
        return ((StatsEntryInfo)mEntries.values().toArray()[pos]).listener;
    }

    public class StatsEntryInfo {
        String key;
        String value;
        OnClickListener listener;

        public StatsEntryInfo(String key, String value, OnClickListener listener) {
            this.key = key;
            this.value = value;
            this.listener = listener;
        }

        public void setKey(String key) {
            this.key = key;
            StatsWindow.this.adapter.notifyDataSetChanged();
        }

        public void setValue(int value) {
            setValue(value + "");
        }

        public void setValue(String value) {
            this.value = value;
            StatsWindow.this.adapter.notifyDataSetChanged();
        }

        public void setListener(OnClickListener listener) {
            this.listener = listener;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public OnClickListener getListener() {
            return listener;
        }
    }




}
