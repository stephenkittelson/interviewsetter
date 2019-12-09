package org.kittelson.interviewsetter.appointments.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.kittelson.interviewsetter.ContactInfo;
import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.AppointmentStage;
import org.kittelson.interviewsetter.appointments.AppointmentType;
import org.kittelson.interviewsetter.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
    private static String CLASS_NAME = AppointmentAdapter.class.getSimpleName();
    private List<Appointment> appointments;
    private RecyclerView recyclerView;
    private FragmentActivity fragmentActivity;
    private Appointment detail;

    private static final String PRES_LAST_NAME = "TODO";

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
        view.setOnClickListener(v -> openTextMsg(appointments.get(recyclerView.getChildLayoutPosition(v)), getContactInfo(appointments.get(recyclerView.getChildLayoutPosition(v)))));
        view.setOnLongClickListener(v -> {
            // TODO show the detail view, provide a way back to the list view
            return true;
        });
        return new AppointmentViewHolder(view);
    }

    private void openTextMsg(Appointment appointment, List<ContactInfo> allContactInfo) {
        if (allContactInfo.size() == 0) {
            Toast.makeText(fragmentActivity, "No phone numbers for companionship", Toast.LENGTH_LONG).show();
        } else {
            String msg = "";
            if (!appointment.getStage().equals(AppointmentStage.Confirmed) && !appointment.getStage().equals(AppointmentStage.Set)) {
                if (appointment.getAppointmentType().equals(AppointmentType.Stewardship)) {
                    msg = "Could you meet with Pres " + PRES_LAST_NAME + " for an individual stewardship interview ";
                } else if (appointment.getAppointmentType().equals(AppointmentType.Family)) {
                    msg = "Could the EQ presidency visit you and your family ";
                } else if (allContactInfo.size() > 1) {
                    msg = "Could you guys meet with a member of the EQ presidency for a ministering interview ";
                } else if (appointment.getCompanions().size() > 1) {
                    String nameMissingPhoneNum = getNameMissingPhoneNum(appointment, allContactInfo);
                    Log.v(CLASS_NAME, "Missing phone number for " + nameMissingPhoneNum + ", companions: " + appointment.getCompanions() + ", contact info: " + allContactInfo);
                    msg = "Could you and " + nameMissingPhoneNum + " meet with a member of the EQ presidency for a ministering interview ";
                } else {
                    msg = "Could you meet with a member of the EQ presidency for a ministering interview ";
                }

                msg += getTimeAndLocation(appointment) + "?";
            } else if (appointment.getStage().equals(AppointmentStage.Set)) {
                if (appointment.getAppointmentType().equals(AppointmentType.Stewardship)) {
                    msg = "Just texting to confirm your individual stewardship interview with Pres " + PRES_LAST_NAME + " ";
                } else if (appointment.getAppointmentType().equals(AppointmentType.Family)) {
                    msg = "Just texting to confirm that the EQ presidency will visit you and your family ";
                } else if (allContactInfo.size() == 1 && appointment.getCompanions().size() > 1) {
                    msg = "Just texting to confirm you and " + getNameMissingPhoneNum(appointment, allContactInfo) + "'s ministering interview with a member of the EQ presidency ";
                } else {
                    msg = "Just texting to confirm your ministering interview with a member of the EQ presidency ";
                }

                msg += getTimeAndLocation(appointment);
            }
            fragmentActivity.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + StringUtils.join(allContactInfo.stream().map(contactInfo -> contactInfo.getPhoneNumber()).collect(Collectors.toList()), ";")))
                    .putExtra("sms_body", msg));
        }
    }

    private String getNameMissingPhoneNum(Appointment appointment, List<ContactInfo> allContactInfo) {
        return appointment.getCompanions().stream()
                .filter(companion -> allContactInfo.stream().noneMatch(contactInfo -> contactInfo.getName().equals(companion)))
                .findFirst().get().split(" ")[0];
    }

    @NonNull
    private List<ContactInfo> getContactInfo(Appointment appointment) {
        List<ContactInfo> allContactInfo = new LinkedList<>();
        if (ContextCompat.checkSelfPermission(fragmentActivity, "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(fragmentActivity, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            Cursor cursor = fragmentActivity.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.StructuredName.SORT_KEY_PRIMARY,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.Contacts.DISPLAY_NAME + " in (" + StringUtils.repeat("?", ",", appointment.getCompanions().size()) + ") AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    appointment.getCompanions().toArray(new String[]{}),
                    null);
            while (cursor.moveToNext()) {
                ContactInfo contactInfo = new ContactInfo(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SORT_KEY_PRIMARY)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                allContactInfo.add(contactInfo);
            }
            cursor.close();
        }
        return allContactInfo;
    }

    @NonNull
    private String getTimeAndLocation(Appointment appointment) {
        String msg;
        if (appointment.getTime().isBefore(LocalDateTime.now().withHour(23).withMinute(59))) {
            msg = "today ";
        } else if (appointment.getTime().isBefore(LocalDateTime.now().plusDays(1).withHour(23).withMinute(59))) {
            msg = "tomorrow ";
        } else {
            msg = "on " + appointment.getTime().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ";
        }
        msg += "at " + DateTimeFormatter.ofPattern("h:mm a").format(appointment.getTime()) + " ";
        if (appointment.getAppointmentType().equals(AppointmentType.Family)) {
            msg += "at your place";
        } else {
            msg += appointment.getLocation();
        }
        return msg;
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.textView.setText(StringUtils.join(appointment.getCompanions(), " / ") + (appointment.getAppointmentType().equals(AppointmentType.Stewardship) ? " (stewardship)" : ""));
        if (appointment.isDuplicate()) {
            holder.textView.setBackgroundColor(Color.RED);
        } else {
            holder.textView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }
}
