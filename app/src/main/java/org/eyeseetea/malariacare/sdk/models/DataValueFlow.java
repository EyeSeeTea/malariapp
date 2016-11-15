package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;

/**
 * Created by idelcano on 14/11/2016.
 */

@Table(database = AppDatabase.class)
public class DataValueFlow extends TempModel {
    public String getValue() {
        return null;
    }

    public DataElementFlow getDataElement() {
        return null;
    }

    public EventFlow getEvent() {
        return null;
    }
}
