package org.kittelson.interviewsetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static String CLASS_NAME = BootBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(CLASS_NAME, "init on boot! Action: " + intent.getAction() + ", URI: " + intent.toUri(Intent.URI_INTENT_SCHEME));
        new JobSchedulingManager().scheduleNextTextingJob(context);
    }
}