package org.kittelson.interviewsetter2;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Appointment {
    private LocalDateTime time;
    private List<String> companions;
    private AppointmentStage stage;
    private String location;
    private String presidencyMember;

    public Appointment(LocalDateTime time, List<String> companions, AppointmentStage stage, String location) {
        this.time = time;
        this.companions = companions;
        this.stage = stage;
        this.location = location;
    }

    public Appointment(List<Object> rawAppointment) {
        // coming in as a number, translate using https://developers.google.com/sheets/api/reference/rest/v4/DateTimeRenderOption#ENUM_VALUES.SERIAL_NUMBER
        Double serialDate = (Double) rawAppointment.get(0);
        this.time = LocalDateTime.of(1899, 12, 30, 0, 0).plusMinutes(Double.valueOf(serialDate /* days */ * 24.0 /* hr / day */ * 60.0 /* min / hr */).longValue());
        this.presidencyMember = (String) rawAppointment.get(1);
        if (!presidencyMember.equals("all")) {
            this.companions = Arrays.stream(((String) rawAppointment.get(2)).split("/")).map(companion -> StringUtils.trim(companion)).collect(Collectors.toList());
        }
        this.location = (String) rawAppointment.get(3);
        this.stage = AppointmentStage.valueOf(StringUtils.deleteWhitespace((String) rawAppointment.get(4)));
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
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
