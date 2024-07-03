package com.hci.ireye.util;

//
// Created by Lithops on 2020/6/7, 16:15.
//

import android.util.Log;

import com.hci.ireye.data.type.FtpInfo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class ApacheFtpUtil {
    public static FTPClient ftpsClient;
    public static FtpInfo info;
    public static boolean connect(FTPClient client, FtpInfo info) {
        boolean success;
        try {
            client.connect(info.server, info.port);

            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                Log.d("我", "connect: Connect failed");
                return false;
            }
            success = client.login(info.username, info.password);
            if (!success) Log.d("我", "connect: Login Failed");
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }

        return success;
    }

    public static boolean changeWorkingDir(String dir, FTPClient ftpClient) throws IOException {
        // Changes working directory
        boolean success;
        success = ftpClient.changeWorkingDirectory(dir);
        if (!success) Log.d("我", "changeWorkingDir: failed");
        return success;
    }
}
