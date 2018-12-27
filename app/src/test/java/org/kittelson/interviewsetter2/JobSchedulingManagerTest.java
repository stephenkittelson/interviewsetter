package org.kittelson.interviewsetter2;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.junit.Assert.*;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class JobSchedulingManagerTest {
    @Test
    public void monday_tuesday1900() {
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 19, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 19, 1, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 20, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 20, 20, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 20, 21, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 21, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 21, 21, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 24, 23, 59, 0, 0, ZoneId.systemDefault())).getWindowStart());
    }

    @Test
    public void tuesdayPreWindow_tuesday1900() {
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 2, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 19, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 1, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 19, 1, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 19, 21, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 19, 21, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 25, 20, 19, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 20, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
    }

    @Test
    public void tuesdayAfterWindow_wednesday1900() {
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 20, 20, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 20, 21, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 20, 59, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 21, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 21, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 21, 21, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 22, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 23, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 26, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 25, 23, 59, 0, 0, ZoneId.systemDefault())).getWindowStart());
    }

    @Test
    public void fridayDuringAfterWindow_saturday1000() {
        assertEquals(ZonedDateTime.of(2018, 12, 28, 20, 19, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 28, 20, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 28, 20, 20, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 28, 21, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 28, 21, 19, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 28, 23, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
    }

    @Test
    public void saturdayCurrent_currentOrTuesday() {
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 9, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 9, 15, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 10, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 10, 10, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 10, 10, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 11, 20, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 11, 20, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 13, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 13, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 15, 40, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 15, 40, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2018, 12, 29, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 19, 0, 0, 0, ZoneId.systemDefault())).getWindowStart());

        assertEquals(ZonedDateTime.of(2019, 1, 1, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 20, 20, 0, 0, ZoneId.systemDefault())).getWindowStart());
        assertEquals(ZonedDateTime.of(2019, 1, 1, 19, 0, 0, 0, ZoneId.systemDefault()), new JobSchedulingManager().getNextJobWindow(ZonedDateTime.of(2018, 12, 29, 21, 18, 0, 0, ZoneId.systemDefault())).getWindowStart());
    }
}
