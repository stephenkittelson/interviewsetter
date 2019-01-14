package org.kittelson.interviewsetter2;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

public class NotifyWork extends AsyncTask<Void, Void, Void> {
    private static String CLASS_NAME = NotifyWork.class.getSimpleName();

    public static final String SEND_TEXTS_NOTIF_CHANNEL = "sendTexts";
    private static final int SET_APPTS_NOTIFICATION = 0;
    private static final int CONFIRM_APPTS_NOTIFICATION = 1;

    private Context context;

    public NotifyWork(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        AppointmentsManager appointmentsManager = new AppointmentsManager();
        if (appointmentsManager.getTentativeAppointments(GoogleSignIn.getLastSignedInAccount(context).getAccount(), context).size() > 0) {
            NotificationManagerCompat.from(context).notify(SET_APPTS_NOTIFICATION, new NotificationCompat.Builder(context, SEND_TEXTS_NOTIF_CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_menu_send)
                    .setContentTitle("Set appointments")
                    .setContentText("Time to setup appointments")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, SET_APPTS_NOTIFICATION, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .build());
        }

        if (appointmentsManager.getAppointmentsToConfirm(context).size() > 0) {
            NotificationManagerCompat.from(context).notify(CONFIRM_APPTS_NOTIFICATION, new NotificationCompat.Builder(context, SEND_TEXTS_NOTIF_CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_menu_send)
                    .setContentTitle("Confirm appointments")
                    .setContentText("Time to confirm appointments")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    // TODO send view state to main activity
                    .setContentIntent(PendingIntent.getActivity(context, CONFIRM_APPTS_NOTIFICATION, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .build());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void ignored) {
        super.onPostExecute(ignored);
    }
}