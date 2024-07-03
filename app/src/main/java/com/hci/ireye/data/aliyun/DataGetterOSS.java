package com.hci.ireye.data.aliyun;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.hci.ireye.R;
import com.hci.ireye.data.type.ArduinoEntry;
import com.hci.ireye.util.ThreadUtil;
import com.hci.ireye.util.ToastUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.hci.ireye.data.aliyun.STSTokenGetter.getSTSToken;

public class DataGetterOSS {
    // returns <device-id, list<time, count>>.
    public static SortedMap<String, List<ArduinoEntry>> getDataFromOSS(final Context context) {
        boolean success = false;
        OSS oss = login(context);

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

            //start retrieving files
            for (OSSObjectSummary summary : oss.listObjects(new ListObjectsRequest(bucketName)).getObjectSummaries()) {
                String deviceId = summary.getKey();
                deviceIds.add(deviceId);


                success = download(oss, deviceId, path + File.separator + deviceId);
            }

        } catch (IOException e) {
            Log.d("我", "DataGetter: exception occurs when downloading files.");
            success = false;
            e.printStackTrace();
        } catch (ClientException | ServiceException e) {
            e.printStackTrace();
        }

        if (!success) {
            Log.d("我", "DataGetter: failed to download files.");
            ThreadUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.makeToast(context, context.getString(R.string.failed_to_fetch_data_prompt), false);
                }
            });
            return null;
        }

        //execute here only if success
        SortedMap<String, List<ArduinoEntry>> all_data = new TreeMap<>();
        try {
            //for every file (one file for every device)
            long startTimeBkup = context.getSharedPreferences(context.getString(R.string.status_prefs), Context.MODE_PRIVATE).getLong(context.getString(R.string.sp_start_time), 0);
            for (String deviceId : deviceIds) {
                File file = new File(path, deviceId);
                ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
                String line;
                List<ArduinoEntry> device_data = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    String[] v = line.split(" ");

                    try {
                        long time = Long.parseLong(v[0]);
                        int count = Integer.parseInt(v[1]);
                        ArduinoEntry entry = new ArduinoEntry(time, count);
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

    static final String deviceId = "Gate1@Entrance";
    static final String bucketName = "projectultracount";
    static final String objectName = "Gate1@Entrance.txt";
    static final String endpoint = "https://oss-ap-southeast-1.aliyuncs.com";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static OSS login(Context context) {
        Map<String, String> credentials = getSTSToken();
        String StsAccessKeyId = credentials.get("AccessKeyId");
        String StsSecretKeyId = credentials.get("AccessKeySecret");
        String StsSecurityToken = credentials.get("SecurityToken");

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(StsAccessKeyId, StsSecretKeyId, StsSecurityToken);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒。
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒。
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个。
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次。

        OSS oss = new OSSClient(context, endpoint, credentialProvider, conf);
        oss.updateCredentialProvider(new OSSStsTokenCredentialProvider(StsAccessKeyId, StsSecretKeyId, StsSecurityToken));
//        OSSLog.enableLog();

        return oss;
    }

    static private boolean download(OSS oss, final String objectName, final String path) {

        try {
            GetObjectResult result = oss.getObject(new GetObjectRequest(bucketName, objectName));
            InputStream content = result.getObjectContent();
            if (content != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                FileWriter fWriter;
                File sdCardFile = new File(path/*context.getFilesDir().toString() + "/" + objectName*/);

                Log.d("TAG", sdCardFile.getPath()); //<-- check the log to make sure the path is correct.

                try {
                    fWriter = new FileWriter(sdCardFile, true);
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) break;
                        fWriter.write(line + "\n");
                    }
                    content.close();
                    fWriter.close();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("屮", e.toString());
                    return false;
                }
                return true;
            }
        } catch (ClientException | ServiceException e) {
            e.printStackTrace();
            Log.e("屮", e.toString());
            return false;
        }
        Log.e("屮", "should not reach here");
        return false;
    }


}
