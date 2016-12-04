package org.eyeseetea.malariacare.sdk.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.eyeseetea.malariacare.database.AppDatabase;

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
public class ProgramAttributeValueFlow extends TempModel {
    @Column
    String attributeId;

    public String getAttributeId() {
        //return code;
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }
    @Column
    String program;

    public String getProgram() {
        //return code;
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }


    @Column
    String event;

    public String getEvent() {
        //return code;
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
    public String getValue() {
        return "";
    }
}
