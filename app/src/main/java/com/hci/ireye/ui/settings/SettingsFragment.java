package com.hci.ireye.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hci.ireye.R;
import com.hci.ireye.data.type.ISeekBarValueFormatter;
import com.hci.ireye.ui.eventviewer.EventViewerFragment;

import java.time.Duration;

import static com.hci.ireye.ui.util.MyUtil.formatDurationString;
import static com.hci.ireye.ui.util.MyUtil.getPercentage;
import static com.hci.ireye.ui.util.MyUtil.getValueByRatio;


//
// Created by Lithops on 2020/7/18, 17:25.
//

public class SettingsFragment extends Fragment {

    private Context mContext;
    private SettingsPage mSettingsPage;
    private SharedPreferences mUserPrefs;

    private static final long REFRESH_FREQUENCY_LOWER = 20000;
    private static final long REFRESH_FREQUENCY_UPPER = 10 * 60000;
    private static final long CHARTS_INTERVAL_LOWER = 60000;
    private static final long CHARTS_INTERVAL_UPPER = 10 * 60000;
    private static final long OFFLINE_PERIOD_LOWER = 20000;
    private static final long OFFLINE_PERIOD_UPPER = 60000 * 60 * 24 * 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSettingsPage = view.findViewById(R.id.rv_settings_page);

        init();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mUserPrefs = mContext.getSharedPreferences(getString(R.string.user_prefs), Context.MODE_PRIVATE);
    }

    private void init() {

        mSettingsPage.addSeparatorTab("separator_notification", getString(R.string.notification))
                .addSwitchTab("switch_notification",
                        mContext.getDrawable(R.drawable.ic_notification),
                        getString(R.string.enable_notification),
                        mUserPrefs.getBoolean("notification", true),
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                mUserPrefs.edit().putBoolean(getString(R.string.sp_notification), isChecked).apply();
                                if (!isChecked) {
                                    ((EventViewerFragment)getParentFragment()).mNotificationMaker.cancelCurrentPresent();
                                }
                            }
                        })
                .addSeparatorTab("separator_refresh", getString(R.string.refresh))
                .addSwitchTab("switch_auto_refresh",
                        mContext.getDrawable(R.drawable.ic_refresh),
                        getString(R.string.enable_auto_refresh),
                        mUserPrefs.getBoolean(getString(R.string.sp_auto_refresh), true),
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                mUserPrefs.edit().putBoolean(getString(R.string.sp_auto_refresh), isChecked).apply();
                            }
                        })
                .addSeekBarTab("seek_bar_frequency",
                        mContext.getDrawable(R.drawable.ic_clock),
                        getString(R.string.auto_refresh_frequency),
                        formatDurationString(Duration.ofMillis(REFRESH_FREQUENCY_LOWER)),
                        formatDurationString(Duration.ofMillis(REFRESH_FREQUENCY_UPPER)),
                        getPercentage(REFRESH_FREQUENCY_LOWER, REFRESH_FREQUENCY_UPPER, mUserPrefs.getLong(getString(R.string.sp_refresh_frequency), 20000)),
                        new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                long frequency = getValueByRatio(seekBar.getMin(), seekBar.getMax(), progress, REFRESH_FREQUENCY_LOWER, REFRESH_FREQUENCY_UPPER);
                                mUserPrefs.edit().putLong(getString(R.string.sp_refresh_frequency), frequency).apply();
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        },
                        new ISeekBarValueFormatter() {
                            @Override
                            public String format(int progress, int minProgress, int maxProgress) {
                                long duration = getValueByRatio(minProgress, maxProgress, progress, REFRESH_FREQUENCY_LOWER, REFRESH_FREQUENCY_UPPER);
                                return formatDurationString(Duration.ofMillis(duration));
                            }
                        })
                .addSeparatorTab("separator_charts", getString(R.string.charts))
                .addSeekBarTab("seek_bar_interval",
                        mContext.getDrawable(R.drawable.ic_bar_chart),
                        getString(R.string.chart_time_axis_interval),
                        formatDurationString(Duration.ofMillis(CHARTS_INTERVAL_LOWER)),
                        formatDurationString(Duration.ofMillis(CHARTS_INTERVAL_UPPER)),
                        getPercentage(CHARTS_INTERVAL_LOWER, CHARTS_INTERVAL_UPPER, mUserPrefs.getLong(getString(R.string.sp_charts_interval), 20000)),
                        new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                long interval = getValueByRatio(seekBar.getMin(), seekBar.getMax(), progress, CHARTS_INTERVAL_LOWER, CHARTS_INTERVAL_UPPER);
                                mUserPrefs.edit().putLong(getString(R.string.sp_charts_interval), interval).apply();
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        },
                        new ISeekBarValueFormatter() {
                            @Override
                            public String format(int progress, int minProgress, int maxProgress) {
                                long duration = getValueByRatio(minProgress, maxProgress, progress, CHARTS_INTERVAL_LOWER, CHARTS_INTERVAL_UPPER);
                                return formatDurationString(Duration.ofMillis(duration));
                            }
                        })
                .addSeparatorTab("separator_devices", getString(R.string.devices))
                .addSwitchTab("switch_notify_if_offline",
                        mContext.getDrawable(R.drawable.ic_warning),
                        "Notify if device is offline",
                        mUserPrefs.getBoolean(mContext.getString(R.string.sp_notify_if_offline), true),
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                mUserPrefs.edit().putBoolean(mContext.getString(R.string.sp_notify_if_offline), isChecked).apply();
                                if (!isChecked) {
                                    ((EventViewerFragment)getParentFragment()).mNotificationMaker.cancelAllDeviceOffline();
                                }
                            }
                        })
                .addSeekBarTab("seek_bar_offline_time",
                        mContext.getDrawable(R.drawable.ic_clock),
                        "Device considered offline after inactivity of: ",
                        formatDurationString(Duration.ofMillis(OFFLINE_PERIOD_LOWER)),
                        formatDurationString(Duration.ofMillis(OFFLINE_PERIOD_UPPER)),
                        getPercentage(OFFLINE_PERIOD_LOWER, OFFLINE_PERIOD_UPPER, mUserPrefs.getLong(getString(R.string.sp_offline_period), 20000)),
                        new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                long period = getValueByRatio(seekBar.getMin(), seekBar.getMax(), progress, OFFLINE_PERIOD_LOWER, OFFLINE_PERIOD_UPPER);
                                mUserPrefs.edit().putLong(getString(R.string.sp_offline_period), period).apply();
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        },
                        new ISeekBarValueFormatter() {
                            @Override
                            public String format(int progress, int minProgress, int maxProgress) {
                                long period = getValueByRatio(minProgress, maxProgress, progress, OFFLINE_PERIOD_LOWER, OFFLINE_PERIOD_UPPER);
                                return formatDurationString(Duration.ofMillis(period));
                            }
                        });

    }


}
