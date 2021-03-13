package org.kittelson.interviewsetter;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.NetworkRequest;
import android.util.Log;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class JobSchedulingManager {
    private static String CLASS_NAME = JobSchedulingManager.class.getSimpleName();

    public void scheduleNextTextingJob(Context context) {
        scheduleNextJobWithOffset(context, ZonedDateTime.now());
    }

    public void scheduleNextJobWithOffset(Context context, ZonedDateTime offset) {
        JobWindow jobWindow = new JobSchedulingManager().getNextJobWindow(offset);
        Log.v(CLASS_NAME, "start time: " + DateTimeFormatter.ISO_DATE_TIME.format(jobWindow.getWindowStart())
                + ", end time: " + DateTimeFormatter.ISO_DATE_TIME.format(jobWindow.getWindowEnd()));

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, TextingService.class))
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(jobWindow.getWindowStartMillis())
                .setOverrideDeadline(jobWindow.getWindowEndMillis())
                .build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(CLASS_NAME, "job scheduling failed");
        }
        Log.v(CLASS_NAME, "job scheduler setup");
    }

    public JobWindow getNextJobWindow(ZonedDateTime currentTime) {
        ZonedDateTime windowStart;
        ZonedDateTime windowEnd;
        DayOfWeek nextWindowDay = DayOfWeek.MONDAY;
        int currentWindowStartHour = 19;
        int nextWindowStartHour = 19;
        boolean isWindowToday = true;
        switch (currentTime.getDayOfWeek()) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
                isWindowToday = true;
                nextWindowDay = currentTime.plusDays(1).getDayOfWeek();
                nextWindowStartHour = 19;
                currentWindowStartHour = 19;
                break;
            case FRIDAY:
                isWindowToday = true;
                nextWindowDay = currentTime.plusDays(1).getDayOfWeek();
                nextWindowStartHour = 10;
                currentWindowStartHour = 19;
                break;
            case SATURDAY:
                isWindowToday = true;
                nextWindowDay = DayOfWeek.MONDAY;
                nextWindowStartHour = 19;
                currentWindowStartHour = 10;
                break;
            case SUNDAY:
                isWindowToday = false;
                nextWindowDay = DayOfWeek.MONDAY;
                nextWindowStartHour = 19;
                break;
        }
        if (!isWindowToday || currentTime.getHour() > 20 || (currentTime.getHour() == 20 && currentTime.getMinute() >= 20)) {
            windowStart = currentTime.with(TemporalAdjusters.next(nextWindowDay)).withHour(nextWindowStartHour).withMinute(0).withSecond(0);
            windowEnd = currentTime.with(TemporalAdjusters.next(nextWindowDay)).withHour(20).withMinute(20).withSecond(0);
        } else if (currentTime.getHour() >= currentWindowStartHour) {
            windowStart = currentTime;
            windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
        } else {
            windowStart = currentTime.withHour(currentWindowStartHour).withMinute(0).withSecond(0);
            windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
        }
        return new JobWindow(windowStart, windowEnd);
    }
}
