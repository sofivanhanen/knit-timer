package com.sofi.knittimer.data;

import android.graphics.Bitmap;

// Knitting project class
public class Project {

    public int id;
    public String name;
    public long timeSpentInMillis;
    public int percentageDone;

    public boolean timerRunning;

    public Bitmap background;
    public boolean wasChecked;

    public Project(int id, String name, long timeSpentInMillis, int percentageDone) {
        this.id = id;
        this.name = name;
        this.timeSpentInMillis = timeSpentInMillis;
        this.percentageDone = percentageDone;
        timerRunning = false;
        background = null;
        wasChecked = false;
    }

    public long timeLeftInMillis() {
        if (percentageDone == 0) {
            return 0;
        } else {
            return (100 - percentageDone) * (timeSpentInMillis / percentageDone);
        }
    }

}
