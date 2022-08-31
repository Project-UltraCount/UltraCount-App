package com.hci.ireye.ui.deviceviewer;

//
// Created by Lithops on 2020/6/4, 14:50.
//

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.hci.ireye.R;
import com.hci.ireye.ui.customview.MyCustomDialog;
import com.hci.ireye.ui.customview.MyDisplayPanel;
import com.hci.ireye.ui.customview.myinteractionpanel.MyInteractionPanel;
import com.hci.ireye.data.aliyun.DeviceManager;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.eventviewer.ViewPagerAdapter;
import com.hci.ireye.ui.util.ToastUtil;

import java.util.ArrayList;

public class DeviceViewerFragment extends Fragment {

    private Context mContext;

    private DeviceManager mDeviceManager;

    private ViewPager mVpContainer;
    private ViewPagerAdapter mViewPagerAdapter;
    private ImageView mIvLeftArrow, mIvRightArrow;
    private SwipeRefreshLayout mSrlRefresh;

    private MyInteractionPanel mInteractionPanel;
    private MyDisplayPanel mDisplayPanel;


    private SharedPreferences mDevicePrefs, mStatusPrefs, mUserPrefs;
//    private static final int VIEWPAGER_PREVIEW_SIZE = 30, VIEWPAGER_PAGE_MARGIN = 20;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mDevicePrefs = mContext.getSharedPreferences(mContext.getString(R.string.device_prefs), Context.MODE_PRIVATE);
        mStatusPrefs = mContext.getSharedPreferences(mContext.getString(R.string.status_prefs), Context.MODE_PRIVATE);
        mUserPrefs = mContext.getSharedPreferences(mContext.getString(R.string.user_prefs), Context.MODE_PRIVATE);

        mDeviceManager = DeviceManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVpContainer = view.findViewById(R.id.vp_device_viewer_container);
        mIvLeftArrow = view.findViewById(R.id.iv_device_viewer_left_arrow);
        mIvRightArrow = view.findViewById(R.id.iv_device_viewer_right_arrow);
        mSrlRefresh = view.findViewById(R.id.srl_device_viewer_refresh);
        mInteractionPanel = view.findViewById(R.id.device_viewer_interaction_panel);
        mDisplayPanel = view.findViewById(R.id.device_viewer_display_panel);


        mIvRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVpContainer.getCurrentItem() == mVpContainer.getAdapter().getCount() - 1)
                    return;
                mVpContainer.setCurrentItem(mVpContainer.getCurrentItem() + 1);
            }
        });

        mIvLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVpContainer.getCurrentItem() == 0) return;
                mVpContainer.setCurrentItem(mVpContainer.getCurrentItem() - 1);
            }
        });

        updateDeviceSelector();
        // enable viewpager's left&right preview
//        mVpContainer.setClipToPadding(false);
//        mVpContainer.setPadding(MyUtil.dpToPx(VIEWPAGER_PREVIEW_SIZE, getActivity()), 0, MyUtil.dpToPx(VIEWPAGER_PREVIEW_SIZE, getActivity()), 0);
//        mVpContainer.setPageMargin(MyUtil.dpToPx(VIEWPAGER_PAGE_MARGIN, getActivity()));


        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mVpContainer.setAdapter(mViewPagerAdapter);

        mVpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //disable refresh when navigating between pages.
                    mSrlRefresh.setEnabled(positionOffsetPixels == 0);
            }

            @Override
            public void onPageSelected(int position) {
                updateDisplayPanel(getCurrentDevice());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mInteractionPanel.addItem("start", mContext.getDrawable(R.drawable.ic_start), getString(R.string.start), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCustomDialog dialog = new MyCustomDialog(mContext);
                dialog.setTitle(getString(R.string.alert))
                        .setMsg(String.format(getString(R.string.alert_activate_device), getCurrentDevice().getDeviceId()))
                        .setCancel(getString(R.string.cancel), null)
                        .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                            @Override
                            public void onConfirm(MyCustomDialog dialog) {
                                ThreadUtil.runOnThread(() -> {
                                    getCurrentDevice().activate(null, null);
                                });
                            }
                        }).show();

            }
        });

        mInteractionPanel.addItem("stop", mContext.getDrawable(R.drawable.ic_stop), getString(R.string.stop), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCustomDialog dialog = new MyCustomDialog(mContext);
                dialog.setTitle(getString(R.string.alert))
                        .setMsg(String.format(getString(R.string.alert_deactivate_device), getCurrentDevice().getDeviceId()))
                        .setCancel(getString(R.string.cancel), null)
                        .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                            @Override
                            public void onConfirm(MyCustomDialog dialog) {
                                ThreadUtil.runOnThread(() -> {
                                    getCurrentDevice().deactivate();

                                });
                            }
                        }).show();

            }
        });

        mDisplayPanel.setTitleVisibility(View.GONE);
        mDisplayPanel.appendEntry(getString(R.string.ip_address), "N/A")
                .appendEntry(getString(R.string.event_id), "N/A")
                .appendEntry(getString(R.string.online_status), "N/A")
                .appendEntry(getString(R.string.running_status), "N/A")
                .appendEntry(getString(R.string.inflow_outflow_status), "N/A")
                .appendEntry(getString(R.string.oss_connection_status), "N/A");


        mSrlRefresh.setOnRefreshListener(this::update);
        update();
    }

    public void update() {
        if (mContext == null) mContext = getContext();

        updateDeviceSelector();

        mSrlRefresh.setRefreshing(false);
        Log.d("我", "updated!! ");
        ToastUtil.makeToast(mContext, getString(R.string.data_fetch_successful_prompt));

        //notify user if current device may be offline
//        boolean enabled = mUserPrefs.getBoolean(mContext.getString(R.string.sp_notify_if_offline), true);
//        if (enabled && System.currentTimeMillis() - ((EventsManager.CountingEntry) dataSet.get(deviceId).toArray()[0]).time >= mUserPrefs.getLong(getString(R.string.sp_offline_period), 20000)) {
//            ((EventViewerFragment) getParentFragment()).mNotificationMaker.makeDeviceOffline(deviceId);
//        } else {
//            ((EventViewerFragment) getParentFragment()).mNotificationMaker.cancelDeviceOffline(deviceId);
//        }


//        for (String tabId : stillOnline.keySet()) {
//            if (!stillOnline.get(tabId)) {
//                mRvSubPage.removeTab(tabId);
//                ((EventViewerFragment)getParentFragment()).mNotificationMaker.cancelDeviceOffline(tabId);
//            }
//        }

    }

    private void updateDeviceSelector() {
        ThreadUtil.runOnThread(() -> {
            ArrayList<DeviceManager.CountingDevice> devices = mDeviceManager.getAllCountingDevices();
            Log.d("屮", devices.toString());
            ThreadUtil.runOnUIThread(() -> {
                // initialise device selector;
                // never removes any fragment because 1. device removal is very unlikely 2.ViewPager has bugs in removing fragments
                for (DeviceManager.CountingDevice device : devices) {
                    if (!mViewPagerAdapter.hasFragment(device.getDeviceId())) {
                        mViewPagerAdapter.addFragment(device.getDeviceId(), DeviceImageFragment.newInstance(device));
                    } else {
                        Log.d("屮", "2");
                        ((DeviceImageFragment)mViewPagerAdapter.getFragment(device.getDeviceId())).update();
                    }

                }

                // IMPT!! update display panel after device selector rendered - otherwise getCurrentDevice() returns null.
                updateDisplayPanel(getCurrentDevice());

            });
        });
    }

    private void updateDisplayPanel(DeviceManager.CountingDevice device) {
        if (device == null) {
            ToastUtil.makeToast(mContext, getString(R.string.login_failed));
            return;
        }
        ThreadUtil.runOnThread(() -> {
            DeviceManager.CountingDeviceProperties properties = device.getThingModelProperties();
            device.fetchOnlineStatus();
            ThreadUtil.runOnUIThread(() -> {
                mDisplayPanel.updateValue(getString(R.string.ip_address), properties.getIpAddress())
                        .updateValue(getString(R.string.event_id), properties.getEventId())
                        .updateValue(getString(R.string.online_status), device.isOnline() ? getString(R.string.online) : getString(R.string.offline))
                        .updateValue(getString(R.string.running_status), properties.isActivated() ? getString(R.string.activated) : getString(R.string.deactivated))
                        .updateValue(getString(R.string.inflow_outflow_status), properties.isCountingInflow() ? getString(R.string.couting_inflow) : (properties.isCountingOutflow() ? getString(R.string.couting_outflow) : getString(R.string.couting_inflow_outflow)))
                        .updateValue(getString(R.string.oss_connection_status), properties.isOSSConnected() ? getString(R.string.oss_connected) : getString(R.string.oss_disconnected));
            });
        });

    }

    private DeviceManager.CountingDevice getCurrentDevice() {
        Log.d("你", "getCurrentDevice: " + ((DeviceImageFragment) mViewPagerAdapter.getItem(mVpContainer.getCurrentItem())).idx);
        return ((DeviceImageFragment) mViewPagerAdapter.getItem(mVpContainer.getCurrentItem())).getDevice();
    }


}

