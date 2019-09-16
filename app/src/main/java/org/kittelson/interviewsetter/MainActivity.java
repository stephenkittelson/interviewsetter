package org.kittelson.interviewsetter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;

import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.view.AppointmentAdapter;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String CLASS_NAME = MainActivity.class.getSimpleName();

    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    private static int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private RecyclerView appointmentView;
    private AppointmentAdapter appointmentAdapter;
    private RecyclerView.LayoutManager appointmentLayoutmanager;
    private List<Appointment> appointments;
    private ApptViewState viewState;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.action_settings, new SettingsFragment())
                .commit();
*/
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/

        viewState = ApptViewState.TentativeAppts;
        appointmentView = (RecyclerView) findViewById(R.id.recycler_view);
        appointmentLayoutmanager = new LinearLayoutManager(this);
        appointmentView.setLayoutManager(appointmentLayoutmanager);
        appointments = new LinkedList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointments, appointmentView);
        appointmentView.setAdapter(appointmentAdapter);

        if (ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.RECEIVE_BOOT_COMPLETED") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.INTERNET") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(SPREADSHEETS_SCOPE))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
        } else {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            new LoadApptList(this).execute(GoogleSignIn.getLastSignedInAccount(this).getAccount());
        }
//        ((Toolbar) findViewById(R.id.toolbar)).setTitle(viewState.toString());

        new JobSchedulingManager().scheduleNextTextingJob(this);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            if (viewState.equals(ApptViewState.TentativeAppts)) {
                viewState = ApptViewState.ApptsToConfirm;
            } else {
                viewState = ApptViewState.TentativeAppts;
            }
            ((Toolbar) findViewById(R.id.toolbar)).setTitle(viewState.toString());
            progressBar.setVisibility(ProgressBar.VISIBLE);
            new LoadApptList(this).execute(GoogleSignIn.getLastSignedInAccount(this).getAccount());
        });
*/
    }

    public ApptViewState getViewState() {
        return viewState;
    }

    public void setAppointmentList(List<Appointment> newAppointments) {
        appointmentAdapter.setAppointments(newAppointments);
        Log.v(CLASS_NAME, "reloading appointments with " + newAppointments);
        appointmentAdapter.notifyDataSetChanged();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                new LoadApptList(this).execute(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class).getAccount());
            } catch (ApiException e) {
                Log.w(CLASS_NAME, "signInResult: failed code=" + e.getStatusCode() + ", reason: " + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()), e);
            }
        }
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
            startActivity(new Intent(this, Settings3Activity.class));
//            Toast.makeText(this, "woohoo!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
