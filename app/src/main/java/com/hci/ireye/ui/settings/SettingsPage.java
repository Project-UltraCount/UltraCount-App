package com.hci.ireye.ui.settings;

//
// Created by Lithops on 2020/7/18, 17:44.
//

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hci.ireye.data.type.ISeekBarValueFormatter;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsPage extends RecyclerView implements SettingsPageAdapter.IGetTab {

    private SettingsPageAdapter adapter;
    private Context mContext;

    private Map<String, Object> mTabs = new LinkedHashMap<>();

    public SettingsPage(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SettingsPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public SettingsPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        adapter = new SettingsPageAdapter(mContext, this);
        setLayoutManager(new LinearLayoutManager(mContext));
        setAdapter(adapter);
    }

    public SettingsPage addSwitchTab(String tabId, Drawable img, String title, boolean initialChecked, CompoundButton.OnCheckedChangeListener listener) {
        mTabs.put(tabId, new SwitchTabInfo(img, title, initialChecked, listener));
        adapter.notifyDataSetChanged();
        return this;
    }

    public SettingsPage addSeekBarTab(String tabId, Drawable img, String title, String lower, String upper, double initialProgress, SeekBar.OnSeekBarChangeListener listener, ISeekBarValueFormatter formatter) {
        mTabs.put(tabId, new SeekBarTabInfo(img, title, lower, upper, initialProgress, listener, formatter));
        adapter.notifyDataSetChanged();
        return this;
    }

    public SettingsPage addSeparatorTab(String tabId, String title) {
        mTabs.put(tabId, new SeparatorTabInfo(title));
        adapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public int getSize() {
        return mTabs.size();
    }

    @Override
    public int getType(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeparatorTabInfo) return SettingsPageAdapter.TYPE_SEPARATOR;
        if (value instanceof SwitchTabInfo) return SettingsPageAdapter.TYPE_SWITCH;
        if (value instanceof SeekBarTabInfo) return SettingsPageAdapter.TYPE_SEEK_BAR;
        throw new IllegalStateException("");
    }

    @Override
    public String getTitle(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeparatorTabInfo) return ((SeparatorTabInfo)value).title;
        if (value instanceof SwitchTabInfo) return ((SwitchTabInfo)value).title;
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).title;
        throw new IllegalStateException("");
    }

    @Override
    public Drawable getImg(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SwitchTabInfo) return ((SwitchTabInfo)value).img;
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).img;
        throw new IllegalStateException("");
    }

    @Override
    public CompoundButton.OnCheckedChangeListener getSwitchListener(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SwitchTabInfo) return ((SwitchTabInfo)value).listener;
        throw new IllegalStateException("");
    }

    @Override
    public SeekBar.OnSeekBarChangeListener getSeekBarListener(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).listener;
        throw new IllegalStateException("");
    }

    @Override
    public String getLowerBound(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).lower;
        throw new IllegalStateException("");
    }

    @Override
    public String getUpperBound(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).upper;
        throw new IllegalStateException("");
    }

    @Override
    public boolean getSwitchChecked(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SwitchTabInfo) return ((SwitchTabInfo)value).initialChecked;
        throw new IllegalStateException("");
    }

    @Override
    public double getSeekBarInitialPercentage(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).initialPercentage;
        throw new IllegalStateException("");
    }

    @Override
    public ISeekBarValueFormatter getSeekBarValueFormatter(int pos) {
        Object value = mTabs.values().toArray()[pos];
        if (value instanceof SeekBarTabInfo) return ((SeekBarTabInfo)value).formatter;
        throw new IllegalStateException("");
    }

    public class SwitchTabInfo {
        Drawable img;
        String title;
        boolean initialChecked;
        CompoundButton.OnCheckedChangeListener listener;

        public SwitchTabInfo(Drawable img, String title, boolean initialChecked, CompoundButton.OnCheckedChangeListener listener) {
            this.img = img;
            this.title = title;
            this.initialChecked = initialChecked;
            this.listener = listener;
        }
    }

    public class SeekBarTabInfo {
        Drawable img;
        String title, lower, upper;
        double initialPercentage;
        SeekBar.OnSeekBarChangeListener listener;
        ISeekBarValueFormatter formatter;

        public SeekBarTabInfo(Drawable img, String title, String lower, String upper, double initialPercentage, SeekBar.OnSeekBarChangeListener listener, ISeekBarValueFormatter formatter) {
            this.img = img;
            this.title = title;
            this.lower = lower;
            this.upper = upper;
            this.initialPercentage = initialPercentage;
            this.listener = listener;
            this.formatter = formatter;
        }
    }

    public class SeparatorTabInfo {
        String title;

        public SeparatorTabInfo(String title) {
            this.title = title;
        }
    }
}
