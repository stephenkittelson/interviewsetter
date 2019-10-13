package org.kittelson.interviewsetter;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SpreadsheetErrorDialogFragment extends DialogFragment {
    private String message;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        return builder.create();
    }

    public SpreadsheetErrorDialogFragment setErrorMessage(String message) {
        this.message = message;
        return this;
    }
}
