package org.kittelson.interviewsetter2;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

public class JobSchedulingManager {
    public JobWindow getNextJobWindow() {
        return getNextJobWindow(ZonedDateTime.now());
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
