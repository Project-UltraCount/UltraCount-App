package com.hci.ireye.ui.deviceviewer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.DeviceManager;
import com.hci.ireye.data.util.ThreadUtil;


public class DeviceImageFragment extends Fragment {

    private DeviceManager.CountingDevice mDevice;
    private TextView mTvId;
    private ImageView mIvImage;

    private static int counter = 0;

    public int idx;


    public static DeviceImageFragment newInstance(DeviceManager.CountingDevice device) {
        DeviceImageFragment fragment = new DeviceImageFragment();
        Bundle args = new Bundle();
        args.putSerializable("device", device);
        fragment.setArguments(args);
        fragment.idx = counter;
        counter++;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("你", "onCreate: " + idx + getArguments() + mDevice);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = (DeviceManager.CountingDevice) getArguments().getSerializable("device");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("你", "onCreateView: " + idx + getArguments() + mDevice);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("你", "onViewCreated: " + idx + getArguments() + mDevice);
        super.onViewCreated(view, savedInstanceState);

        mTvId = view.findViewById(R.id.tv_device_image_id);
        mIvImage = view.findViewById(R.id.iv_device_image_bg);

        mTvId.setText(mDevice.getDeviceId());

        Log.d("屮", "update: 1");
        update();
    }

    public DeviceManager.CountingDevice getDevice() {
        return mDevice;
    }

    public void update() {
        ThreadUtil.runOnThread(()->{
            if (mDevice == null) return;
            mDevice.fetchOnlineStatus();
            ThreadUtil.runOnUIThread(()-> {
                if (!mDevice.isOnline()) {
                    mIvImage.setImageResource(R.drawable.ic_ultracount_2_offline);
                    mTvId.setTextColor(getResources().getColor(R.color.colorDarkGrey));
                } else {
                    mIvImage.setImageResource(R.drawable.ic_ultracount_2);
                    mTvId.setTextColor(getResources().getColor(R.color.colorMyThemeDark));
                }
            });
        });
    }
}