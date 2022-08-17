package com.hci.ireye.data.util;

//
// Created by Lithops on 2020/5/24, 12:30.
//


import android.os.Handler;

public class ThreadUtil {
    public static Thread runOnThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    private static Handler mHandler = new Handler();//this handler is on main thread

    public static void runOnUIThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
