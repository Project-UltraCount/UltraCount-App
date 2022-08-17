package com.hci.ireye.ui.newevent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.DeviceManager;
import com.hci.ireye.data.aliyun.EventsManager;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.customview.MyCustomDialog;
import com.hci.ireye.ui.util.MyUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class NewEventActivity extends AppCompatActivity {

    private EditText mEtEventName;
    private LinearLayout mLLDeviceList;
    private Button mBtnConfirm;
    private LinkedHashMap<LinearLayout, DeviceManager.CountingDevice> mViewToDevices = new LinkedHashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);


        mEtEventName = findViewById(R.id.et_new_event_name);
        mLLDeviceList = findViewById(R.id.ll_new_events_devices_container);
        mBtnConfirm = findViewById(R.id.btn_new_event_confirm);

        // add devices
        ThreadUtil.runOnThread(() -> {
            ArrayList<DeviceManager.CountingDevice> devices = DeviceManager.getInstance().getAllCountingDevices();
            devices.forEach(DeviceManager.CountingDevice::fetchOnlineStatus);

            ThreadUtil.runOnUIThread(() -> {
                for (DeviceManager.CountingDevice device : devices) {

                    LinearLayout choice = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_new_event_choose_device, mLLDeviceList, false);
                    TextView title = choice.findViewById(R.id.tv_item_new_event_choose_device_name);
                    title.setTextColor(getColor(R.color.colorDarkGrey));
                    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.size_text_big_18sp));

                    String t = device.getDeviceId();
                    if (!device.isOnline()) t += "(" + getString(R.string.offline) + ")";
                    title.setText(t);

                    mLLDeviceList.addView(choice);

                    mViewToDevices.put(choice, device);
                }
            });
        });

        mBtnConfirm.setOnClickListener(v -> {
            // gather user input
            String eventName = mEtEventName.getText().toString();
            LinkedHashMap<DeviceManager.CountingDevice, Integer> chosenDevicesToIOStatus = new LinkedHashMap<>();

            for (Map.Entry<LinearLayout, DeviceManager.CountingDevice> entry : mViewToDevices.entrySet()) {
                LinearLayout choice = entry.getKey();
                CheckBox inflow = choice.findViewById(R.id.cb_item_new_event_choose_device_inflow);
                CheckBox outflow = choice.findViewById(R.id.cb_item_new_event_choose_device_outflow);

                DeviceManager.CountingDevice device = entry.getValue();


                if (inflow.isChecked() || outflow.isChecked()) {

                    int inflowOutflowStatus = inflow.isChecked() ? (outflow.isChecked() ? 3 : 1) : 2;
                    chosenDevicesToIOStatus.put(device, inflowOutflowStatus);
                }
            }

            ArrayList<String> dnames = new ArrayList<>();
            for (DeviceManager.CountingDevice device : chosenDevicesToIOStatus.keySet()) dnames.add(device.getDeviceId());
            String d = String.join(", ", dnames);

            // show dialog
            new MyCustomDialog(NewEventActivity.this)
                    .setTitle(getString(R.string.alert))
                    .setCancel(getString(R.string.cancel), null)
                    .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                        @Override
                        public void onConfirm(MyCustomDialog dialog) {
                            EventsManager eventsManager = EventsManager.getInstance();
                            ThreadUtil.runOnThread(() -> eventsManager.startNewEvent(NewEventActivity.this, eventName, chosenDevicesToIOStatus));
                            finish();
                        }
                    })
                    .setMsg(String.format(getString(R.string.alert_new_event), eventName, d))
                    .show();

        });
    }
}