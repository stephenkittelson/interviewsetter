package org.kittelson.interviewsetter2;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class AppointmentTest {
    @Test
    public void normalAppointment_parsed() throws Exception {
        Appointment appt = new Appointment(Arrays.asList("1/13/2019","12:35 PM","Smith","Doe, John / Curie, Sam","outside the Bishop's office","Initial Contact"));
        Assert.assertEquals(appt.getLocation(), "outside the Bishop's office");
        Assert.assertEquals(appt.getStage(), AppointmentStage.InitialContact);
        Assert.assertEquals(appt.getTime(), ZonedDateTime.of(2019, 1, 13, 12, 35, 0, 0, ZoneOffset.systemDefault()));
        Assert.assertTrue(appt.getCompanions().contains("John Doe"));
        Assert.assertTrue(appt.getCompanions().contains("Sam Curie"));
        Assert.assertEquals(2, appt.getCompanions().size());
    }
}
