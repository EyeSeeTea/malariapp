package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by idelcano on 14/11/2016.
 */

@Table(database = AppDatabase.class)
public class OrganisationUnitLevelFlow extends TempModel {

    public String getUid() {
        //return getUId();
        return null;
    }

    public int getLevel() {
        //// TODO: 15/11/2016  
        return 0;
    }
}
