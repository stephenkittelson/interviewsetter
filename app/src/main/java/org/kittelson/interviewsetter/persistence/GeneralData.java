package org.kittelson.interviewsetter.persistence;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GeneralData {
    @PrimaryKey
    public Integer id;
    public Boolean acceptedLicense;

    public GeneralData(Boolean acceptedLicense) {
        this.id = 0;
        this.acceptedLicense = acceptedLicense;
    }


}
