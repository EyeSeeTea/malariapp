package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by idelcano on 14/11/2016.
 */

@Table(database = AppDatabase.class)
public class AttributeFlow   extends TempModel {

    @Column
    String code;

    public String getCode() {
        //return code;
        return code;
    }

    public void setCode(String code){
        this.code=code;
    }
}
