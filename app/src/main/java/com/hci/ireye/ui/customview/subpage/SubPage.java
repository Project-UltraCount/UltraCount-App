package com.hci.ireye.ui.customview.subpage;

//
// Created by Lithops on 2020/6/11, 16:24.
//

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.Map;

//used by StatsFragment, ChartsFragment, DeviceFragment.
public class SubPage extends RecyclerView implements SubPageAdapter.IGetTab {

    private SubPageAdapter adapter;
    private Context mContext;

    Map<String, SubPageTabInfo> mTabs = new LinkedHashMap<>();

    public SubPage(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SubPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public SubPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init(){
        adapter = new SubPageAdapter(mContext, this);
        setLayoutManager(new LinearLayoutManager(mContext));
        setAdapter(adapter);
    }

    public SubPage addTab(String tabId, Drawable img, String title, View tabView) {
        SubPageTabInfo info = new SubPageTabInfo(img, title, tabView);
        mTabs.put(tabId, info);
        adapter.notifyDataSetChanged();
        return this;
    }

    public SubPageTabInfo[] getTabs() {
        return mTabs.values().toArray(new SubPageTabInfo[0]);
    }

    public String[] getIds() {
        return mTabs.keySet().toArray(new String[0]);
    }

    public SubPageTabInfo getTab(String tabId) {
        return mTabs.get(tabId);
    }

    public SubPage removeTab(String tabId) {
        if (!mTabs.containsKey(tabId)) return this;
        int pos = -1;
        while(mTabs.keySet().toArray()[++pos] != tabId);
        mTabs.remove(tabId);
        adapter.notifyItemRemoved(pos);
        return this;
    }

    public int getTabCount() {
        return mTabs.size();
    }

    @Override
    public Drawable getImg(int pos) {
        return ((SubPageTabInfo)mTabs.values().toArray()[pos]).img;
    }

    @Override
    public String getTitle(int pos) {
        return ((SubPageTabInfo)mTabs.values().toArray()[pos]).title;
    }

    @Override
    public View getWindowView(int pos) {
        return ((SubPageTabInfo)mTabs.values().toArray()[pos]).windowView;
    }

    @Override
    public int getSize() {
        return mTabs.size();
    }

    public class SubPageTabInfo {
        Drawable img;
        String title;
        View windowView;

        public SubPageTabInfo(Drawable img, String title, View windowView) {
            this.img = img;
            this.title = title;
            this.windowView = windowView;
        }

        public Drawable getImg() {
            return img;
        }

        public String getTitle() {
            return title;
        }

        public View getWindowView() {
            return windowView;
        }

        public void setImg(Drawable img) {
            this.img = img;
            SubPage.this.adapter.notifyDataSetChanged();
        }

        public void setTitle(String title) {
            this.title = title;
            SubPage.this.adapter.notifyDataSetChanged();
        }

        public void setWindowView(View windowView) {
            this.windowView = windowView;
            SubPage.this.adapter.notifyDataSetChanged();
        }
    }
}
