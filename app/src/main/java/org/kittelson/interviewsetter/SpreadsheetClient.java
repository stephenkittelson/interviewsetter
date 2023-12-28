package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;

import javax.inject.Inject;

public class SpreadsheetClient {
    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

    @Inject
    public SpreadsheetClient() {
    }

    public Spreadsheet getSpreadsheetData(Account account, Context context, Matcher sheetIdMatcher) throws IOException {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(SPREADSHEETS_SCOPE));
        credential.setSelectedAccount(account);
        Sheets.Builder sheetsServiceBuilder = new Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("InterviewSetter");
        final HttpRequestInitializer originalHttpRequestInitializer = sheetsServiceBuilder.getHttpRequestInitializer();
        Sheets sheetsService = sheetsServiceBuilder
                .setHttpRequestInitializer(request -> {
                    if (originalHttpRequestInitializer != null) {
                        originalHttpRequestInitializer.initialize(request);
                    }
                    request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
                    request.setNumberOfRetries(10);
                    request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff.Builder().setMaxIntervalMillis(60_000).build()));
                })
                .build();
        Spreadsheet response = sheetsService.spreadsheets().get(sheetIdMatcher.group("sheetId"))
                .setRanges(Arrays.asList("'Upcoming Interviews'!A2:G100"))
                .setFields("sheets.data.rowData.values.effectiveValue").execute();
        return response;
    }
}
