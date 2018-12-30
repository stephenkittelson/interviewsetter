package org.kittelson.interviewsetter2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
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
                .setMinimumLatency(jobWindow.getWindowStartMillis())
                .setOverrideDeadline(jobWindow.getWindowEndMillis())
                .build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(CLASS_NAME, "job scheduling failed");
        }
        Log.v(CLASS_NAME, "job scheduler setup");
    }

    public JobWindow getNextJobWindow(ZonedDateTime currentTime) {
        ZonedDateTime windowStart = ZonedDateTime.now();
        ZonedDateTime windowEnd = ZonedDateTime.now();
        switch (currentTime.getDayOfWeek()) {
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
                if (currentTime.getHour() > 20 || (currentTime.getHour() == 20 && currentTime.getMinute() >= 20)) {
                    windowStart = currentTime.plusDays(1).withHour(19).withMinute(0).withSecond(0);
                    windowEnd = currentTime.plusDays(1).withHour(20).withMinute(20).withSecond(0);
                } else if (currentTime.getHour() >= 19) {
                    windowStart = currentTime;
                    windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
                } else {
                    windowStart = currentTime.withHour(19).withMinute(0).withSecond(0);
                    windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
                }
                break;
            case FRIDAY:
                if (currentTime.getHour() > 20 || (currentTime.getHour() == 20 && currentTime.getMinute() >= 20)) {
                    windowStart = currentTime.plusDays(1).withHour(10).withMinute(0).withSecond(0);
                    windowEnd = currentTime.plusDays(1).withHour(20).withMinute(20).withSecond(0);
                } else if (currentTime.getHour() >= 19) {
                    windowStart = currentTime;
                    windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
                } else {
                    windowStart = currentTime.withHour(19).withMinute(0).withSecond(0);
                    windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
                }
                break;
            case SATURDAY:
                if (currentTime.getHour() > 20 || (currentTime.getHour() == 20 && currentTime.getMinute() >= 20)) {
                    windowStart = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(19).withMinute(0).withSecond(0);
                    windowEnd = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(20).withMinute(20).withSecond(0);
                } else if (currentTime.getHour() >= 10) {
                    windowStart = currentTime;
                    windowEnd = currentTime.withHour(20).withMinute(20).withSecond(0);
                } else {
                    windowStart = currentTime.withHour(10).withMinute(0).withSecond(0);
                    windowEnd = currentTime.withHour(11).withMinute(0).withSecond(0);
                }
                break;
            case MONDAY:
            case SUNDAY:
                windowStart = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(19).withMinute(0).withSecond(0);
                windowEnd = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(20).withMinute(0).withSecond(0);
                break;
        }
        return new JobWindow(windowStart, windowEnd);
    }
}
