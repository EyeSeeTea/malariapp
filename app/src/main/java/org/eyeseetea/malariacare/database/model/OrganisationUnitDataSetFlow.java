package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.hisp.dhis.client.sdk.models.common.Access;
import com.raizlabs.android.dbflow.structure.BaseModel;
import org.joda.time.DateTime;

/**
 * Created by idelcano on 15/11/2016.
 */

/** This class will be disappeared soon.
 * This is a SDK Pojo and is created to fix the Queries,
 * is in the app side and with hardcoded methods.
 * It makes the project compile and centralized the necessary methods
 * and necessary sdk new Pojos..
 */
@Table(database = AppDatabase.class)
public class OrganisationUnitDataSetFlow  extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_flow;
    public Long getId_flow() {
        return id_flow;
    }

    public void setId_flow(Long id_flow) {
        this.id_flow = id_flow;
    }
    public OrganisationUnitDataSetFlow(){}
    @Column
    String organisationUnitId;

    public String getOrganisationUnitId() {
        //return code;
        return organisationUnitId;
    }

    public void setOrganisationUnitId(String organisationUnitId){
        this.organisationUnitId=organisationUnitId;
    }
    @Column(name = "name")
    String name;

    @Column(name = "displayName")
    String displayName;

    @Column(name = "created")
    DateTime created;

    @Column(name = "lastUpdated")
    DateTime lastUpdated;

    @Column(name = "access")
    Access access;

    @Column(name = "apiSortOrder")
    int apiSortOrder;

    public final String getUId() {
        return null;
    }

    public final void setUId(String uId) {

    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public final DateTime getCreated() {
        return created;
    }

    public final void setCreated(DateTime created) {
        this.created = created;
    }

    public final DateTime getLastUpdated() {
        return lastUpdated;
    }

    public final void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public final Access getAccess() {
        return access;
    }

    public final void setAccess(Access access) {
        this.access = access;
    }

    public void setApiSortOrder(int sortOrder) {
        this.apiSortOrder = sortOrder;
    }

    public int getApiSortOrder() {
        return apiSortOrder;
    }
}
