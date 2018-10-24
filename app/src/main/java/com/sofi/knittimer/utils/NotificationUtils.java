package com.sofi.knittimer.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sofi.knittimer.MainActivity;
import com.sofi.knittimer.R;

public class NotificationUtils {

    public static final String NOTIFICATION_CHANNEL_ID = "project_channel";
    public static final int NOTIFICATION_ID_TIMER_RUNNING = 001;

    public static Notification getTimerRunningNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_needles);
        builder.setContentTitle(context.getString(R.string.notification_title_running));
        builder.setContentText(context.getString(R.string.notification_detail_running));
        builder.setOngoing(true);

        builder.setContentIntent(getPendingIntentForMainActivity(context));

        return builder.build();
    }

    private static PendingIntent getPendingIntentForMainActivity(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

}
