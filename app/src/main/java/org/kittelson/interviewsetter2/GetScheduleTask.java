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

import org.kittelson.interviewsetter2.appointments.Appointment;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GetScheduleTask extends AsyncTask<Account, Void, List<Appointment>> {
    private static String CLASS_NAME = GetScheduleTask.class.getSimpleName();
    private static String SPREADSHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    private MainActivity context;

    public GetScheduleTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected List<Appointment> doInBackground(Account... accounts) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(SPREADSHEETS_SCOPE));
        credential.setSelectedAccount(accounts[0]);
        Sheets sheetsService = new Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).build();
        Spreadsheet response = null;
        List<Appointment> appointments = new LinkedList<>();
        try {
            response = sheetsService.spreadsheets().get("TODO").setRanges(Arrays.asList("'Upcoming Interviews'!A2:G30"))
                    .setFields("sheets.data.rowData.values.effectiveValue").execute();
            appointments = response.getSheets().stream()
                    .flatMap(sheet -> sheet.getData().stream()
                            .flatMap(gridData -> gridData.getRowData().stream()
                                    .filter(rowData -> rowData.getValues() != null && rowData.getValues().size() >= 7)
                                    .filter(rowData -> {
                                        for (int i = 0; i < 7; i++) {
                                            if (i != 3 && (rowData.getValues().get(i) == null || rowData.getValues().get(i).getEffectiveValue() == null)) {
                                                return false;
                                            }
                                        }
                                        return true;
                                    })
                                    .map(rowData -> new Appointment()
                                            .setTime(rowData.getValues().get(0).getEffectiveValue().getNumberValue() + rowData.getValues().get(1).getEffectiveValue().getNumberValue())
                                            .setPresidencyMember(rowData.getValues().get(2).getEffectiveValue().getStringValue())
                                            .setAppointmentType(rowData.getValues().get(3).getEffectiveValue() != null ? rowData.getValues().get(3).getEffectiveValue().getStringValue() : "Ministering")
                                            .setCompanions(rowData.getValues().get(4).getEffectiveValue().getStringValue())
                                            .setLocation(rowData.getValues().get(5).getEffectiveValue().getStringValue())
                                            .setStage(rowData.getValues().get(6).getEffectiveValue().getStringValue())
                                    ).filter(appt -> appt.getTime().isBefore(LocalDateTime.now().plusWeeks(1))))).collect(Collectors.toList());
            Log.v(CLASS_NAME, "appointments: " + appointments);
        } catch (UserRecoverableAuthIOException ex) {
            context.startActivity(ex.getIntent());
        } catch (IOException e) {
            Log.e(CLASS_NAME, "failure to get spreadsheet: " + e.getMessage(), e);
        }
        return appointments;
    }

    @Override
    protected void onPostExecute(List<Appointment> appointments) {
        super.onPostExecute(appointments);
        if (context != null) {
            context.reloadAppointments(appointments);
        }
    }
}
