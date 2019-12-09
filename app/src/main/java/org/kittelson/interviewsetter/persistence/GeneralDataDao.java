package org.kittelson.interviewsetter.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GeneralDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(GeneralData generalData);

    @Query("select * from generaldata where id = 0")
    LiveData<GeneralData> load();
}
