package org.kittelson.interviewsetter2;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GetScheduleTask extends AsyncTask<Account, Void, List<String>> {
    private static String CLASS_NAME = GetScheduleTask.class.getSimpleName();
    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    private Context context;

    public GetScheduleTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<String> doInBackground(Account... accounts) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(SPREADSHEETS_SCOPE));
        credential.setSelectedAccount(accounts[0]);
        Sheets sheetsService = new Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).build();
        Spreadsheet response = null;
        try {
            response = sheetsService.spreadsheets().get("TODO").setRanges(Arrays.asList("'Upcoming Interviews'!A2:F30")).execute();
        } catch (UserRecoverableAuthIOException ex) {
            context.startActivity(ex.getIntent());
        } catch (IOException e) {
            Log.e(CLASS_NAME, "failure to get spreadsheet: " + e.getMessage(), e);
        }
        Log.v(CLASS_NAME, "response from google sheets: " + response);
        return null;
    }
}
