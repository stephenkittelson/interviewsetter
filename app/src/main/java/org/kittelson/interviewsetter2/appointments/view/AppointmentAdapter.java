package org.kittelson.interviewsetter2.appointments.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.kittelson.interviewsetter2.R;
import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.AppointmentType;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
    private static String CLASS_NAME = AppointmentAdapter.class.getSimpleName();
    private List<Appointment> appointments;
    private RecyclerView recyclerView;

    public AppointmentAdapter(List<Appointment> appointments, RecyclerView recyclerView) {
        this.appointments = appointments;
        this.recyclerView = recyclerView;
    }

    public void setAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_list_item, parent, false);
        view.setOnClickListener(v -> {
            Log.v(CLASS_NAME, "clicked on item " + appointments.get(recyclerView.getChildLayoutPosition(v)));
        });
        return new AppointmentViewHolder(view);
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
