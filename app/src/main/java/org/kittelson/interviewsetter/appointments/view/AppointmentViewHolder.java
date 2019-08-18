package org.kittelson.interviewsetter.appointments.view;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public AppointmentViewHolder(@NonNull TextView itemView) {
        super(itemView);
        textView = itemView;
    }
}
