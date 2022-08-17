package com.hci.ireye.ui.eventviewer.eventstats;


import static com.hci.ireye.ui.util.MyUtil.dpToPx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hci.ireye.R;
import com.hci.ireye.ui.customview.MyDisplayPanel;
import com.hci.ireye.data.aliyun.EventsManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;


//
// Created by Lithops on 2020/6/4, 14:50.
//

public class EventStatsFragment extends Fragment {

    //ui
    private Context mContext;
    private MyDisplayPanel mDisplayPanelSummary;
    private TreeMap<String, MyDisplayPanel> mDisplayPanelDevices = new TreeMap<>();

    private LinearLayout mDisplayPanelDevicesContainer;

    private SharedPreferences mStatusPrefs, mDevicePrefs;

    private EventsManager.CountingEvent mEvent;

    //transfer data to main activity (display on top circle)
//    private IOnUpdateStats updateListener;


    public static EventStatsFragment newInstance(EventsManager.CountingEvent event) {

        Bundle args = new Bundle();
        args.putSerializable("event", event);

        EventStatsFragment fragment = new EventStatsFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDisplayPanelSummary = view.findViewById(R.id.event_stats_summary_container);
        mDisplayPanelDevicesContainer = view.findViewById(R.id.ll_event_stats_devices_container);

        mEvent = (EventsManager.CountingEvent) getArguments().getSerializable("event");

        mDisplayPanelSummary.setTitle(getString(R.string.summary))
                .appendEntry(getString(R.string.guests_present), "N/A")
                .appendEntry(getString(R.string.guests_arrived), "N/A")
                .appendEntry(getString(R.string.guests_left), "N/A")
                .appendEntry(getString(R.string.start_time), SimpleDateFormat.getDateTimeInstance().format(new Date(mEvent.getStartTime() * 1000L)).replace(' ', '\n'));

        for (String deviceId : mEvent.getDeviceIds()) {
            MyDisplayPanel newDisplayPanel = new MyDisplayPanel(mContext);

            newDisplayPanel.setTitle(deviceId)
                    .appendEntry(getString(R.string.inflow), "N/A")
                    .appendEntry(getString(R.string.outflow), "N/A")
                    .appendEntry(getString(R.string.netflow), "N/A");

            mDisplayPanelDevices.put(deviceId, newDisplayPanel);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(dpToPx(10, mContext), 0, dpToPx(10, mContext), dpToPx(10, mContext));

            mDisplayPanelDevicesContainer.addView(newDisplayPanel, layoutParams);

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mStatusPrefs = mContext.getSharedPreferences(getString(R.string.status_prefs), Context.MODE_PRIVATE);
        mDevicePrefs = mContext.getSharedPreferences(getString(R.string.device_prefs), Context.MODE_PRIVATE);
    }

    //called when new data is successfully retrieved
    public void update(EventsManager.CountingDataSet dataSet) {
        if (mContext == null) mContext = getContext();

        mDisplayPanelSummary.updateValue(getString(R.string.guests_present), dataSet.getTotalNetflow())
                .updateValue(getString(R.string.guests_arrived), dataSet.getTotalInflow())
                .updateValue(getString(R.string.guests_left), dataSet.getTotalOutflow());

        for (String deviceId : dataSet.getDeviceIds()) {
            MyDisplayPanel panel = mDisplayPanelDevices.get(deviceId);
            assert panel != null;

            panel.updateValue(getString(R.string.inflow), dataSet.getTotalInflow(deviceId))
                    .updateValue(getString(R.string.outflow), dataSet.getTotalOutflow(deviceId))
                    .updateValue(getString(R.string.netflow), dataSet.getTotalNetflow(deviceId));
        }

    }

    //called when counting is ended.
    public void stop() {
    }


}
