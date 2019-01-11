package org.kittelson.interviewsetter2;

import android.accounts.Account;
import android.os.AsyncTask;

import org.kittelson.interviewsetter2.appointments.Appointment;
import org.kittelson.interviewsetter2.appointments.AppointmentStage;

import java.time.LocalDateTime;
import java.util.List;

public class LoadUnconfirmedList extends AsyncTask<Account, Void, List<Appointment>> {
    private static String CLASS_NAME = LoadUnconfirmedList.class.getSimpleName();

    private MainActivity context;

    public LoadUnconfirmedList(MainActivity context) {
        this.context = context;
    }

    @Override
    protected List<Appointment> doInBackground(Account... accounts) {
        return new AppointmentsManager().getAppointments(accounts[0],
                appt -> appt.getTime().isBefore(LocalDateTime.now().plusWeeks(1)) && !appt.getStage().equals(AppointmentStage.Confirmed),
                context);
    }


    @Override
    protected void onPostExecute(List<Appointment> appointments) {
        super.onPostExecute(appointments);
        if (context != null) {
            context.reloadAppointments(appointments);
        }
    }
}
