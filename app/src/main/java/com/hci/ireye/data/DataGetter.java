package com.hci.ireye.data;

//
// Created by Lithops on 2020/6/6, 21:23.
// Completed by Lithops on 2020/6/13 22:50.
// deprecated. Use DataGetterOSS.

import android.content.Context;
import android.util.Log;

import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.EventsManager;
import com.hci.ireye.data.util.ApacheFtpUtil;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.util.ToastUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

//core method for data transmission
@Deprecated
public class DataGetter {

    // returns <device-id, list<time, count>>.
    public static SortedMap<String, List<EventsManager.CountingEntry>> getDataFromFtp(final Context context) {
        boolean success = false;

        //reconnect if connection has been lost
        try {
            ApacheFtpUtil.ftpsClient.listFiles();
        } catch (Exception e) {
            Log.d("我", "DataGetter: connection has lost. Trying to reconnect.");
            ThreadUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.makeToast(context, context.getString(R.string.reconnect_prompt));
                }
            });
            success = ApacheFtpUtil.connect(ApacheFtpUtil.ftpsClient, ApacheFtpUtil.info);
            if (!success) {
                ThreadUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.makeToast(context, context.getString(R.string.failed_to_reconnect_prompt));
                        Log.d("我", "DataGetter: connection has lost. reconnect failed.");
                    }
                });
                return null;
            }
        }

        //path for storing all record data.
        final String path = context.getFilesDir() + File.separator + "record";
        //make dir if not present
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //record all device name
        List<String> deviceIds = new ArrayList<>();
        //download all files on server
        try {
            //delete previous record
            FileUtils.cleanDirectory(dir);

            //check if no file is present
            //assert empty if only 2 files(dirs) are listed, i.e. . and ..
            if (ApacheFtpUtil.ftpsClient.listFiles().length == 2) {
                ThreadUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.makeToast(context, context.getString(R.string.no_data_on_server_prompt));
                        Log.d("我", "DataGetter: no files currently on server.");
                    }
                });
                return null;
            }

            //start retrieving files
            for (FTPFile file : ApacheFtpUtil.ftpsClient.listFiles()) {
                if (file.isDirectory()) continue;
                String deviceId = file.getName();
                deviceIds.add(deviceId);

                success = ApacheFtpUtil.ftpsClient.retrieveFile(file.getName(), new FileOutputStream(path + File.separator + file.getName()));

                //file retrieved may be incomplete when data is currently writing into it, so manually fail this case.
                if (new File(path + File.separator + file.getName()).length() != file.getSize()) {
                    success = false;
                    Log.d("我", "DataGetter: failed: Retrieved file is incomplete.");
                    ThreadUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.makeToast(context, "data is uploading, auto refresh..");
                        }
                    });
                    //recursive call
                    return getDataFromFtp(context);
                }
                if (!success) break;
            }

        } catch (IOException e) {
            Log.d("我", "DataGetter: exception occurs when downloading files.");
            success = false;
            e.printStackTrace();
        }

        if(!success) {
            Log.d("我", "DataGetter: failed to download files due to exception or retrieveFile() returned false or retrieved file is empty.");
            ThreadUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.makeToast(context, context.getString(R.string.failed_to_fetch_data_prompt), false);
                }
            });
            return null;
        }

        //execute here only if success

        SortedMap<String, List<EventsManager.CountingEntry>> all_data = new TreeMap<>();
        try {
            //for every file (one file for every device)
            long startTimeBkup = context.getSharedPreferences(context.getString(R.string.status_prefs), Context.MODE_PRIVATE).getLong(context.getString(R.string.sp_start_time),0);
            for (String deviceId : deviceIds) {
                File file = new File(path, deviceId);
                ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
                String line;
                List<EventsManager.CountingEntry> device_data = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    String[] v = line.split(" ");

                    try {
                        long time = Long.parseLong(v[0]);
                        int count = Integer.parseInt(v[1]);
                        EventsManager.CountingEntry entry = new EventsManager.CountingEntry(time, count);
                        device_data.add(entry);
                        //ignore all records bef startTime
                        if (time < startTimeBkup) break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("我", "getDataFromFtp: file content format error: cannot parse value or value is missing");
                        ThreadUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.makeToast(context, context.getString(R.string.malformed_file_prompt));
                            }
                        });
                        return null;
                    }

                }
                all_data.put(deviceId.split("\\.")[0], device_data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("我", "DataGetter: data retrieved successfully");
        ThreadUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.makeToast(context, context.getString(R.string.data_fetch_successful_prompt));
            }
        });
        return all_data;
    }
}

