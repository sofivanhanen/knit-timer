package com.sofi.knittimer;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by Default User on 28.5.2017.
 */

public class TimerService extends IntentService {

    public TimerService() {
        super("Project timer service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {



    }
}
