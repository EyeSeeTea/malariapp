package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;

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
public class DataValueFlow extends TempModel {
    @Column
    String event;

    public String getEvent() {
        //return code;
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Column
    String dataElement;

    public String getDataElement() {
        //return code;
        return dataElement;
    }

    public void setDataElement(String dataElement) {
        this.dataElement = dataElement;
    }

    private long localId;


    public String getValue() {
        return null;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public void setProvidedElsewhere(boolean b) {
        setProvidedElsewhere(b);
    }

    public void setStoredBy(String safeUsername) {
        setStoredBy(safeUsername);
    }

    public void setValue(String round) {
        setValue(round);
    }
}
