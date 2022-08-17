package com.hci.ireye.data.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.util.Vector;

public class JSchSftpUtil {

    public static Session setJSchSession(String username, String remoteHost, String password, String known_hosts, int port) throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(known_hosts);
        Session jSchSession = jsch.getSession(username, remoteHost, port);
        jSchSession.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSchSession.setConfig(config);
        jSchSession.connect();
        return jSchSession;
    }

    public static ChannelSftp setJSchChannelSftp(Session jSchSession) throws JSchException {
        ChannelSftp channelSftp = (ChannelSftp)jSchSession.openChannel("sftp");
        channelSftp.connect();
        return channelSftp;
    }

    public static Vector<MyFileInfo> getSftpFileList(ChannelSftp channelSftp, String remotePath, Context context) throws SftpException {
        Vector<MyFileInfo> fileInfo = new Vector<>();

        Vector fileList = channelSftp.ls(remotePath);
        for(int i = 0; i < fileList.size(); i++) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) fileList.get(i);
            fileInfo.add(new MyFileInfo(
                    entry.getFilename(),
                    entry.getAttrs().getSize(),
                    entry.getAttrs().isDir(),
                    DateFormat.format("yyyy/MM/dd hh:mm a", entry.getAttrs().getMTime() * 1000L).toString(),
                    context));
        }

        return fileInfo;
    }

    public static class MyFileInfo {
        public String name;
        public String size;
        public String format;
        public String lastModify;
        public boolean isDir;
        MyFileInfo(String name, long byteSize, boolean isDir, String lastModify, Context context) {
            this.name = name;
            this.size = Formatter.formatShortFileSize(context, byteSize);
            this.isDir = isDir;
            this.lastModify = lastModify;

            int p = this.name.lastIndexOf('.');
            if(!this.isDir && p > 0) {
                format = this.name.substring(p + 1);
            } else {
                format = null;
            }
        }
    }

    public static class MySftpInfo {
        public String username;
        public String remoteHost;
        public String password;
        public String knownHosts;
        public int port;
        public String remotePath;

        public MySftpInfo(String username, String remoteHost, String password, String knownHosts, int port, String remotePath) {
            this.username = username;
            this.remoteHost = remoteHost;
            this.password = password;
            this.knownHosts = knownHosts;
            this.port = port;
            this.remotePath = remotePath;
        }

        public MySftpInfo(MySftpInfo that) {
            this.username = that.username;
            this.remoteHost = that.remoteHost;
            this.password = that.password;
            this.knownHosts = that.knownHosts;
            this.port = that.port;
            this.remotePath = that.remotePath;
        }

    }

}
