package com.sofi.knittimer;

import android.os.Handler;

public class TimingRunnable implements Runnable {

    private Handler myHandler;
    private ProjectAdapter adapter;
    private int currentlyRunningId;
    private long timeAtBeginning;
    private long timeAtLastUpdate;

    TimingRunnable(Handler myHandler, ProjectAdapter adapter) {
        this.myHandler = myHandler;
        this.adapter = adapter;
    }

    void begin() {
        currentlyRunningId = adapter.preferences.getInt(adapter.activityContext.getResources()
                .getString(R.string.shared_preferences_current_id_key), -1);
        timeAtBeginning = adapter.preferences.getLong(adapter.activityContext.getResources()
                .getString(R.string.shared_preferences_begin_time_key), -1);
        timeAtLastUpdate = timeAtBeginning;
        myHandler.post(this);
    }

    @Override
    public void run() {
        if (timeAtLastUpdate == timeAtBeginning) { // first run - check that it really should be running
            if (currentlyRunningId == -1 || timeAtBeginning == -1) {
                // if values in SharedPreferences show that timer should not be running, we stop the timer
                return;
            }
        }
        long currentTime = System.currentTimeMillis();
        adapter.updateTime(currentlyRunningId, currentTime - timeAtLastUpdate);
        timeAtLastUpdate = currentTime;
        myHandler.postDelayed(this, 1000);
    }
}
