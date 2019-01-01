package org.kittelson.interviewsetter2;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class AppointmentTest {
    @Test
    public void normalAppointment_parsed() throws Exception {
        Appointment appt = new Appointment(Arrays.asList(43478.5243055556, "Smith", "Doe, John / Curie, Sam", "outside the Bishop's office", "Initial Contact"));
        Assert.assertEquals("outside the Bishop's office", appt.getLocation());
        Assert.assertEquals(AppointmentStage.InitialContact, appt.getStage());
        Assert.assertEquals(LocalDateTime.of(2019, 1, 13, 12, 35, 0, 0), appt.getTime());
        Assert.assertEquals(2, appt.getCompanions().size());
        Assert.assertTrue(appt.getCompanions().contains("John Doe"));
        Assert.assertTrue(appt.getCompanions().contains("Sam Curie"));
    }
}
