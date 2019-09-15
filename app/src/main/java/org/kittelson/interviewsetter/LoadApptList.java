package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.os.AsyncTask;

import org.kittelson.interviewsetter.appointments.Appointment;

import java.util.List;

public class LoadApptList extends AsyncTask<Account, Void, List<Appointment>> {
    private static String CLASS_NAME = LoadApptList.class.getSimpleName();

    private MainActivity context;

    public LoadApptList(MainActivity context) {
        this.context = context;
    }

    @Override
    protected List<Appointment> doInBackground(Account... accounts) {
        List<Appointment> appointments;
        if (context.getViewState().equals(ApptViewState.TentativeAppts)) {
            appointments = new AppointmentsManager().getTentativeAppointments(accounts[0], context);
        } else {
            appointments = new AppointmentsManager().getAppointmentsToConfirm(context);
        }
        return appointments;
    }

    @Override
    protected void onPostExecute(List<Appointment> appointments) {
        super.onPostExecute(appointments);
        if (context != null) {
            context.setAppointmentList(appointments);
        }
    }
}