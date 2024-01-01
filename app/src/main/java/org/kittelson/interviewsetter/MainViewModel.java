package org.kittelson.interviewsetter;

import android.accounts.Account;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.kittelson.interviewsetter.appointments.Appointment;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<IllegalArgumentException> spreadsheetException = new MutableLiveData<>();

    private final AppointmentsManager appointmentsManager;

    @Inject
    public MainViewModel(AppointmentsManager appointmentsManager) {
        super();
        this.appointmentsManager = appointmentsManager;
    }

    public LiveData<List<Appointment>> getAppointments() {
        return appointments;
    }

    public LiveData<IllegalArgumentException> getSpreadsheetException() {
        return spreadsheetException;
    }

    public void loadAppointments(Account account, Context context, ApptViewState apptViewState) {
        new Thread(() -> {
            try {
                if (apptViewState.equals(ApptViewState.TentativeAppts)) {
                    appointments.postValue(appointmentsManager.getTentativeAppointments(account, context));
                } else {
                    appointments.postValue(appointmentsManager.getAppointmentsToConfirm(account, context));
                }
            } catch (IllegalArgumentException ex) {
                spreadsheetException.postValue(ex);
            } catch (UserRecoverableAuthIOException ex) {
                // auth fix would have already been triggered - should never actually get here
            }
        }).start();
    }
}
