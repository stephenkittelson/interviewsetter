package org.kittelson.interviewsetter2;

import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Appointment {
    private ZonedDateTime time;
    private List<String> companions;
    private AppointmentStage stage;
    private String location;
    private String presidencyMember;

    public Appointment(ZonedDateTime time, List<String> companions, AppointmentStage stage, String location) {
        this.time = time;
        this.companions = companions;
        this.stage = stage;
        this.location = location;
    }

    public Appointment(List<Object> rawAppointment) {
        this.time = ZonedDateTime.parse((String) rawAppointment.get(0) + " " + (String) rawAppointment.get(1));
        this.presidencyMember = (String) rawAppointment.get(2);
        if (!presidencyMember.equals("all")) {
            this.companions = Arrays.stream(((String) rawAppointment.get(3)).split("/")).map(companion -> StringUtils.trim(companion)).collect(Collectors.toList());
        }
        this.location = (String) rawAppointment.get(4);
        this.stage = AppointmentStage.valueOf((String) rawAppointment.get(5));
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public List<String> getCompanions() {
        return companions;
    }

    public void setCompanions(List<String> companions) {
        this.companions = companions;
    }

    public AppointmentStage getStage() {
        return stage;
    }

    public void setStage(AppointmentStage stage) {
        this.stage = stage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
