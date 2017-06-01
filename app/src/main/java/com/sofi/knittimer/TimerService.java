package com.sofi.knittimer;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sofi.knittimer.data.ProjectContract;

/**
 * Created by Default User on 28.5.2017.
 */

public class TimerService extends Service {

    public static final String EXTRA_KEY_TIME_LEFT = "Time left";
    public static final String EXTRA_KEY_ID = "Id";

    public static final String BROADCAST_ACTION_UPDATE = "com.sofi.knittimer.ACTION_UPDATE";
    public static final String BROADCAST_ACTION_FINISH = "com.sofi.knittimer.ACTION_FINISH";

    private Handler handler = new Handler();
    private long initialTime = 0L;
    private long timeInMillis = 0L;

    private int projectId;
    private int initialTimeSpent;

    @Override
    public void onCreate() {
        super.onCreate();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialTime = SystemClock.uptimeMillis();
        projectId = intent.getIntExtra(EXTRA_KEY_ID, 0);
        initialTimeSpent = intent.getIntExtra(EXTRA_KEY_TIME_LEFT, 0);
        handler.postDelayed(sendUpdatesToUI, 1000);
        return START_NOT_STICKY;
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            sendTimerInfo(BROADCAST_ACTION_UPDATE);
            handler.postDelayed(this, 1000);
        }
    };

    private void sendTimerInfo(String action) {
        timeInMillis = SystemClock.uptimeMillis() - initialTime;

        Intent intent = new Intent(action);
        int timer = initialTimeSpent + (int) timeInMillis;
        intent.putExtra(EXTRA_KEY_ID, projectId);
        intent.putExtra(EXTRA_KEY_TIME_LEFT, timer);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        sendTimerInfo(BROADCAST_ACTION_FINISH);
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
