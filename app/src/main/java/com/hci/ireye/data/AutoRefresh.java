package com.hci.ireye.data;

//
// Created by Lithops on 2020/7/17, 21:53.
//

import android.content.Context;
import android.content.SharedPreferences;

import com.hci.ireye.R;
import com.hci.ireye.ui.activity.MainInterfaceActivity;
import com.hci.ireye.util.ThreadUtil;

public class AutoRefresh {
    private MainInterfaceActivity mContext;
    private SharedPreferences mUserPrefs, mStatusPrefs;

    public AutoRefresh(MainInterfaceActivity context) {
        mContext = context;
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);
        mStatusPrefs = mContext.getSharedPreferences(mContext.getString(R.string.status_prefs), Context.MODE_PRIVATE);
    }

    public void run() {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long lastRefresh = mStatusPrefs.getLong(mContext.getString(R.string.sp_last_refresh), 0);
                    boolean ongoing = mStatusPrefs.getBoolean(mContext.getString(R.string.sp_ongoing), false);
                    boolean enabled = mUserPrefs.getBoolean(mContext.getString(R.string.sp_auto_refresh), true);
                    long interval = mUserPrefs.getLong(mContext.getString(R.string.sp_refresh_frequency), 100000);

                    if (ongoing && enabled && System.currentTimeMillis() - lastRefresh >= interval) {
                        ThreadUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mContext.refresh();
                            }
                        });
                    }
                }
            }
        });
    }
}
