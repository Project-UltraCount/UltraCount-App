package com.hci.ireye.data.aliyun;

import android.content.Context;
import android.util.Log;

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
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hci.ireye.data.aliyun.STSTokenGetter.getSTSToken;

public class OSSManager {

    private static OSSManager ossManager = null;
    private final OSS oss;

    private OSSManager(Context context) {
        oss = login(context);
    }

    public static OSSManager getInstance(Context context) {
        if (ossManager == null) ossManager = new OSSManager(context);
        return ossManager;
    }

    static final String BUCKET_NAME = "projectultracount";
    static final String ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";

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

        OSS oss = new OSSClient(context, ENDPOINT, credentialProvider, conf);
        oss.updateCredentialProvider(new OSSStsTokenCredentialProvider(StsAccessKeyId, StsSecretKeyId, StsSecurityToken));
//        OSSLog.enableLog();
        return oss;
    }

    public boolean downloadFile(final String objectName, final String path) {

        try {
            GetObjectResult result = oss.getObject(new GetObjectRequest(BUCKET_NAME, objectName));
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

    public String readFile(final String objectName) throws ServiceException, ClientException {
        GetObjectResult result = oss.getObject(new GetObjectRequest(BUCKET_NAME, objectName));
        InputStream inputStream = result.getObjectContent();
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void uploadFile(String objectName, String content) throws ServiceException, ClientException {
        oss.putObject(new PutObjectRequest(BUCKET_NAME, objectName, content.getBytes(StandardCharsets.UTF_8)));
    }

    public List<String> listFolders(String path) {
        ListObjectsRequest request = new ListObjectsRequest(BUCKET_NAME);

        ArrayList<String> ret = new ArrayList<>();
        try {
            for (OSSObjectSummary summary : oss.listObjects(request).getObjectSummaries()) {
                String objectIdentifier = summary.getKey();
                if (objectIdentifier.matches("^" + path + "[^/]+/$")) ret.add(summary.getKey());
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> listFiles(String path) {
        ListObjectsRequest request = new ListObjectsRequest(BUCKET_NAME);

        ArrayList<String> ret = new ArrayList<>();
        try {
            for (OSSObjectSummary summary : oss.listObjects(request).getObjectSummaries()) {
                String objectIdentifier = summary.getKey();
                if (objectIdentifier.matches("^" + path + "[^/]+\\..+$")) ret.add(summary.getKey());
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
