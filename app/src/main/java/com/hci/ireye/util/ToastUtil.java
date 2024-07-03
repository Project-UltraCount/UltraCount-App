package com.hci.ireye.util;

//
// Created by Lithops on 2020/5/24, 12:30.
//

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

public class ToastUtil {
    private static Toast mToast;

    public static void makeToast(Context context, String msg, boolean override) {

        //do nothing if this app is in background - to avoid interrupting users
        if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            return;
        }

        if (!override) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else {
            if(mToast == null) {
                mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            } else {
                mToast.setText(msg);
            }
            mToast.show();
        }
    }
    public static void makeToast(Context context, String msg) {
        makeToast(context, msg, true);
    }
}
