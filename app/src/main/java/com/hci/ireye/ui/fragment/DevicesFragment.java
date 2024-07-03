package com.hci.ireye.ui.fragment;

//
// Created by Lithops on 2020/6/4, 14:50.
//

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrinterId;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hci.ireye.R;
import com.hci.ireye.data.type.ArduinoEntry;
import com.hci.ireye.ui.activity.MainInterfaceActivity;
import com.hci.ireye.ui.customview.DevicesWindow;
import com.hci.ireye.ui.customview.SubPage;
import com.hci.ireye.widget.MyCustomDialog;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesFragment extends Fragment {

    private Context mContext;
    private SubPage mRvSubPage;
    private SharedPreferences mDevicePrefs, mStatusPrefs, mUserPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvSubPage = view.findViewById(R.id.rv_devices_page);
        init();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mDevicePrefs = mContext.getSharedPreferences(mContext.getString(R.string.device_prefs), Context.MODE_PRIVATE);
        mStatusPrefs = mContext.getSharedPreferences(mContext.getString(R.string.status_prefs), Context.MODE_PRIVATE);
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);
    }

    public void init() {
        //no need to init anything
    }

    public void update(Map<String, List<ArduinoEntry>> dataSet) {
        if (mContext == null) mContext = getContext();
        Map<String, Boolean> stillOnline = new HashMap<>();
        for (String tabId : mRvSubPage.getIds()) {
            stillOnline.put(tabId, false);
        }

        if (dataSet != null) {
            for (final String deviceId : dataSet.keySet()) {
                String lastUpdate, relativeLastUpdate;
                int count;
                String position;

                if (dataSet.get(deviceId).size() == 0) continue;

                count = ((ArduinoEntry) dataSet.get(deviceId).toArray()[0]).count - ((ArduinoEntry) dataSet.get(deviceId).toArray()[dataSet.get(deviceId).size() - 1]).count;
                relativeLastUpdate = DateUtils.getRelativeTimeSpanString(((ArduinoEntry) dataSet.get(deviceId).toArray()[0]).time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                DateFormat format = DateFormat.getDateTimeInstance();
                lastUpdate = format.format(new Date(((ArduinoEntry) dataSet.get(deviceId).toArray()[0]).time)) + "\n(" + relativeLastUpdate + ")";
                //have not configured this device, set true by default
                //false for counting outflow, true for counting inflow
                if (!mDevicePrefs.contains(deviceId)) {
                    mDevicePrefs.edit().putBoolean(deviceId, true).apply();
                }

                position = (mDevicePrefs.getBoolean(deviceId, true) ? mContext.getString(R.string.entrance) : mContext.getString(R.string.exit));

                if (mRvSubPage.getTab(deviceId) != null) {
                    stillOnline.put(deviceId, true);

                    DevicesWindow window = ((DevicesWindow) mRvSubPage.getTab(deviceId).getWindowView());
                    window.getEntry("id").setValue(deviceId);
                    window.getEntry("update").setValue(lastUpdate);
                    window.getEntry("count").setValue(count);
                    window.getEntry("position").setValue(position);
                    mRvSubPage.getTab(deviceId).setTitle(mContext.getString(R.string.device) + " " + deviceId);

                } else {
                    final DevicesWindow window = new DevicesWindow(mContext);
                    window.addEntry("id", mContext.getString(R.string.id), deviceId)
                            .addEntry("update", mContext.getString(R.string.last_update), lastUpdate)
                            .addEntry("count", mContext.getString(R.string.total_count), count)
                            .addEntry("position", mContext.getString(R.string.position), position, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MyCustomDialog dialog = new MyCustomDialog(mContext);

                                    dialog.setTitle(mContext.getString(R.string.change_position))
                                            .setMsg(String.format(mContext.getString(R.string.change_position_msg), deviceId, (mDevicePrefs.getBoolean(deviceId, true) ? mContext.getString(R.string.exit) : mContext.getString(R.string.entrance))))
                                            .setCancel(mContext.getString(R.string.cancel), null)
                                            .setConfirm(mContext.getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                                                @Override
                                                public void onConfirm(MyCustomDialog dialog) {
                                                    mDevicePrefs.edit().putBoolean(deviceId, !mDevicePrefs.getBoolean(deviceId, true)).apply();
                                                    ((DevicesWindow) mRvSubPage.getTab(deviceId).getWindowView()).getEntry("position").setValue(mDevicePrefs.getBoolean(deviceId, true) ? mContext.getString(R.string.entrance) : mContext.getString(R.string.exit));
                                                    //auto refresh if position is changed during counting.
                                                    if (mStatusPrefs.getBoolean(mContext.getString(R.string.sp_ongoing), false)) {
                                                        ((MainInterfaceActivity) mContext).refresh();
                                                    }
                                                }
                                            }).show();
                                }
                            });

                    mRvSubPage.addTab(deviceId, getResources().getDrawable(R.drawable.ic_menu_arduino), getString(R.string.device) + " " + deviceId, window);
                }

                //notify user if current device may be offline
                boolean enabled = mUserPrefs.getBoolean(mContext.getString(R.string.sp_notify_if_offline), true);
                if (enabled && System.currentTimeMillis() - ((ArduinoEntry) dataSet.get(deviceId).toArray()[0]).time >= mUserPrefs.getLong(getString(R.string.sp_offline_period), 20000)) {
                    ((MainInterfaceActivity) mContext).mNotificationMaker.makeDeviceOffline(deviceId);
                } else {
                    ((MainInterfaceActivity) mContext).mNotificationMaker.cancelDeviceOffline(deviceId);
                }
            }
        }

        for (String tabId : stillOnline.keySet()) {
            if (!stillOnline.get(tabId)) {
                mRvSubPage.removeTab(tabId);
                ((MainInterfaceActivity) mContext).mNotificationMaker.cancelDeviceOffline(tabId);
            }
        }

    }

    public void stop() {

    }

}

