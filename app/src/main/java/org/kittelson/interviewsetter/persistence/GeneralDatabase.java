package org.kittelson.interviewsetter.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {GeneralData.class}, version = 1)
public abstract class GeneralDatabase extends RoomDatabase {
    public abstract GeneralDataDao generalDataDao();
}
