package org.kittelson.interviewsetter2.appointments.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.kittelson.interviewsetter2.R;
import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.AppointmentType;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
    private List<Appointment> appointments;

    public AppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void setAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppointmentViewHolder((TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.textView.setText(StringUtils.join(appointments.get(position).getCompanions(), " / ") + (appointments.get(position).getAppointmentType().equals(AppointmentType.Stewardship) ? " (stewardship)" : ""));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }
}
