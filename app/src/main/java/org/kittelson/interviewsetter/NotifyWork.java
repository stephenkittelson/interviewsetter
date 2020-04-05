package org.kittelson.interviewsetter;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.kittelson.interviewsetter.appointments.Appointment;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotifyWork extends AsyncTask<Void, Void, Void> {
    private static String CLASS_NAME = NotifyWork.class.getSimpleName();

    public static final String SEND_TEXTS_NOTIF_CHANNEL = "sendTexts";
    private static final int SET_APPTS_NOTIFICATION = 0;
    private static final int CONFIRM_APPTS_NOTIFICATION = 1;
    private static final int ERROR_NOTIFICATION = 3;

    private TextingService context;

    public NotifyWork(TextingService context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        AppointmentsManager appointmentsManager = new AppointmentsManager();
        boolean notifiedUser = false;
        try {
            try {
                List<Appointment> tentativeAppointments = appointmentsManager.getTentativeAppointments(GoogleSignIn.getLastSignedInAccount(context).getAccount(), context);
                if (tentativeAppointments.size() > 0) {
                    notify(SET_APPTS_NOTIFICATION, "Set appointments", "Set " + getEarliestDate(tentativeAppointments) + " appt");
                    notifiedUser = true;
                }
            } catch (IllegalArgumentException ignored2) {
                // can't notify the user, ignore it
            }

            try {
                List<Appointment> appointmentsToConfirm = appointmentsManager.getAppointmentsToConfirm(context);
                if (appointmentsToConfirm.size() > 0) {
                    notify(CONFIRM_APPTS_NOTIFICATION, "Confirm appointments", "Confirm " + getEarliestDate(appointmentsToConfirm) + " appt");
                    notifiedUser = true;
                }
            } catch (IllegalArgumentException ignored2) {
                // can't notify the user, ignore it
            }
        } catch (UserRecoverableAuthIOException ex) {
            // we'll notify them below
        }
        if (!notifiedUser) {
            notify(ERROR_NOTIFICATION, "Set/confirm appointments", "Spreadsheet error - set/confirm appts?");
        }
        context.finishJob();
        return null;
    }

    private String getEarliestDate(List<Appointment> appointments) {
        return DateTimeFormatter.ofPattern("LLL d").format(appointments.stream().sorted((appt1, appt2) -> appt1.getTime().compareTo(appt2.getTime())).findFirst().get().getTime());
    }

    private void notify(int errorNotification, String title, String text) {
        NotificationManagerCompat.from(context).notify(errorNotification, new NotificationCompat.Builder(context, SEND_TEXTS_NOTIF_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, errorNotification, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build());
    }


    @Override
    protected void onPostExecute(Void ignored) {
        super.onPostExecute(ignored);
    }
}
