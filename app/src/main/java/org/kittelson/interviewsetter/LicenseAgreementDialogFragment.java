package org.kittelson.interviewsetter;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LicenseAgreementDialogFragment extends DialogFragment {
    public static final String TERMS_OF_SERVICE = "Last updated 2019-12-15.\n" +
            "\n" +
            "BSD 2-Clause License\n" +
            "\n" +
            "Copyright (c) 2019, Stephen Kittelson\n" +
            "All rights reserved.\n" +
            "\n" +
            "Redistribution and use in source and binary forms, with or without " +
            "modification, are permitted provided that the following conditions are met:\n" +
            "\n" +
            "* Redistributions of source code must retain the above copyright notice, this " +
            "  list of conditions and the following disclaimer.\n" +
            "\n" +
            "* Redistributions in binary form must reproduce the above copyright notice, " +
            "  this list of conditions and the following disclaimer in the documentation " +
            "  and/or other materials provided with the distribution.\n" +
            "\n" +
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" " +
            "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE " +
            "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE " +
            "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE " +
            "FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL " +
            "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR " +
            "SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER " +
            "CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, " +
            "OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE " +
            "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" +
            "\n" +
            "Copyright notice: this app uses Material icons, copyright: Google.";

    public static final String PRIVACY_POLICY = "Privacy Policy\n" +
            "\n" +
            "This app searches your contacts for matches to the names read from the spreadsheet you configure it to read, and sets up a text message to those contacts via your default or chosen SMS app. It does not store any information in a persistent way, nor does it transmit any data to any other device or server.";

    private MainActivity mainActivity;

    public LicenseAgreementDialogFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(TERMS_OF_SERVICE + "\n\n\n" + PRIVACY_POLICY);
        builder.setNegativeButton(R.string.decline_license, (dialog, which) -> mainActivity.rejectAgreement());
        builder.setPositiveButton(R.string.accept_license, (dialog, which) -> mainActivity.acceptAgreement());
        return builder.create();
    }
}
