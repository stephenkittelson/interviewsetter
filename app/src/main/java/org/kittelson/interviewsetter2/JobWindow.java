package org.kittelson.interviewsetter2;

import java.time.ZonedDateTime;

public class JobWindow {
    private ZonedDateTime windowStart;
    private ZonedDateTime windowEnd;

    public JobWindow(ZonedDateTime windowStart, ZonedDateTime windowEnd) {
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    public ZonedDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(ZonedDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public ZonedDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(ZonedDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }

    public long getWindowStartMillis() {
        return millisToDateTime(windowStart);
    }

    public long getWindowEndMillis() {
        return millisToDateTime(windowEnd);
    }

    private long millisToDateTime(ZonedDateTime targetTime) {
        return (targetTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond()) * 1000;
    }
}
