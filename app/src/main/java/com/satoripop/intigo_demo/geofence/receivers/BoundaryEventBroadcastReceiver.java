package com.satoripop.intigo_demo.geofence.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.JobIntentService;


import com.satoripop.intigo_demo.geofence.services.BoundaryEventJobIntentService;

import static com.satoripop.intigo_demo.MainActivity.TAG;

public class BoundaryEventBroadcastReceiver extends BroadcastReceiver {

    public BoundaryEventBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Broadcasting geofence event");
        JobIntentService.enqueueWork(context, BoundaryEventJobIntentService.class, 0, intent);
    }
}
