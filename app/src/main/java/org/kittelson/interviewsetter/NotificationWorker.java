package org.kittelson.interviewsetter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.time.ZonedDateTime;

public class NotificationWorker extends Worker {
    private static String CLASS_NAME = NotificationWorker.class.getSimpleName();

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.v(CLASS_NAME, "starting job");
        NotificationChannel channel = new NotificationChannel(NotifyWork.SEND_TEXTS_NOTIF_CHANNEL, "Send Text messages", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifies when it's time to send out text messages.");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            Log.v(CLASS_NAME, "getting sign-in again");
            NotificationManagerCompat.from(this).notify(1001, new NotificationCompat.Builder(this, NotifyWork.SEND_TEXTS_NOTIF_CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_menu_send)
                    .setContentTitle("Login required")
                    .setContentText("Open the app to login")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(this, 1001, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .build());
        } else {
            Log.v(CLASS_NAME, "sign-on good, executing notifications");
            new NotifyWork(this).execute();
            new JobSchedulingManager().scheduleNextJobWithOffset(this, ZonedDateTime.now().plusDays(1).withHour(9).withMinute(0));
                /*
         TODO keep executing in window, just don't reactivate the notification if 1) it's already active, 2) no one is pending a text message - all in case
         someone responded that the day doesn't work for them
          */
        }
        return Result.success();
    }
}
