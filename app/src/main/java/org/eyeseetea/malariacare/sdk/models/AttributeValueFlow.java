package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by idelcano on 14/11/2016.
 */

@Table(database = AppDatabase.class)
public class AttributeValueFlow  extends TempModel {

    public AttributeValueFlow getAttributeValue() {
        return this;
    }

    public AttributeFlow getAttribute() {
        return null;
    }

    public String getValue() {
        return null;
    }

    public String getCode() {
        return null;
    }

    public String getAttributeId() {
        return getAttribute().getUId();
    }

}
