package org.kittelson.interviewsetter2;

import android.Manifest;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;

import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.view.AppointmentAdapter;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String CLASS_NAME = MainActivity.class.getSimpleName();

    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";
    private static int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private RecyclerView appointmentView;
    private AppointmentAdapter appointmentAdapter;
    private RecyclerView.LayoutManager appointmentLayoutmanager;
    private List<Appointment> appointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appointmentView = (RecyclerView) findViewById(R.id.recycler_view);
        appointmentView.setHasFixedSize(true);
        appointmentLayoutmanager = new LinearLayoutManager(this);
        appointmentView.setLayoutManager(appointmentLayoutmanager);
        appointments = new LinkedList<>();
        appointmentAdapter = new AppointmentAdapter(appointments, appointmentView);
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

        getSupportLoaderManager().initLoader(0, null, this);

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
        } else {
            new GetScheduleTask(this).execute(GoogleSignIn.getLastSignedInAccount(this).getAccount());
        }

        new JobSchedulingManager().scheduleNextTextingJob(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    protected void reloadAppointments(List<Appointment> newAppointments) {
        appointmentAdapter.setAppointments(newAppointments);
        Log.v(CLASS_NAME, "reloading appointments with " + newAppointments);
        appointmentAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                new GetScheduleTask(this).execute(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class).getAccount());
            } catch (ApiException e) {
                Log.w(CLASS_NAME, "signInResult:failed code=" + e.getStatusCode() + ", reason: " + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()), e);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this,
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                ContactsContract.Contacts.DISPLAY_NAME + " = ? AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                new String[]{"Heidi"},
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToFirst();
            String phoneNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.v(CLASS_NAME, "phone number: " + phoneNumber);
//            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:5551; 5552; 5553"))
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber))
                    .putExtra("sms_body", "Testing..."));
        } else {
            Log.e(CLASS_NAME, "failed to find contact");
            Toast.makeText(this, "Failed to find contact", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
