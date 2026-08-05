package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.RowData;
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
    private static final String FAMILY_APPOINTMENT_TYPE = "Family";
    private static final String DEFAULT_APPOINTMENT_TYPE = "Ministering";

    // Column indices within a spreadsheet row.
    private static final int COL_DATE = 0;
    private static final int COL_TIME = 1;
    private static final int COL_PRESIDENCY_MEMBER = 2;
    private static final int COL_APPOINTMENT_TYPE = 3;
    private static final int COL_COMPANIONS = 4;
    private static final int COL_LOCATION = 5;
    private static final int COL_STAGE = 6;
    private static final int REQUIRED_COLUMN_COUNT = 7;

    private static Pattern spreadsheetIdPattern = Pattern.compile("^https://docs.google.com/spreadsheets/d/(?<sheetId>[-_a-zA-Z0-9]+)/.*$");

    private SpreadsheetClient spreadsheetClient;

    @Inject
    public AppointmentsManager(SpreadsheetClient spreadsheetClient) {
        this.spreadsheetClient = spreadsheetClient;
    }

    // "Tentative" appointments are ones the elders quorum presidency has proposed (stage =
    // TentativelySet) but hasn't locked in yet. Only those, within the next week, are worth
    // surfacing here - anything already Set/Confirmed belongs on the "to confirm" list instead,
    // and anything that's already passed is no longer actionable.
    public List<Appointment> getTentativeAppointments(Account account, Context context) throws UserRecoverableAuthIOException {
        return getAppointments(account, appt -> appt.getTime().isAfter(LocalDateTime.now())
                && appt.getTime().isBefore(LocalDateTime.now().plusDays(7))
                && appt.getStage().equals(AppointmentStage.TentativelySet), context);
    }

    // "Appointments to confirm" are ones that have been Set and are coming up within the next
    // day - a quick confirmation text is due. Anything already in the past is no longer
    // actionable.
    public List<Appointment> getAppointmentsToConfirm(Account account, Context context) throws UserRecoverableAuthIOException {
        return getAppointments(account,
                appt -> appt.getTime().isAfter(LocalDateTime.now())
                        && appt.getTime().isBefore(LocalDateTime.now().plusDays(1).withHour(23))
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
                                    .filter(AppointmentsManager::isUsableRow)
                                    .map(rowData -> new Appointment()
                                            .setTime(rowData.getValues().get(COL_DATE).getEffectiveValue().getNumberValue() + rowData.getValues().get(COL_TIME).getEffectiveValue().getNumberValue())
                                            .setPresidencyMember(rowData.getValues().get(COL_PRESIDENCY_MEMBER).getEffectiveValue().getStringValue())
                                            .setAppointmentType(appointmentTypeText(rowData))
                                            .setCompanions(rowData.getValues().get(COL_COMPANIONS).getEffectiveValue().getStringValue())
                                            .setLocation(cellStringValue(rowData, COL_LOCATION))
                                            .setStage(rowData.getValues().get(COL_STAGE).getEffectiveValue().getStringValue())
                                    )
                                    // a row with a companionship string that doesn't match any of
                                    // the known formats can't be texted, so treat it the same as a
                                    // missing/short row and just skip it rather than erroring out.
                                    .filter(appt -> appt.getCompanions() != null && !appt.getCompanions().isEmpty())
                            )).collect(Collectors.toList());

            // validate rest of the sheet - mark duplicate appointments
            appointments = allAppointments.stream().peek(appt -> {
                if (allAppointments.stream().filter(subAppt -> subAppt.getAppointmentType().equals(appt.getAppointmentType())
                        && appt.getCompanions().containsAll(subAppt.getCompanions())
                        && subAppt.getCompanions().containsAll(appt.getCompanions())
                        && !appt.getTime().equals(subAppt.getTime())).findAny().isPresent()) {
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

    // A row is usable if it has all 7 columns and every required field is populated. A row that
    // is simply too short (fewer than 7 columns) is not an error - it's just an unfinished/blank
    // row further down the sheet, and should be silently ignored rather than blowing up the whole
    // load. The location column is not required for Family visits, since those happen at the
    // family's home rather than a scheduled location.
    private static boolean isUsableRow(RowData rowData) {
        List<CellData> values = rowData.getValues();
        if (values == null || values.size() < REQUIRED_COLUMN_COUNT) {
            return false;
        }
        if (!hasNumericValue(values, COL_DATE) || !hasNumericValue(values, COL_TIME)) {
            return false;
        }
        if (isBlankStringCell(values, COL_PRESIDENCY_MEMBER)) {
            return false;
        }
        if (isBlankStringCell(values, COL_COMPANIONS)) {
            return false;
        }
        if (isBlankStringCell(values, COL_STAGE)) {
            return false;
        }
        boolean isFamilyVisit = FAMILY_APPOINTMENT_TYPE.equals(appointmentTypeText(rowData));
        if (!isFamilyVisit && isBlankStringCell(values, COL_LOCATION)) {
            return false;
        }
        return true;
    }

    // The appointment type column is the one optional column - a blank cell defaults to a
    // "Ministering" appointment.
    private static String appointmentTypeText(RowData rowData) {
        List<CellData> values = rowData.getValues();
        if (isBlankStringCell(values, COL_APPOINTMENT_TYPE)) {
            return DEFAULT_APPOINTMENT_TYPE;
        }
        return values.get(COL_APPOINTMENT_TYPE).getEffectiveValue().getStringValue().trim();
    }

    private static String cellStringValue(RowData rowData, int index) {
        CellData cell = rowData.getValues().get(index);
        return (cell == null || cell.getEffectiveValue() == null) ? null : cell.getEffectiveValue().getStringValue();
    }

    private static boolean isBlankStringCell(List<CellData> values, int index) {
        CellData cell = values.get(index);
        if (cell == null || cell.getEffectiveValue() == null) {
            return true;
        }
        return StringUtils.isBlank(cell.getEffectiveValue().getStringValue());
    }

    private static boolean hasNumericValue(List<CellData> values, int index) {
        CellData cell = values.get(index);
        return cell != null && cell.getEffectiveValue() != null && cell.getEffectiveValue().getNumberValue() != null;
    }

}
