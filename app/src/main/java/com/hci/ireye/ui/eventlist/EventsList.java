package com.hci.ireye.ui.eventlist;

//
// Created by Lithops on 2022/6/2, 18:32.
//

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventsList extends RecyclerView implements EventsListAdapter.IGetEntry{

    public EventsListAdapter adapter;
    private Context mContext;

    private Map<String, EventsEntryInfo> mEntries = new LinkedHashMap<>();

    public EventsList(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public EventsList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public EventsList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        adapter = new EventsListAdapter(mContext, this);
        setLayoutManager(new LinearLayoutManager(mContext));
        setAdapter(adapter);
        setNestedScrollingEnabled(false);
    }

    public EventsList addEntry(String entryId, String eventName, int startTime, OnClickListener listener) {
        EventsList.EventsEntryInfo info = new EventsEntryInfo(eventName, startTime, listener);
        mEntries.put(entryId, info);
        adapter.notifyDataSetChanged();
        return this;
    }

    public EventsEntryInfo getEntry(String entryId) {
        return mEntries.get(entryId);
    }

    public EventsList removeEntry(String entryId) {
        mEntries.remove(entryId);
        adapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public String getEventName(int pos) {
        return ((EventsEntryInfo)mEntries.values().toArray()[pos]).eventName;
    }

    @Override
    public int getStartTime(int pos) {
        return ((EventsEntryInfo)mEntries.values().toArray()[pos]).startTime;
    }

    @Override
    public int getSize() {
        return mEntries.size();
    }

    @Override
    public OnClickListener getClickListener(int pos) {
        return ((EventsEntryInfo)mEntries.values().toArray()[pos]).listener;
    }

    public class EventsEntryInfo {
        String eventName;
        int startTime;
        OnClickListener listener;

        public EventsEntryInfo(String eventName, int startTime, OnClickListener listener) {
            this.eventName = eventName;
            this.startTime = startTime;
            this.listener = listener;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
            EventsList.this.adapter.notifyDataSetChanged();
        }

        public void setStartTime(int startTime) {
            this.startTime = startTime;
            EventsList.this.adapter.notifyDataSetChanged();
        }

        public void setListener(OnClickListener listener) {
            this.listener = listener;
        }

        public OnClickListener getListener() {
            return listener;
        }
    }


}
