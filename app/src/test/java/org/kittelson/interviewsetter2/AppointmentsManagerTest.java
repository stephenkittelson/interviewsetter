package org.kittelson.interviewsetter2;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.kittelson.interviewsetter.AppointmentsManager;
import org.kittelson.interviewsetter.R;
import org.kittelson.interviewsetter.SpreadsheetClient;
import org.mockito.Mockito;

public class AppointmentsManagerTest {

    private SpreadsheetClient spreadsheetClient;
    private AppointmentsManager appointmentsManager;
    private Context context;

    @Before
    public void setup() {
        spreadsheetClient = Mockito.mock(SpreadsheetClient.class);
        appointmentsManager = new AppointmentsManager(spreadsheetClient);
        context = Mockito.mock(Context.class);
    }

    @Test
    public void givenUnspecifiedGoogleSheetsId_whenGetAppointments_thenReturnEmpty() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("googleSheetId_key", "TODO");
//        editor.apply();
        // TODO write unit test
    }

    /*
    givenNullGoogleSheetId_whenGetAppointments_thenReturnEmpty
    givenBlankGoogleSheetId_whenGetAppointments_thenReturnEmpty
    givenTooFewColumns_whenGetAppointments_thenThrowException
    givenZeroRows_whenGetAppointments_thenReturnEmpty
    givenZeroColumns_whenGetAppointments_thenThrowException
    givenNullDate_whenGetAppointments_thenReturnEmpty
    givenBlankDate_whenGetAppointments_thenReturnEmpty
    givenNullTime_whenGetAppointments_thenReturnEmpty
    givenBlankTime_whenGetAppointments_thenReturnEmpty
    givenNullPresidencyMember_whenGetAppointments_thenReturnEmpty
    givenBlankPresidencyMember_whenGetAppointments_thenReturnEmpty
    givenNullInterviewType_whenGetAppointments_thenReturnEmpty
    givenNullCompanionship_whenGetAppointments_thenReturnEmpty
    givenBlankCompanionship_whenGetAppointments_thenReturnEmpty
    givenNullLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty
    givenNullLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment
    givenBlankLocationAndNotFamilyVisit_whenGetAppointments_thenReturnEmpty
    givenBlankLocationForFamilyVisit_whenGetAppointments_thenReturnAppointment
    givenNullStage_whenGetAppointments_thenReturnEmpty
    givenBlankStage_whenGetAppointments_thenReturnEmpty
    givenInvalidCompanionship_whenGetAppointments_thenReturnEmpty
    givenInvalidStage_whenGetAppointments_thenThrowException

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