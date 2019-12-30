package org.kittelson.interviewsetter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.view.AppointmentAdapter;
import org.kittelson.interviewsetter.persistence.GeneralData;
import org.kittelson.interviewsetter.persistence.GeneralDatabase;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Observer<GeneralData> {
    private static String CLASS_NAME = MainActivity.class.getSimpleName();

    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";
    private static final String IS_LICENSE_AGREED = "IsLicenseAgreed";

    private static int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private RecyclerView appointmentView;
    private AppointmentAdapter appointmentAdapter;
    private RecyclerView.LayoutManager appointmentLayoutmanager;
    private List<Appointment> appointments;
    private ApptViewState viewState;
    private ProgressBar progressBar;
    private boolean agreedToLicense;
    private GeneralDatabase generalDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        generalDatabase = Room.databaseBuilder(getApplicationContext(), GeneralDatabase.class, "general-data").build();
        agreedToLicense = false;
        if (savedInstanceState != null) {
            agreedToLicense = savedInstanceState.getBoolean(IS_LICENSE_AGREED);
        }
        if (!agreedToLicense) {
            LiveData<GeneralData> generalData = generalDatabase.generalDataDao().load();
            generalData.observe(this, this);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
    }

    private void setupScreen() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        viewState = ApptViewState.TentativeAppts;
        appointmentView = (RecyclerView) findViewById(R.id.recycler_view);
        appointmentLayoutmanager = new LinearLayoutManager(this);
        appointmentView.setLayoutManager(appointmentLayoutmanager);
        appointments = new LinkedList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointments, appointmentView);
        appointmentView.setAdapter(appointmentAdapter);
        registerForContextMenu(appointmentView);

        if (ContextCompat.checkSelfPermission(this, "android.permission.RECEIVE_BOOT_COMPLETED") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.INTERNET") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(SPREADSHEETS_SCOPE))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
        } else {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            new LoadApptList(this).execute(GoogleSignIn.getLastSignedInAccount(this).getAccount());
        }
//        ((Toolbar) findViewById(R.id.toolbar)).setTitle(viewState.toString());

        new JobSchedulingManager().scheduleNextTextingJob(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            if (viewState.equals(ApptViewState.TentativeAppts)) {
                viewState = ApptViewState.ApptsToConfirm;
            } else {
                viewState = ApptViewState.TentativeAppts;
            }
            ((Toolbar) findViewById(R.id.my_toolbar)).setTitle(viewState.toString());
            progressBar.setVisibility(ProgressBar.VISIBLE);
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                new LoadApptList(this).execute(GoogleSignIn.getLastSignedInAccount(this).getAccount());
            } else {
                startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Text individuals");
        menu.add(Menu.NONE, 1, Menu.NONE, "Smith, Donny");
        menu.add(Menu.NONE, 2, Menu.NONE, "Doe, Susan");
        // TODO store the appointment separately for use after clicking
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        item.getTitle();
        // TODO use stored appointment to populate text message
//        return super.onContextItemSelected(item);
        return false;
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

    public void appointmentListLoadFailed(String message) {
        appointmentAdapter.setAppointments(new LinkedList<>());
        appointmentAdapter.notifyDataSetChanged();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        new SpreadsheetErrorDialogFragment().setErrorMessage(message).show(getSupportFragmentManager(), "da tag");
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(IS_LICENSE_AGREED, agreedToLicense);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void acceptAgreement() {
        agreedToLicense = true;
        new Thread(() -> {
            generalDatabase.generalDataDao().save(new GeneralData(true));
        }).start();
        setupScreen();
    }

    public void rejectAgreement() {
        finish();
    }

    @Override
    public void onChanged(GeneralData generalData) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        agreedToLicense = generalData != null ? generalData.acceptedLicense : false;
        if (!agreedToLicense) {
            new LicenseAgreementDialogFragment(this).show(getSupportFragmentManager(), "da tag 2");
        } else {
            setupScreen();
        }
    }
}
