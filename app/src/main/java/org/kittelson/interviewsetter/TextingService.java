package org.kittelson.interviewsetter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;

import java.time.ZonedDateTime;

public class TextingService extends JobService {
    private static String CLASS_NAME = TextingService.class.getSimpleName();

//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private JobParameters params;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
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

    public void finishJob() {
        jobFinished(params, false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
