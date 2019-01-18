package org.kittelson.interviewsetter2.appointments;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Appointment {
    private LocalDateTime time;
    private List<String> companions;
    private AppointmentStage stage;
    private String location;
    private String presidencyMember;
    private AppointmentType appointmentType;
    private boolean duplicate;

    public Appointment() {
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Appointment setTime(LocalDateTime time) {
        this.time = time;
        return this;
    }

    public Appointment setTime(Double serialDate) {
        // coming in as a number, translate using https://developers.google.com/sheets/api/reference/rest/v4/DateTimeRenderOption#ENUM_VALUES.SERIAL_NUMBER
        this.time = LocalDateTime.of(1899, 12, 30, 0, 0).plusMinutes(Math.round(Double.valueOf(serialDate /* days */ * 24.0 /* hr / day */ * 60.0 /* min / hr */)));
        return this;
    }

    public List<String> getCompanions() {
        return companions;
    }

    public Appointment setCompanions(List<String> companions) {
        this.companions = companions;
        return this;
    }

    public Appointment setCompanions(String companions) {
        if (companions.contains("&")) {
            companions = StringUtils.substringBefore(StringUtils.strip(companions, "~"), "&");
        }
        this.companions = Arrays.stream(companions.split("/")).map(companion -> {
            String[] reversedName = StringUtils.strip(StringUtils.trim(companion), "~").split(",");
            return StringUtils.trim(reversedName[1]) + " " + StringUtils.trim(reversedName[0]);
        }).collect(Collectors.toList());
        return this;
    }

    public AppointmentStage getStage() {
        return stage;
    }

    public Appointment setStage(AppointmentStage stage) {
        this.stage = stage;
        return this;
    }

    public Appointment setStage(String stage) {
        this.stage = AppointmentStage.valueOf(StringUtils.deleteWhitespace(stage));
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Appointment setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getPresidencyMember() {
        return presidencyMember;
    }

    public Appointment setPresidencyMember(String presidencyMember) {
        this.presidencyMember = presidencyMember;
        return this;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Appointment setAppointmentType(String appointmentType) {
        if (StringUtils.equals(appointmentType, "Comp chg")) {
            this.appointmentType = AppointmentType.CompChg;
        } else {
            this.appointmentType = AppointmentType.valueOf(appointmentType);
        }
        return this;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "time=" + time +
                ", companions=" + companions +
                ", stage=" + stage +
                ", location='" + location + '\'' +
                ", presidencyMember='" + presidencyMember + '\'' +
                ", appointmentType=" + appointmentType +
                '}';
    }
}
