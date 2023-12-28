package org.kittelson.interviewsetter;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module
@InstallIn(ActivityComponent.class)
public abstract class MainModule {
//    @Binds
//    public abstract SpreadsheetClient bindSpreadsheetClient(SpreadsheetClient spreadsheetClient);
}
