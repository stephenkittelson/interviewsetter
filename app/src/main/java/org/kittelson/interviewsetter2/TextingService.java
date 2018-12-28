package org.kittelson.interviewsetter2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class TextingService extends JobService {

    private static final String SEND_TEXTS_NOTIF_CHANNEL = "sendTexts";

    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(this, "job executing!", Toast.LENGTH_LONG).show();
        Log.v("TextingService", "job running!");
        NotificationChannel channel = new NotificationChannel(SEND_TEXTS_NOTIF_CHANNEL, "Send Text messages", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifies when it's time to send out text messages to set appointments for the next Sunday.");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Intent pendingIntent = new Intent(this, SendTexts.class);

        NotificationManagerCompat.from(this).notify(0, new NotificationCompat.Builder(this, SEND_TEXTS_NOTIF_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Send out msgs")
                .setContentText("Time to setup appointments!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, pendingIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
