package org.kittelson.interviewsetter2;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.RECEIVE_BOOT_COMPLETED") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 1);
        }
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        System.out.println("scheduling job...");
        if (jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(this, TextingService.class))
                .setPersisted(true)
//                .setMinimumLatency(millisToDateTime(ZonedDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(19).withMinute(0).withSecond(0)))
//                .setOverrideDeadline(millisToDateTime(ZonedDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)).withHour(20).withMinute(20).withSecond(0)))
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .build()) == JobScheduler.RESULT_FAILURE) {
            Toast.makeText(this, "job scheduling failed", Toast.LENGTH_LONG).show();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private long millisToDateTime(ZonedDateTime targetTime) {
        return (targetTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond()) * 1000;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
