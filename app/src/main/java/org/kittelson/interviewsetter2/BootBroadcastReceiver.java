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

        JobWindow jobWindow = new JobSchedulingManager().getNextJobWindow();
        jobWindow.setWindowStart(ZonedDateTime.now().plusSeconds(1)); // TODO debugging only
        jobWindow.setWindowEnd(ZonedDateTime.now().plusSeconds(2)); // TODO debugging only
        Log.v(CLASS_NAME, "start time: " + DateTimeFormatter.ISO_DATE_TIME.format(jobWindow.getWindowStart())
                + ", end time: " + DateTimeFormatter.ISO_DATE_TIME.format(jobWindow.getWindowEnd()));

/* this didn't work, wouldn't allow the service to start, jobscheduler works though
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP,
                jobWindow.getWindowStart().withZoneSameInstant(ZoneOffset.UTC).toEpochSecond() * 1000,
                jobWindow.getWindowEndMillis() - jobWindow.getWindowStartMillis(),
                PendingIntent.getService(context, 0, new Intent(context, TextingService.class), PendingIntent.FLAG_UPDATE_CURRENT));
        Log.v(BootBroadcastReceiver.class.getSimpleName(), "setup alarm manager");
*/
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, TextingService.class))
                .setPersisted(true)
                .setMinimumLatency(jobWindow.getWindowStartMillis())
                .setOverrideDeadline(jobWindow.getWindowEndMillis())
                .build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(CLASS_NAME, "job scheduling failed");
        }
        Log.v(CLASS_NAME, "job scheduler setup");
    }
}
