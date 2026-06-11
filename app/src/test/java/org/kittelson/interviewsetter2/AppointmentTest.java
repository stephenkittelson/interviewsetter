package org.kittelson.interviewsetter2;

import org.junit.Assert;
import org.junit.Test;
import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.AppointmentStage;

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

    @Test
    public void compoundLastName_oneCompanion_parsed() throws Exception {
        Appointment appt = new Appointment().setCompanions("De La Cruz, Maria");
        Assert.assertEquals(1, appt.getCompanions().size());
        Assert.assertTrue("companionship should include Maria De La Cruz", appt.getCompanions().contains("Maria De La Cruz"));
    }

    @Test
    public void compoundLastName_twoCompanions_parsed() throws Exception {
        Appointment appt = new Appointment().setCompanions("De La Cruz, Maria / Garcia Lopez, Juan");
        Assert.assertEquals(2, appt.getCompanions().size());
        Assert.assertTrue("companionship should include Maria De La Cruz", appt.getCompanions().contains("Maria De La Cruz"));
        Assert.assertTrue("companionship should include Juan Garcia Lopez", appt.getCompanions().contains("Juan Garcia Lopez"));
    }

    @Test
    public void compoundLastName_familyPattern_parsed() throws Exception {
        Appointment appt = new Appointment().setCompanions("De La Cruz, Maria & Jose");
        Assert.assertEquals(1, appt.getCompanions().size());
        Assert.assertTrue("companionship should include Maria De La Cruz", appt.getCompanions().contains("Maria De La Cruz"));
    }

    @Test
    public void hyphenatedLastName_twoCompanions_parsed() throws Exception {
        Appointment appt = new Appointment().setCompanions("Smith-Jones, Alice / Brown-Davis, Bob");
        Assert.assertEquals(2, appt.getCompanions().size());
        Assert.assertTrue(appt.getCompanions().contains("Alice Smith-Jones"));
        Assert.assertTrue(appt.getCompanions().contains("Bob Brown-Davis"));
    }

    @Test
    public void tildePrefix_compoundLastName_parsed() throws Exception {
        Appointment appt = new Appointment().setCompanions("~De La Cruz, Maria / ~Garcia Lopez, Juan");
        Assert.assertEquals(2, appt.getCompanions().size());
        Assert.assertTrue(appt.getCompanions().contains("Maria De La Cruz"));
        Assert.assertTrue(appt.getCompanions().contains("Juan Garcia Lopez"));
    }
}
