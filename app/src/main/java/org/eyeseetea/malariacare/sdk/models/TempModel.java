package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.hisp.dhis.client.sdk.models.common.Access;
import org.joda.time.DateTime;

/**
 * Created by idelcano on 15/11/2016.
 */

/** This class will be disappeared soon.
 * This is a SDK Pojo BaseModel
 */
public class TempModel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    public long getId(){
        return id;
    }
    public void setId(){
        this.id=id;
    }
    //sdk model superclass methods

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
