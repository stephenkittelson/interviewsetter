package org.kittelson.interviewsetter.appointments;

import java.util.List;

public interface AppointmentListCallback {
    void setAppointmentList(List<Appointment> appointments);
}
