package com.hci.ireye.ui.deviceviewer;

//
// Created by Lithops on 2020/6/14, 14:37.
//

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.Map;

public class DevicesWindow extends RecyclerView implements DevicesWindowAdapter.IGetEntry {

    public DevicesWindowAdapter adapter;
    private Context mContext;

    private Map<String, DevicesEntryInfo> mEntries = new LinkedHashMap<>();

    public DevicesWindow(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        adapter = new DevicesWindowAdapter(mContext, this);
        setLayoutManager(new GridLayoutManager(mContext, 2));
        setAdapter(adapter);
        setNestedScrollingEnabled(false);
    }

    public DevicesWindow addEntry(String entryId, String key, int value) {
        return addEntry(entryId, key, value + "");
    }

    public DevicesWindow addEntry(String entryId, String key, String value) {
        return addEntry(entryId, key, value, null);
    }

    public DevicesWindow addEntry(String entryId, String key, String value, OnClickListener listener) {
        DevicesEntryInfo info = new DevicesEntryInfo(key, value, listener);
        mEntries.put(entryId, info);
        adapter.notifyDataSetChanged();
        return this;
    }

    public DevicesEntryInfo getEntry(String entryId) {
        return mEntries.get(entryId);
    }

    public DevicesWindow removeEntry(String entryId) {
        mEntries.remove(entryId);
        adapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public String getKey(int pos) {
        return ((DevicesEntryInfo)mEntries.values().toArray()[pos]).key;
    }

    @Override
    public String getValue(int pos) {
        return ((DevicesEntryInfo)mEntries.values().toArray()[pos]).value;
    }

    @Override
    public int getSize() {
        return mEntries.size();
    }

    @Override
    public OnClickListener getClickListener(int pos) {
        return ((DevicesEntryInfo)mEntries.values().toArray()[pos]).listener;
    }

    public class DevicesEntryInfo {
        String key;
        String value;
        OnClickListener listener;

        public DevicesEntryInfo(String key, String value, OnClickListener listener) {
            this.key = key;
            this.value = value;
            this.listener = listener;
        }

        public void setKey(String key) {
            this.key = key;
            DevicesWindow.this.adapter.notifyDataSetChanged();
        }

        public void setValue(int value) {
            setValue(value + "");
        }

        public void setValue(String value) {
            this.value = value;
            DevicesWindow.this.adapter.notifyDataSetChanged();
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
