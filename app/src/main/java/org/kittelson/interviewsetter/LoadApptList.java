package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.kittelson.interviewsetter.appointments.Appointment;

import java.util.LinkedList;
import java.util.List;

public class LoadApptList extends AsyncTask<Account, Void, List<Appointment>> {
    private static String CLASS_NAME = LoadApptList.class.getSimpleName();

    private MainActivity context;
    private IllegalArgumentException spreadsheetException;

    public LoadApptList(MainActivity context) {
        this.context = context;
        spreadsheetException = null;
    }

    @Override
    protected List<Appointment> doInBackground(Account... accounts) {
        List<Appointment> appointments = new LinkedList<>();
        try {
            if (context.getViewState().equals(ApptViewState.TentativeAppts)) {
                appointments = new AppointmentsManager().getTentativeAppointments(accounts[0], context);
            } else {
                appointments = new AppointmentsManager().getAppointmentsToConfirm(context);
            }
        } catch (IllegalArgumentException ex) {
            spreadsheetException = ex;
        } catch (UserRecoverableAuthIOException ex) {
            // auth fix would have already been triggered - should never actually get here
        }
        return appointments;
    }

    @Override
    protected void onPostExecute(List<Appointment> appointments) {
        super.onPostExecute(appointments);
        if (context != null) {
            if (spreadsheetException == null) {
                context.setAppointmentList(appointments);
            } else {
                context.appointmentListLoadFailed(spreadsheetException.getMessage());
            }
        }
    }
}
