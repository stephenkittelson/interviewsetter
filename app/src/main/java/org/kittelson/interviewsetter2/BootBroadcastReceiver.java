package org.kittelson.interviewsetter2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static String CLASS_NAME = BootBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(CLASS_NAME, "init on boot! Action: " + intent.getAction() + ", URI: " + intent.toUri(Intent.URI_INTENT_SCHEME));
        new JobSchedulingManager().scheduleNextTextingJob(context);
    }
}
