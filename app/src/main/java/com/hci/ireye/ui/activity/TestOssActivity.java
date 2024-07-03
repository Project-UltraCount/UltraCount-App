package com.hci.ireye.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.DataGetterOSS;
import com.hci.ireye.data.type.ArduinoEntry;

import java.util.List;
import java.util.SortedMap;

import static com.hci.ireye.util.ThreadUtil.runOnThread;

public class TestOssActivity extends AppCompatActivity {


    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler; // update sts getter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_oss);

        final int delayTime = 21600000; // update every 6 hours
        final Context context = this;
        runOnThread(new Runnable() {
            @Override
            public void run() {
                SortedMap<String, List<ArduinoEntry>> data = DataGetterOSS.getDataFromOSS(context);

                for (String s : data.keySet()) {
                    Log.e("屮", s);

                }
            }
        });
//
//        startHandler(delayTime);
    }
//
//    private Handler taskHandler = new android.os.Handler();
//
//    private Runnable repeatativeTaskRunnable = new Runnable() {
//        public void run() {
//            //getting sts token from the aliyun server
//        }
//    };
//
//    void startHandler(int delay) {
//        taskHandler.postDelayed(repeatativeTaskRunnable, delay);
//    }
//
//    void stopHandler() {
//        taskHandler.removeCallbacks(repeatativeTaskRunnable);
//    }
}