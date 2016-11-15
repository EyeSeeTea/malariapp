package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by idelcano on 14/11/2016.
 */

@Table(database = AppDatabase.class)
public class OrganisationUnitGroupFlow extends TempModel {


    @Column
    String organisationUnitId;

    public String getOrganisationUnitId() {
        //return code;
        return organisationUnitId;
    }

    public void setOrganisationUnitId(String organisationUnitId){
        this.organisationUnitId=organisationUnitId;
    }
}
