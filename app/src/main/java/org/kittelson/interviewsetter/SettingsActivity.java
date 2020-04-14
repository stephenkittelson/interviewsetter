package org.kittelson.interviewsetter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    private static final String CLASS_NAME = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final int DOC_REQUEST_CODE = 93;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            EditTextPreference googleSheet = findPreference(getString(R.string.googleSheetId_key));
            googleSheet.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/vnd.google-apps.spreadsheet");
                startActivityForResult(intent, DOC_REQUEST_CODE);
                return true;
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (requestCode == DOC_REQUEST_CODE) {
                EditTextPreference googleSheet = findPreference(getString(R.string.googleSheetId_key));
                try (Cursor cursor = getActivity().getContentResolver().query(data.getData(), null, null, null, null, null)) {
                    cursor.moveToFirst();
                    String id = cursor.getString(0);
                    String documentId = cursor.getString(1);
                    String displayName = cursor.getString(2);
                    // TODO this won't give the sheet ID, might just need to create my own search function, or just use this and hope people don't have two docs with the same name, probably not with the same last modified date
                }
                googleSheet.setText(data.getDataString());
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}