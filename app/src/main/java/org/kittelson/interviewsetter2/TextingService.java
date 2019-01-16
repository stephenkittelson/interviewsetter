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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.time.ZonedDateTime;

public class TextingService extends JobService {
    private static String CLASS_NAME = TextingService.class.getSimpleName();


    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Override
    public boolean onStartJob(JobParameters params) {
        NotificationChannel channel = new NotificationChannel(NotifyWork.SEND_TEXTS_NOTIF_CHANNEL, "Send Text messages", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifies when it's time to send out text messages.");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        new NotifyWork(this).execute();
        new JobSchedulingManager().scheduleNextJobWithOffset(this, ZonedDateTime.now().plusDays(1).withHour(9).withMinute(0));
        /*
         TODO keep executing in window, just don't reactivate the notification if 1) it's already active, 2) no one is pending a text message - all in case
         someone responded that the day doesn't work for them
          */
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
