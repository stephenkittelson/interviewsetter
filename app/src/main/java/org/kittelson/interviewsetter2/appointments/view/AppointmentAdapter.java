package org.kittelson.interviewsetter2.appointments.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.kittelson.interviewsetter2.R;
import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.AppointmentType;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
    private static String CLASS_NAME = AppointmentAdapter.class.getSimpleName();
    private List<Appointment> appointments;
    private RecyclerView recyclerView;
    private FragmentActivity fragmentActivity;

    public AppointmentAdapter(FragmentActivity fragmentActivity, List<Appointment> appointments, RecyclerView recyclerView) {
        this.fragmentActivity = fragmentActivity;
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
            Appointment appointment = appointments.get(recyclerView.getChildLayoutPosition(v));
            Log.v(CLASS_NAME, "clicked on item " + appointment);
            Cursor cursor = fragmentActivity.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.Contacts.DISPLAY_NAME + " in (" + StringUtils.repeat("?", ",", appointment.getCompanions().size()) + ") AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    appointment.getCompanions().toArray(new String[]{}),
                    null);
            List<String> allPhoneNumbers = new LinkedList<>();
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.v(CLASS_NAME, "phone number: " + phoneNumber);
                allPhoneNumbers.add(phoneNumber);
            }
            cursor.close();
            String msg = "";
            if (appointment.getAppointmentType().equals(AppointmentType.Ministering)) {
                msg = "Could you guys meet with a member of the EQ presidency for a ministering interview on ";
            } else {
                msg = "Could you meet with Pres TODO for an individual stewardship interview on ";
            }
            msg += appointment.getTime().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                    + " at " + DateTimeFormatter.ofPattern("h:m").format(appointment.getTime()) + " " + appointment.getLocation() + "?";
            fragmentActivity.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + StringUtils.join(allPhoneNumbers, ";")))
                    .putExtra("sms_body", msg));
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
