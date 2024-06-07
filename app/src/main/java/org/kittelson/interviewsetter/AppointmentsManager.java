package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import org.apache.commons.lang3.StringUtils;
import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.AppointmentStage;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class AppointmentsManager {
    private static final String CLASS_NAME = AppointmentsManager.class.getSimpleName();
    private static Pattern spreadsheetIdPattern = Pattern.compile("^https://docs.google.com/spreadsheets/d/(?<sheetId>[-_a-zA-Z0-9]+)/.*$");

    private SpreadsheetClient spreadsheetClient;

    @Inject
    public AppointmentsManager(SpreadsheetClient spreadsheetClient) {
        this.spreadsheetClient = spreadsheetClient;
    }

    public List<Appointment> getTentativeAppointments(Account account, Context context) throws UserRecoverableAuthIOException {
        return getAppointments(account, appt -> appt.getTime().isBefore(LocalDateTime.now().plusDays(7))
                && !appt.getStage().equals(AppointmentStage.Confirmed)
                && !appt.getStage().equals(AppointmentStage.Set), context);
    }

    public List<Appointment> getAppointmentsToConfirm(Account account, Context context) throws UserRecoverableAuthIOException {
        return getAppointments(account,
                appt -> appt.getTime().isBefore(LocalDateTime.now().plusDays(1).withHour(23))
                        && appt.getStage().equals(AppointmentStage.Set),
                context);
    }

    private List<Appointment> getAppointments(Account account, Predicate<Appointment> filter, Context context) throws UserRecoverableAuthIOException {
        List<Appointment> appointments = new LinkedList<>();
        Matcher sheetIdMatcher;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains(context.getString(R.string.googleSheetId_key))
                || StringUtils.isBlank(sharedPreferences.getString(context.getString(R.string.googleSheetId_key), ""))
                || !(sheetIdMatcher = spreadsheetIdPattern.matcher(sharedPreferences.getString(context.getString(R.string.googleSheetId_key), ""))).find()) {
            // no Google sheet ID has been specified - can't retrieve appointments
            return appointments;
        }
        try {
            Spreadsheet response = spreadsheetClient.getSpreadsheetData(
                    account, context, sheetIdMatcher.group("sheetId"));
            List<Appointment> allAppointments = response.getSheets().stream()
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
                                    )
                            )).collect(Collectors.toList());

            // validate rest of the sheet - mark duplicate appointments
            appointments = allAppointments.stream().peek(appt -> {
                if (allAppointments.stream().anyMatch(subAppt -> subAppt.getAppointmentType().equals(appt.getAppointmentType())
                        && appt.getCompanions().containsAll(subAppt.getCompanions())
                        && subAppt.getCompanions().containsAll(appt.getCompanions())
                        && !appt.getTime().equals(subAppt.getTime()))) {
                    appt.setDuplicate(true);
                }
            }).filter(filter).collect(Collectors.toList());
        } catch (UserRecoverableAuthIOException ex) {
            if (context instanceof MainActivity) {
                context.startActivity(ex.getIntent());
            } else {
                throw ex;
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (GoogleJsonResponseException ex) {
            if (ex.getDetails().getCode() == 404) {
                throw new IllegalArgumentException("Cannot find spreadsheet - invalid spreadsheet URL.");
            } else if (ex.getDetails().getCode() == 400) {
                throw new IllegalArgumentException("Can't load spreadsheet: " + ex.getDetails().getMessage());
            }
            Log.e(CLASS_NAME, "details: " + ex.getDetails());
            throw new IllegalArgumentException("failed to load spreadsheet: " + ex.getMessage(), ex);
        } catch (Exception e) {
            Log.e(CLASS_NAME, "failure to get spreadsheet: " + e.getClass().getName() + ": " + e.getMessage(), e);
            throw new IllegalArgumentException("failed to load spreadsheet: " + e.getMessage(), e);
        }
        return appointments;
    }

}
