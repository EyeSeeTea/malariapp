/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.sdk.models;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.sdk.SdkController;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;
import org.hisp.dhis.client.sdk.models.event.Event.EventStatus;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by idelcano on 09/11/2016.
 */

public class Event extends EventFlow {

    public static final EventStatus STATUS_ACTIVE = EventStatus.ACTIVE;
    public static final EventStatus STATUS_COMPLETED = EventStatus.COMPLETED;
    public static final EventStatus STATUS_FUTURE_VISIT = EventStatus.SCHEDULE;
    public static final EventStatus STATUS_SKIPPED = EventStatus.SKIPPED;

    public void setFromServer(boolean value) {
        //// FIXME: 09/11/2016
    }
    public void setOrganisationUnitId(String uid) {
        setOrgUnit(uid);
    }
    public void setProgramId(String uid) {
        setProgram(uid);
    }
    public void setProgramStageId(String uid) {
        setProgramStage(uid);
    }

    public void setLastUpdated(String dateAsString){
        Date date=null;
        try {
            date=EventExtended.parseDate(dateAsString, EventExtended.DHIS2_GMT_DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        super.setLastUpdated(new DateTime(date));
    }

    public void setDueDate(String dateAsString) {
        Date date=null;
        try {
            date=EventExtended.parseDate(dateAsString, EventExtended.DHIS2_GMT_DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.setDueDate(new DateTime(date));
    }

    public void setEventDate(String dateAsString) {
        Date date=null;
        try {
            date=EventExtended.parseDate(dateAsString, EventExtended.DHIS2_GMT_DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        super.setEventDate(new DateTime(date));
    }

    public String getEventDateFormatted(){
        Date date=super.getEventDate().toDate();
        return EventExtended.format(date, EventExtended.DHIS2_GMT_DATE_FORMAT);
    }

    public long getLocalId() {
        return getId();
    }

    public EventFlow getEvent() {
        return null;
    }

    public String getUid() {
        return getUId();
    }

    public void setDataValues(Object o) {
    }

    public String getOrganisationUnitId() {
        return getOrgUnit();
    }

    public String getProgramStageId() {
        return super.getProgramStage();
    }

    public List<DataValue> getDataValues() {
        //// FIXME: 09/11/2016
        return null;
    }

    public String getProgramId() {
        return getProgram();
    }
}
