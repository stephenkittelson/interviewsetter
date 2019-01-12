package org.kittelson.interviewsetter2.appointments;

import java.util.List;

public interface AppointmentListCallback {
    void setAppointmentList(List<Appointment> appointments);
}
