package org.kittelson.interviewsetter2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.kittelson.interviewsetter.AppointmentsManager;
import org.kittelson.interviewsetter.R;
import org.kittelson.interviewsetter.SpreadsheetClient;
import org.kittelson.interviewsetter.appointments.Appointment;
import org.kittelson.interviewsetter.appointments.AppointmentStage;
import org.kittelson.interviewsetter.appointments.AppointmentType;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class AppointmentsManagerTest {

    private SpreadsheetClient spreadsheetClient;
    private AppointmentsManager appointmentsManager;
    private SharedPreferences sharedPreferences;
    private Context context;
    private Account account;

    @Before
    public void setup() {
        spreadsheetClient = mock(SpreadsheetClient.class);
        appointmentsManager = new AppointmentsManager(spreadsheetClient);
        context = mock(Context.class);
        account = mock(Account.class);
        sharedPreferences = mock(SharedPreferences.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
    }

    @Test
    public void givenUnspecifiedGoogleSheetsId_whenGetAppointments_thenReturnEmpty() throws IOException, GeneralSecurityException {
        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for unspecified google sheet ID",
                results, Matchers.empty());
    }

    @Test
    public void givenNullGoogleSheetId_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(any(), any())).thenReturn(null);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for null google sheet ID",
                results, Matchers.empty());
    }

    @Test
    public void givenBlankGoogleSheetId_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(any(), any())).thenReturn(" \t \n \r ");

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for blank google sheet ID",
                results, Matchers.empty());


        // TODO write unit test
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenTooFewColumns_whenGetAppointments_thenThrowException() throws IOException, GeneralSecurityException {
        setGoodGoogleSheetId();
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(12.0)),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12))
                                ))))))
                        )));

        appointmentsManager.getAppointmentsToConfirm(account, context);
        // TODO write unit test
    }

    private void setGoodGoogleSheetId() {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(any(), any())).thenReturn("https://docs.google.com/spreadsheets/d/1Pu_1cGDWJd3BHgyOeu7M6dRGNZAd9U5ueTduf1BmrzI/edit?usp=sharing");
//        editor.putString("googleSheetId_key", );
    }

    @Test
    public void givenZeroRows_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(Collections.emptyList())))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when zero rows are returned",
                results, Matchers.empty());
        // TODO write unit test
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenZeroColumns_whenGetAppointments_thenThrowException() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(Collections.emptyList())))))
                        )));

        appointmentsManager.getAppointmentsToConfirm(account, context);
        // TODO write unit test
    }

    @Test
    public void givenNullDate_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(null),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12)),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Young")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the date is null", results,
                Matchers.empty());
        // TODO write unit test
    }

    @Test
    public void givenBlankDate_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12)),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Young")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the date is blank", results,
                Matchers.empty());
        // TODO write unit test
    }

    private double convertDateToNumber(LocalDate date) {
        return (double) ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), LocalDate.now());
    }

    @Test
    public void givenNullTime_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(convertDateToNumber(LocalDate.now()))),
                                        new CellData().setEffectiveValue(null),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Young")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the time is null", results,
                Matchers.empty());
        // TODO write unit test
    }

    @Test
    public void givenBlankTime_whenGetAppointments_thenReturnEmpty() throws IOException, GeneralSecurityException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(convertDateToNumber(LocalDate.now()))),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Young")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the time is blank", results,
                Matchers.empty());
        // TODO write unit test
    }

    @Test
    public void givenNullPresidencyMember_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(convertDateToNumber(LocalDate.now().plusDays(1L)))),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12)),
                                        new CellData().setEffectiveValue(null),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the presidency member is null", results,
                Matchers.empty());
        // TODO write unit test
    }

    @Test
    public void givenBlankPresidencyMember_whenGetAppointments_thenReturnEmpty() throws GeneralSecurityException, IOException {
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(convertDateToNumber(LocalDate.now().plusDays(1L)))),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12)),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("  \n \r \t ")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the presidency member is blank", results,
                Matchers.empty());
        // TODO write unit test
    }

    @Test
    public void givenNullInterviewType_whenGetAppointments_thenReturnMinisteringAppointment() throws GeneralSecurityException, IOException {
        // TODO maybe just assume ministering interview
        setGoodGoogleSheetId();
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString()))
                .thenReturn(new Spreadsheet()
                        .setSheets(List.of(
                                new Sheet().setData(List.of(new GridData().setRowData(List.of(new RowData().setValues(List.of(
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(convertDateToNumber(LocalDate.now().plusDays(1L)))),
                                        new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(0.12)),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Young")),
                                        new CellData().setEffectiveValue(null),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Lastington, Test1")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("outside of Bishop's office")),
                                        new CellData().setEffectiveValue(new ExtendedValue().setStringValue("Initial Contact"))
                                ))))))
                        )));

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be appointment when the interview type is null", results,
                Matchers.contains(Matchers.equalTo(new Appointment()
                        .setCompanions("Lastington, Test1")
                        .setAppointmentType(AppointmentType.Ministering)
                        .setLocation("outside of Bishop's office")
                        .setStage(AppointmentStage.InitialContact))));
        // TODO write unit test - not working right
    }

    @Test
    public void givenNullCompanionship_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenBlankCompanionship_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenNullLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenNullLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment() {
        // TODO write unit test
    }

    @Test
    public void givenBlankLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenBlankLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment() {
        // TODO write unit test
    }

    @Test
    public void givenNullStage_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenBlankStage_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenInvalidCompanionship_whenGetAppointments_thenReturnEmpty() {
        // TODO write unit test
    }

    @Test
    public void givenInvalidStage_whenGetAppointments_thenThrowException() {
        // TODO write unit test
    }

/*
    givenTodayWithTimeInPast_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithTimeAfterCurrentTime_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithTimeBeforeCurrentTime_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithTimeEqualToCurrentTime_whenGetTentativeAppointments_thenReturnEmpty
    givenTodayWithTimeInFuture_whenGetTentativeAppointments_thenReturnAppointment
    givenTomorrowWithTimeBeforeCurrentTime_whenGetTentativeAppointments_thenReturnAppointment
    givenTomorrowWithTimeAfterCurrentTime_whenGetTentativeAppointments_thenReturnAppointment
    givenTomorrowWithTimeEqualToCurrentTime_whenGetTentativeAppointments_thenReturnAppointment
    givenDateInPastWithInitialContact_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithAwaitingReply_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithStageSet_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithStageTentativelySet_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInPastWithStageConfirmed_whenGetTentativeAppointments_thenReturnEmpty
    givenInvalidDate_whenGetTentativeAppointments_thenReturnEmpty
    givenInvalidTime_whenGetTentativeAppointments_thenReturnEmpty
    givenDateMoreThanSevenDaysInTheFuture_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInTwoDaysWithStageAtInitialContact_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInTwoDaysWithStageAtSet_whenGetTentativeAppointments_thenReturnEmpty
    givenDateInTwoDaysWithStageAtConfirmed_whenGetTentativeAppointments_thenReturnEmpty

    givenTodayWithTimeInPast_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithTimeAfterCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithTimeBeforeCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithTimeEqualToCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenTodayWithTimeInFutureAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment
    givenTomorrowWithTimeBeforeCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment
    givenTomorrowWithTimeAfterCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment
    givenTomorrowWithTimeEqualToCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment
    givenDateInPastWithStageAtInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithStageAtAwaitingReply_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWIthStageAtSet_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithStageAtTentativelySet_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInPastWithStageAtConfirmed_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenInvalidDate_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenInvalidTime_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenDateInTwoDaysWithStageAtSet_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenTodayWithTimeInFutureAndStageIsConfirmed_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenTodayWithTimeInFutureAndStageIsInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenTomorrowWithTimeAfterCurrentTimeWithStageAtInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty
    givenTomorrowWithTimeBeforeCurrentTimeWithStageAtConfirmed_whenGetAppointmentsToConfirm_thenReturnAppointment

    givenTwoApptsOneCommonPersonInFirstPosition_whenGetAppointments_thenMarkDuplicate
    givenTwoApptsOneCommonPersonInSecondPosition_whenGetAppointments_thenMarkDuplicate
    givenTwoApptsOneCommonPersonInThirdPosition_whenGetAppointments_thenMarkDuplicate
    givenTwoApptsSameTypeNoCommonPerson_whenGetAppointments_thenNoDuplicates
     */
}