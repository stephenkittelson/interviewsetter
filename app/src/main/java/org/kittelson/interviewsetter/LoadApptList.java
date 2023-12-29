package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.kittelson.interviewsetter.appointments.Appointment;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class LoadApptList {
    private static String CLASS_NAME = LoadApptList.class.getSimpleName();

    private MainActivity context;
    private IllegalArgumentException spreadsheetException;
    private AppointmentsManager appointmentsManager;

    @Inject
    public LoadApptList(AppointmentsManager appointmentsManager) {
        this.appointmentsManager = appointmentsManager;
        spreadsheetException = null;
    }

    public LoadApptList setContext(MainActivity context) {
        this.context = context;
        return this;
    }

    public void execute(Account account) {
        new Thread(() -> {
            List<Appointment> appointments = new LinkedList<>();
            try {
                if (context.getViewState().equals(ApptViewState.TentativeAppts)) {
                    appointments = appointmentsManager.getTentativeAppointments(account, context);
                } else {
                    appointments = appointmentsManager.getAppointmentsToConfirm(context);
                }
            } catch (IllegalArgumentException ex) {
                spreadsheetException = ex;
            } catch (UserRecoverableAuthIOException ex) {
                // auth fix would have already been triggered - should never actually get here
            }
            if (context != null) {
                if (spreadsheetException == null) {
                    context.setAppointmentList(appointments);
                } else {
                    context.appointmentListLoadFailed(spreadsheetException.getMessage());
                }
            }
        }).start();
    }
}
