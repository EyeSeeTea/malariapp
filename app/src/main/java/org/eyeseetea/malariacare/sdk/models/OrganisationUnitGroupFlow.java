package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by idelcano on 14/11/2016.
 */

/** This class will be disappeared soon.
 * This is a SDK Pojo and is created to fix the Queries,
 * is in the app side and with hardcoded methods.
 * It makes the project compile and centralized the necessary methods
 * and necessary sdk new Pojos..
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
