package com.sofi.knittimer.utils;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.sofi.knittimer.R;

public class NotificationUtils {

    public static final int NOTIFICATION_ID_TIMER_RUNNING = 001;

    public static Notification getTimerRunningNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_play_circle);
        builder.setContentTitle(context.getString(R.string.notification_title_running));
        builder.setContentText(context.getString(R.string.notification_detail_running));
        builder.setOngoing(true);
        return builder.build();
    }

}
