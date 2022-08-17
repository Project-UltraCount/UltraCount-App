package com.hci.ireye.data;

//
// Created by Lithops on 2020/7/17, 22:45.
//

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hci.ireye.R;

import java.util.LinkedList;
import java.util.List;

public class NotificationMaker {

    private Context mContext;

    private List<String> mDeviceNotificationTags = new LinkedList<>();

    private static final int CURRENT_PRESENT_ID = 0;
    private static final int DEVICE_OFFLINE_ID = 1;

    public NotificationMaker(Context context) {
        mContext = context;
        // copied from https://developer.android.com/training/notify-user/build-notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(mContext.getString(R.string.channel_id), mContext.getString(R.string.channel_name), importance);
            channel.setDescription(mContext.getString(R.string.channel_description));
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void makeCurrentPresent(int currentPresent) {

//        Intent intent = new Intent(mContext, activity);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        String title = currentPresent + "";
        String content = mContext.getString(R.string.chart_total_present);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(CURRENT_PRESENT_ID, builder.build());
    }

    public void cancelCurrentPresent() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.cancel(CURRENT_PRESENT_ID);
    }

    public void makeDeviceOffline(String deviceId) {
        String title = mContext.getString(R.string.device) + " " + deviceId;
        String content = mContext.getString(R.string.device_offline);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        notificationManager.notify(deviceId, DEVICE_OFFLINE_ID, builder.build());

        mDeviceNotificationTags.add(deviceId);
    }

    public void cancelDeviceOffline(String deviceId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.cancel(deviceId, DEVICE_OFFLINE_ID);

        mDeviceNotificationTags.remove(deviceId);
    }

    public void cancelAllDeviceOffline() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        for (Object deviceId: mDeviceNotificationTags.toArray()) {
            notificationManager.cancel((String)deviceId, DEVICE_OFFLINE_ID);
        }
    }
}
