package org.kittelson.interviewsetter2.appointments.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

public class AppointmentViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public AppointmentViewHolder(@NonNull TextView itemView) {
        super(itemView);
        textView = itemView;
    }
}
