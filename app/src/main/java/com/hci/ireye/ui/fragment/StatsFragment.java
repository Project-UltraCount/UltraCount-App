package com.hci.ireye.ui.fragment;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hci.ireye.R;
import com.hci.ireye.data.type.ArduinoEntry;
import com.hci.ireye.ui.activity.MainInterfaceActivity;
import com.hci.ireye.ui.customview.StatsWindow;
import com.hci.ireye.ui.customview.SubPage;
import com.hci.ireye.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


//
// Created by Lithops on 2020/6/4, 14:50.
//

public class StatsFragment extends Fragment {

    //ui
    private Context mContext;
    private SubPage mRvSubPage;

    private SharedPreferences mStatusPrefs, mDevicePrefs;

    //transfer data to main activity (display on top circle)
    private IOnUpdateStats updateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvSubPage = view.findViewById(R.id.rv_stats_page);

        init();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mStatusPrefs = mContext.getSharedPreferences(getString(R.string.status_prefs), Context.MODE_PRIVATE);
        mDevicePrefs = mContext.getSharedPreferences(getString(R.string.device_prefs), Context.MODE_PRIVATE);
        try {
            updateListener = (IOnUpdateStats)context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    //called when drawing ui at the start of app
    public void init() {

        StatsWindow window = new StatsWindow(mContext);

        final Calendar calendar = Calendar.getInstance();
        window.addEntry("startTime", getString(R.string.start_time), "N/A", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(mContext, R.style.DateTimePicker, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        new TimePickerDialog(mContext, R.style.DateTimePicker, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                long newTime = new Date(year - 1900, month, dayOfMonth, hourOfDay, minute).getTime();
                                if (newTime > System.currentTimeMillis()) {
                                    ToastUtil.makeToast(mContext, getString(R.string.set_to_future_forbidden_prompt));
                                    return;
                                }
                                mStatusPrefs.edit().putLong(getString(R.string.sp_start_time), newTime).apply();
                                ((MainInterfaceActivity)mContext).refresh();
                                ((MainInterfaceActivity)mContext).setupTimer();
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(mContext))
                                .show();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        })
                .addEntry("lastRefresh", getString(R.string.last_refresh), "N/A")
                .addEntry("present", getString(R.string.guests_present), "N/A")
                .addEntry("left", getString(R.string.guests_left), "N/A")
                .addEntry("arrived", getString(R.string.guests_arrived), "N/A");

        mRvSubPage.addTab("stats", getResources().getDrawable(R.drawable.ic_menu_stat), getString(R.string.stats), window);

    }

    //called when new data is successfully retrieved
    //returns currentPresent for use by notification
    public int update(Map<String, List<ArduinoEntry>> dataSet) {
        if (mContext == null) mContext = getContext();
        //do not respond if counting not started
        if (!mStatusPrefs.getBoolean(getString(R.string.sp_ongoing), false)) return 0;

        int newPresent = 0, newLeft = 0, newArrived = 0;
        String lastRefresh = SimpleDateFormat.getDateTimeInstance().format(mStatusPrefs.getLong(getString(R.string.sp_last_refresh), 0));

        if (dataSet != null) {
            for (String deviceId : dataSet.keySet()) {
                //no entries from this device
                if (dataSet.get(deviceId).size() == 0) continue;

                int count = ((ArduinoEntry)dataSet.get(deviceId).toArray()[0]).count - ((ArduinoEntry)dataSet.get(deviceId).toArray()[dataSet.get(deviceId).size() - 1]).count;
                long time = ((ArduinoEntry)dataSet.get(deviceId).toArray()[0]).time;

                if (!mDevicePrefs.getBoolean(deviceId, true)) {
                    newPresent -= count;
                    newLeft += count;
                } else if (mDevicePrefs.getBoolean(deviceId, true)) {
                    newPresent += count;
                    newArrived += count;
                }

            }

        }

        updateListener.onUpdate(newPresent);

        StatsWindow window = (StatsWindow)mRvSubPage.getTab("stats").getWindowView();
        window.getEntry("lastRefresh").setValue(lastRefresh);
        window.getEntry("present").setValue(newPresent);
        window.getEntry("left").setValue(newLeft);
        window.getEntry("arrived").setValue(newArrived);
        java.text.DateFormat format = SimpleDateFormat.getDateTimeInstance();
        window.getEntry("startTime").setValue(format.format(new Date(mStatusPrefs.getLong("startTime", 0))));

        return newPresent;
    }

    //called when counting is ended.
    public void stop() {
    }

    //called when data is updated, to update the data shown in big circle.
    public interface IOnUpdateStats {
        void onUpdate(int newPresent);
    }


}
