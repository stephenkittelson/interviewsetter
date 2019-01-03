package org.kittelson.interviewsetter2;

import org.junit.Assert;
import org.junit.Test;
import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.AppointmentStage;

import java.time.LocalDateTime;

public class AppointmentTest {
    @Test
    public void normalAppointment_parsed() throws Exception {
        Appointment appt = new Appointment()
                .setTime(43478.5243055556)
                .setPresidencyMember("Smith")
                .setCompanions("Doe, John / Curie, Sam")
                .setLocation("outside the Bishop's office")
                .setStage("Initial Contact");
        Assert.assertEquals("outside the Bishop's office", appt.getLocation());
        Assert.assertEquals(AppointmentStage.InitialContact, appt.getStage());
        Assert.assertEquals(LocalDateTime.of(2019, 1, 13, 12, 35, 0, 0), appt.getTime());
        Assert.assertEquals(2, appt.getCompanions().size());
        Assert.assertTrue("companionship should include John Doe", appt.getCompanions().contains("John Doe"));
        Assert.assertTrue("companionship should include Sam Curie", appt.getCompanions().contains("Sam Curie"));
    }
}
