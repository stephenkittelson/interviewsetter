package org.kittelson.interviewsetter2;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Covers {@link AppointmentsManager#getAppointmentsToConfirm} and
 * {@link AppointmentsManager#getTentativeAppointments}, which turn raw "Upcoming Interviews"
 * spreadsheet rows into {@link Appointment}s.
 * <p>
 * A few notes on tests below that don't exactly match the original scratch list of test names:
 * <ul>
 *     <li>{@code givenTooFewColumns_...} and {@code givenZeroColumns_...} were originally named as
 *     "thenThrowException" - that's incorrect. A short row is just an unfinished/blank row further
 *     down the sheet and should be silently ignored, not treated as an error. Renamed to
 *     "thenIgnoreRow" below.</li>
 *     <li>{@code givenTomorrowWithTimeBeforeCurrentTimeWithStageAtConfirmed_whenGetAppointmentsToConfirm}
 *     was listed as "thenReturnAppointment", but an already-Confirmed appointment doesn't belong on
 *     the "to confirm" list (that's the whole point of the Set -> Confirmed progression) - this
 *     looks like a copy/paste mistake from the Set-stage variant right above it. Fixed to
 *     "thenReturnEmpty" below.</li>
 *     <li>{@code givenNullInterviewType_...} was listed as "thenReturnEmpty", but a missing
 *     appointment type is meant to default to "Ministering" (see
 *     {@code AppointmentsManager.appointmentTypeText}), not drop the row. Fixed to
 *     "thenReturnAppointment" below.</li>
 *     <li>{@code givenTwoApptsOneCommonPersonInThirdPosition_...} - the app only ever parses one or
 *     two companions out of a cell (see {@link Appointment#setCompanions(String)}), so there's no
 *     real "third position" to exercise. Implemented as the single-companion case instead.</li>
 * </ul>
 */
public class AppointmentsManagerTest {

    private static final String SHEET_URL = "https://docs.google.com/spreadsheets/d/1Pu_1cGDWJd3BHgyOeu7M6dRGNZAd9U5ueTduf1BmrzI/edit?usp=sharing";
    private static final LocalDateTime SERIAL_EPOCH = LocalDateTime.of(1899, 12, 30, 0, 0);

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
        when(context.getString(R.string.googleSheetId_key)).thenReturn("googleSheetId");
    }

    // ------------------------------------------------------------------
    // Sheet ID resolution
    // ------------------------------------------------------------------

    @Test
    public void givenUnspecifiedGoogleSheetsId_whenGetAppointments_thenReturnEmpty() throws Exception {
        // sharedPreferences.contains(...) is unstubbed -> defaults to false
        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for unspecified google sheet ID",
                results, Matchers.empty());
    }

    @Test
    public void givenNullGoogleSheetId_whenGetAppointments_thenReturnEmpty() throws Exception {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn(null);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for a null google sheet ID",
                results, Matchers.empty());
    }

    @Test
    public void givenBlankGoogleSheetId_whenGetAppointments_thenReturnEmpty() throws Exception {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("   ");

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty for a blank google sheet ID",
                results, Matchers.empty());
    }

    @Test
    public void givenZeroRows_whenGetAppointments_thenReturnEmpty() throws Exception {
        stubSpreadsheet();

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("results should be empty when the sheet has no rows",
                results, Matchers.empty());
    }

    // ------------------------------------------------------------------
    // Row shape validation
    // ------------------------------------------------------------------

    @Test
    public void givenTooFewColumns_whenGetAppointments_thenIgnoreRow() throws Exception {
        RowData shortRow = new RowData().setValues(Arrays.asList(
                numericCell(serialFor(inHours(20))), numericCell(0.0), stringCell("Smith")));
        stubSpreadsheet(shortRow);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("a row with fewer than the required columns should be ignored, not error",
                results, Matchers.empty());
    }

    @Test
    public void givenZeroColumns_whenGetAppointments_thenIgnoreRow() throws Exception {
        RowData emptyRow = new RowData().setValues(new ArrayList<>());
        stubSpreadsheet(emptyRow);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("a row with zero columns should be ignored, not error",
                results, Matchers.empty());
    }

    // ------------------------------------------------------------------
    // Required field validation - each of these takes an otherwise-valid,
    // appointment-returning row and knocks out exactly one field.
    // ------------------------------------------------------------------

    @Test
    public void givenNullDate_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(null));
    }

    @Test
    public void givenBlankDate_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().dateBlank());
    }

    @Test
    public void givenNullTime_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().time(null));
    }

    @Test
    public void givenBlankTime_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().timeBlank());
    }

    @Test
    public void givenNullPresidencyMember_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().presidencyMember(null));
    }

    @Test
    public void givenBlankPresidencyMember_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().presidencyMember("   "));
    }

    @Test
    public void givenNullInterviewType_whenGetAppointments_thenReturnAppointment() throws Exception {
        // Renamed from "thenReturnEmpty": appointment type is the one optional column - a
        // missing value defaults to "Ministering" rather than dropping the row (see
        // AppointmentsManager.appointmentTypeText).
        RowData row = validSetRow().appointmentType(null).build();
        stubSpreadsheet(row);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("a missing appointment type should default rather than be dropped",
                results, Matchers.hasSize(1));
    }

    @Test
    public void givenNullCompanionship_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().companions(null));
    }

    @Test
    public void givenBlankCompanionship_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().companions("   "));
    }

    @Test
    public void givenInvalidCompanionship_whenGetAppointments_thenReturnEmpty() throws Exception {
        // doesn't match any of the "Last, First" companion patterns
        assertEmptyForToConfirm(validSetRow().companions("not a recognizable name format"));
    }

    @Test
    public void givenNullLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().appointmentType("Ministering").location(null));
    }

    @Test
    public void givenNullLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment() throws Exception {
        RowData row = validSetRow().appointmentType("Family").location(null).build();
        stubSpreadsheet(row);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("a family visit doesn't need a location", results, Matchers.hasSize(1));
    }

    @Test
    public void givenBlankLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().appointmentType("Ministering").location("   "));
    }

    @Test
    public void givenBlankLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment() throws Exception {
        RowData row = validSetRow().appointmentType("Family").location("   ").build();
        stubSpreadsheet(row);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat("a family visit doesn't need a location", results, Matchers.hasSize(1));
    }

    @Test
    public void givenNullStage_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().stage(null));
    }

    @Test
    public void givenBlankStage_whenGetAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().stage("   "));
    }

    @Test
    public void givenInvalidStage_whenGetAppointments_thenThrowException() throws Exception {
        RowData row = validSetRow().stage("Not A Real Stage").build();
        stubSpreadsheet(row);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentsManager.getAppointmentsToConfirm(account, context));
    }

    // ------------------------------------------------------------------
    // getTentativeAppointments window: only TentativelySet appointments in
    // the next 7 days, and never anything that's already in the past.
    // ------------------------------------------------------------------

    @Test
    public void givenTodayWithTimeInPast_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))));
    }

    @Test
    public void givenDateInPastWithTimeAfterCurrentTime_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().minusDays(1).withHour(23))));
    }

    @Test
    public void givenDateInPastWithTimeBeforeCurrentTime_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().minusDays(1).withHour(1))));
    }

    @Test
    public void givenDateInPastWithTimeEqualToCurrentTime_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().minusDays(1))));
    }

    @Test
    public void givenTodayWithTimeInFuture_whenGetTentativeAppointments_thenReturnAppointment() throws Exception {
        assertAppointmentForTentative(tentativeRow().date(serialFor(inHours(2))));
    }

    @Test
    public void givenTomorrowWithTimeBeforeCurrentTime_whenGetTentativeAppointments_thenReturnAppointment() throws Exception {
        assertAppointmentForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(1))));
    }

    @Test
    public void givenTomorrowWithTimeAfterCurrentTime_whenGetTentativeAppointments_thenReturnAppointment() throws Exception {
        assertAppointmentForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(23))));
    }

    @Test
    public void givenTomorrowWithTimeEqualToCurrentTime_whenGetTentativeAppointments_thenReturnAppointment() throws Exception {
        assertAppointmentForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(1))));
    }

    @Test
    public void givenDateInPastWithInitialContact_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))).stage("Initial Contact"));
    }

    @Test
    public void givenDateInPastWithAwaitingReply_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))).stage("Awaiting Reply"));
    }

    @Test
    public void givenDateInPastWithStageSet_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))).stage("Set"));
    }

    @Test
    public void givenDateInPastWithStageTentativelySet_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))).stage("Tentatively Set"));
    }

    @Test
    public void givenDateInPastWithStageConfirmed_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(hoursAgo(1))).stage("Confirmed"));
    }

    @Test
    public void givenInvalidDate_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        // a nonsensical serial value (e.g. 0) resolves to a date in 1899 - i.e. the past
        assertEmptyForTentative(tentativeRow().date(0.0).time(0.0));
    }

    @Test
    public void givenInvalidTime_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(0.0).time(0.0));
    }

    @Test
    public void givenDateMoreThanSevenDaysInTheFuture_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(8))));
    }

    @Test
    public void givenDateInTwoDaysWithStageAtInitialContact_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(2))).stage("Initial Contact"));
    }

    @Test
    public void givenDateInTwoDaysWithStageAtSet_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(2))).stage("Set"));
    }

    @Test
    public void givenDateInTwoDaysWithStageAtConfirmed_whenGetTentativeAppointments_thenReturnEmpty() throws Exception {
        assertEmptyForTentative(tentativeRow().date(serialFor(LocalDateTime.now().plusDays(2))).stage("Confirmed"));
    }

    // ------------------------------------------------------------------
    // getAppointmentsToConfirm window: only Set appointments in the next
    // day, and never anything that's already in the past.
    // ------------------------------------------------------------------

    @Test
    public void givenTodayWithTimeInPast_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)));
    }

    @Test
    public void givenDateInPastWithTimeAfterCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().minusDays(1).withHour(23))));
    }

    @Test
    public void givenDateInPastWithTimeBeforeCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().minusDays(1).withHour(1))));
    }

    @Test
    public void givenDateInPastWithTimeEqualToCurrentTime_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().minusDays(1))));
    }

    @Test
    public void givenTodayWithTimeInFutureAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment() throws Exception {
        assertAppointmentForToConfirm(validSetRow().date(serialFor(inHours(2))));
    }

    @Test
    public void givenTomorrowWithTimeBeforeCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment() throws Exception {
        assertAppointmentForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(1))));
    }

    @Test
    public void givenTomorrowWithTimeAfterCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment() throws Exception {
        assertAppointmentForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(22))));
    }

    @Test
    public void givenTomorrowWithTimeEqualToCurrentTimeAndStageIsSet_whenGetAppointmentsToConfirm_thenReturnAppointment() throws Exception {
        assertAppointmentForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(1))));
    }

    @Test
    public void givenDateInPastWithStageAtInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)).stage("Initial Contact"));
    }

    @Test
    public void givenDateInPastWithStageAtAwaitingReply_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)).stage("Awaiting Reply"));
    }

    @Test
    public void givenDateInPastWIthStageAtSet_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)));
    }

    @Test
    public void givenDateInPastWithStageAtTentativelySet_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)).stage("Tentatively Set"));
    }

    @Test
    public void givenDateInPastWithStageAtConfirmed_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(hoursAgoSerial(1)).stage("Confirmed"));
    }

    @Test
    public void givenInvalidDate_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(0.0).time(0.0));
    }

    @Test
    public void givenInvalidTime_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(0.0).time(0.0));
    }

    @Test
    public void givenDateInTwoDaysWithStageAtSet_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(2))));
    }

    @Test
    public void givenTodayWithTimeInFutureAndStageIsConfirmed_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(inHours(2))).stage("Confirmed"));
    }

    @Test
    public void givenTodayWithTimeInFutureAndStageIsInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(inHours(2))).stage("Initial Contact"));
    }

    @Test
    public void givenTomorrowWithTimeAfterCurrentTimeWithStageAtInitialContact_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(22))).stage("Initial Contact"));
    }

    @Test
    public void givenTomorrowWithTimeBeforeCurrentTimeWithStageAtConfirmed_whenGetAppointmentsToConfirm_thenReturnEmpty() throws Exception {
        // Confirmed appointments have already moved past the "needs confirming" step.
        assertEmptyForToConfirm(validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(1))).stage("Confirmed"));
    }

    // ------------------------------------------------------------------
    // Duplicate detection - flags two appointments of the same type with
    // the exact same companionship, scheduled at two different times.
    // ------------------------------------------------------------------

    @Test
    public void givenTwoApptsOneCommonPersonInFirstPosition_whenGetAppointments_thenMarkDuplicate() throws Exception {
        assertBothMarkedDuplicate("Doe, John / Curie, Sam", "Doe, John / Curie, Sam");
    }

    @Test
    public void givenTwoApptsOneCommonPersonInSecondPosition_whenGetAppointments_thenMarkDuplicate() throws Exception {
        // same two companions, listed in the opposite order in the sheet text - the set of
        // companions is what matters, not the order they were typed in.
        assertBothMarkedDuplicate("Doe, John / Curie, Sam", "Curie, Sam / Doe, John");
    }

    @Test
    public void givenTwoApptsOneCommonPersonInThirdPosition_whenGetAppointments_thenMarkDuplicate() throws Exception {
        // the app only ever parses one or two companions per row - this is the single-companion
        // equivalent of the above two cases.
        assertBothMarkedDuplicate("Doe, John", "Doe, John");
    }

    @Test
    public void givenTwoApptsSameTypeNoCommonPerson_whenGetAppointments_thenNoDuplicates() throws Exception {
        RowData row1 = validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(10))).companions("Doe, John").build();
        RowData row2 = validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(14))).companions("Miller, Amy").build();
        stubSpreadsheet(row1, row2);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat(results, Matchers.hasSize(2));
        results.forEach(appt -> MatcherAssert.assertThat("appointments with different companions shouldn't be marked duplicate",
                appt.isDuplicate(), Matchers.is(false)));
    }

    private void assertBothMarkedDuplicate(String companions1, String companions2) throws Exception {
        RowData row1 = validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(10))).companions(companions1).build();
        RowData row2 = validSetRow().date(serialFor(LocalDateTime.now().plusDays(1).withHour(14))).companions(companions2).build();
        stubSpreadsheet(row1, row2);

        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);

        MatcherAssert.assertThat(results, Matchers.hasSize(2));
        results.forEach(appt -> MatcherAssert.assertThat("both appointments for the same companionship/type at different times should be flagged duplicate",
                appt.isDuplicate(), Matchers.is(true)));
    }

    // ------------------------------------------------------------------
    // Test data helpers
    // ------------------------------------------------------------------

    /** A row that satisfies {@code getAppointmentsToConfirm}: Set stage, later today. */
    private RowBuilder validSetRow() {
        return new RowBuilder().date(serialFor(inHours(2))).stage("Set");
    }

    /** A row that satisfies {@code getTentativeAppointments}: TentativelySet stage, later today. */
    private RowBuilder tentativeRow() {
        return new RowBuilder().date(serialFor(inHours(2))).stage("TentativelySet");
    }

    private void assertEmptyForToConfirm(RowBuilder row) throws Exception {
        stubSpreadsheet(row.build());
        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);
        MatcherAssert.assertThat(results, Matchers.empty());
    }

    private void assertAppointmentForToConfirm(RowBuilder row) throws Exception {
        stubSpreadsheet(row.build());
        List<Appointment> results = appointmentsManager.getAppointmentsToConfirm(account, context);
        MatcherAssert.assertThat(results, Matchers.hasSize(1));
    }

    private void assertEmptyForTentative(RowBuilder row) throws Exception {
        stubSpreadsheet(row.build());
        List<Appointment> results = appointmentsManager.getTentativeAppointments(account, context);
        MatcherAssert.assertThat(results, Matchers.empty());
    }

    private void assertAppointmentForTentative(RowBuilder row) throws Exception {
        stubSpreadsheet(row.build());
        List<Appointment> results = appointmentsManager.getTentativeAppointments(account, context);
        MatcherAssert.assertThat(results, Matchers.hasSize(1));
    }

    private void stubSpreadsheet(RowData... rows) throws Exception {
        when(sharedPreferences.contains(anyString())).thenReturn(true);
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn(SHEET_URL);
        Spreadsheet spreadsheet = new Spreadsheet().setSheets(Arrays.asList(
                new Sheet().setData(Arrays.asList(
                        new GridData().setRowData(Arrays.asList(rows))))));
        when(spreadsheetClient.getSpreadsheetData(any(), any(), anyString())).thenReturn(spreadsheet);
    }

    private static LocalDateTime inHours(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    private static LocalDateTime hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }

    private static Double hoursAgoSerial(int hours) {
        return serialFor(hoursAgo(hours));
    }

    /** Converts a LocalDateTime to the Google Sheets serial-date-number format. */
    private static Double serialFor(LocalDateTime dateTime) {
        return ChronoUnit.MINUTES.between(SERIAL_EPOCH, dateTime) / (24.0 * 60.0);
    }

    private static CellData numericCell(Double value) {
        return value == null ? null : new CellData().setEffectiveValue(new ExtendedValue().setNumberValue(value));
    }

    private static CellData blankNumericCell() {
        return new CellData().setEffectiveValue(new ExtendedValue());
    }

    private static CellData stringCell(String value) {
        return value == null ? null : new CellData().setEffectiveValue(new ExtendedValue().setStringValue(value));
    }

    /** Fluent builder for a 7-column "Upcoming Interviews" row, defaulting to a fully valid row. */
    private static class RowBuilder {
        private CellData date = numericCell(serialFor(LocalDateTime.now().plusHours(2)));
        private CellData time = numericCell(0.0);
        private CellData presidencyMember = stringCell("Smith");
        private CellData appointmentType = stringCell("Ministering");
        private CellData companions = stringCell("Doe, John");
        private CellData location = stringCell("the Bishop's office");
        private CellData stage = stringCell("Set");

        RowBuilder date(Double serial) {
            this.date = numericCell(serial);
            return this;
        }

        RowBuilder dateBlank() {
            this.date = blankNumericCell();
            return this;
        }

        RowBuilder time(Double serial) {
            this.time = numericCell(serial);
            return this;
        }

        RowBuilder timeBlank() {
            this.time = blankNumericCell();
            return this;
        }

        RowBuilder presidencyMember(String value) {
            this.presidencyMember = stringCell(value);
            return this;
        }

        RowBuilder appointmentType(String value) {
            this.appointmentType = stringCell(value);
            return this;
        }

        RowBuilder companions(String value) {
            this.companions = stringCell(value);
            return this;
        }

        RowBuilder location(String value) {
            this.location = stringCell(value);
            return this;
        }

        RowBuilder stage(String value) {
            this.stage = stringCell(value);
            return this;
        }

        RowData build() {
            return new RowData().setValues(Arrays.asList(date, time, presidencyMember, appointmentType, companions, location, stage));
        }
    }
}
