/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataValueExtended {

    private final static String TAG = ".DataValueExtended";
    private final static String REGEXP_FACTOR = ".*\\[([0-9]*)\\]";

    TrackedEntityDataValueFlow dataValue;

    String programUid;

    public DataValueExtended(TrackedEntityDataValueFlow dataValue) {
        this.dataValue = dataValue;
    }

    public DataValueExtended(DataValueExtended dataValueExtended) {
        this.dataValue = dataValueExtended.getDataValue();
    }

    public TrackedEntityDataValueFlow getDataValue() {
        return dataValue;
    }

    public static long count() {
        return new SQLite().selectCountOf()
                .from(TrackedEntityDataValueFlow.class)
                .count();
    }

    public String getProgramUid() {
        return programUid;
    }

    public void setProgramUid(String programUid) {
        this.programUid = programUid;
    }

    public String getEvent() {
        return dataValue.getEvent().getUId();
    }

    public String getDataElement() {
        return dataValue.getDataElement();
    }

    public String getValue() {
        return dataValue.getValue();
    }

    public void setDataElement(String uid) {
        dataValue.setDataElement(uid);
    }

    public void setEvent(EventFlow event) {
        dataValue.setEvent(event);
    }

    public void setValue(String round) {
        dataValue.setValue(round);
    }

    public void save() {
        System.out.println("Saving "+dataValue.getValue());
        String dataElement = dataValue.getDataElement();
        String value = dataValue.getValue();
        System.out.println(dataElement + " val " + value);
        dataValue.save();
    }

    public void update() {
        System.out.println("Updating"+dataValue.getValue());
        String dataElement = dataValue.getDataElement();
        String value = dataValue.getValue();
        System.out.println(dataElement + "val " + value);
        dataValue.update();
    }

    public static List<DataValueExtended> getExtendedList(
            List<TrackedEntityDataValueFlow> flowList) {
        List<DataValueExtended> extendedsList = new ArrayList<>();
        for (TrackedEntityDataValueFlow flowPojo : flowList) {
            extendedsList.add(new DataValueExtended(flowPojo));
        }
        return extendedsList;
    }
}
