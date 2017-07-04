package com.sofi.knittimer.utils;

import android.content.ContentValues;
import android.content.Intent;

import com.sofi.knittimer.MainActivity;
import com.sofi.knittimer.data.ProjectContract;

public final class DataUtils {

    private DataUtils() {

    }

    public static ContentValues intentToContentValues(Intent intent) {
        ContentValues contentValues = new ContentValues();

        if (intent.hasExtra(MainActivity.PROJECT_NAME_KEY)) {
            contentValues.put(ProjectContract.ProjectEntry._NAME,
                    intent.getStringExtra(MainActivity.PROJECT_NAME_KEY));
        } else {
            throw new UnsupportedOperationException("Received intent without project name!!!");
        }

        if (intent.hasExtra(MainActivity.PROJECT_ID_KEY)) {
            contentValues.put(ProjectContract.ProjectEntry._ID,
                    intent.getIntExtra(MainActivity.PROJECT_ID_KEY, 0));
        }

        if (intent.hasExtra(MainActivity.PROJECT_TIME_KEY)) {
            contentValues.put(ProjectContract.ProjectEntry._TIME_SPENT,
                    intent.getLongExtra(MainActivity.PROJECT_TIME_KEY, 0));
        } else {
            contentValues.put(ProjectContract.ProjectEntry._TIME_SPENT, 0);
        }

        if (intent.hasExtra(MainActivity.PROJECT_PERCENT_KEY)) {
            contentValues.put(ProjectContract.ProjectEntry._PERCENT_DONE,
                    intent.getIntExtra(MainActivity.PROJECT_PERCENT_KEY, 0));
        } else {
            contentValues.put(ProjectContract.ProjectEntry._PERCENT_DONE, 0);
        }
        return contentValues;
    }

}
