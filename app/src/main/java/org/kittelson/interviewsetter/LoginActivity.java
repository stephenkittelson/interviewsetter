package org.kittelson.interviewsetter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;

import static org.kittelson.interviewsetter.MainActivity.SPREADSHEETS_SCOPE;

public class LoginActivity extends AppCompatActivity {
    private static String CLASS_NAME = LoginActivity.class.getSimpleName();


    private static int RC_SIGN_IN = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            Log.v(CLASS_NAME, "getting sign-on again");
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(SPREADSHEETS_SCOPE))
                    .requestEmail()
                    .build();
            startActivityForResult(GoogleSignIn.getClient(this, gso).getSignInIntent(), RC_SIGN_IN);
        } else {
            Log.v(CLASS_NAME, "sign-on is still good, reusing");
            startService(new Intent(this, NotificationWorker.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                Log.v(CLASS_NAME, "sign-on complete, ensuring result and starting service again");
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class).getAccount();
                startService(new Intent(this, NotificationWorker.class));
            } catch (ApiException e) {
                Log.w(CLASS_NAME, "signInResult: failed code=" + e.getStatusCode() + ", reason: " + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()), e);
                throw new RuntimeException(e);
            }
        }
    }
}
