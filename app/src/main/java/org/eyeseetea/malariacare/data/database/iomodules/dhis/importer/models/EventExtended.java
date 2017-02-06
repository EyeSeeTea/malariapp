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

import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.remote.SdkQueries;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow_Table;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class EventExtended implements VisitableFromSDK {

    private final static String TAG = ".EventExtended";
    public final static String DHIS2_GMT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final static String DHIS2_LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String AMERICAN_DATE_FORMAT = "yyyy-MM-dd";


    public static final Event.EventStatus STATUS_ACTIVE = Event.EventStatus.ACTIVE;
    public static final Event.EventStatus STATUS_COMPLETED = Event.EventStatus.COMPLETED;
    public static final Event.EventStatus STATUS_FUTURE_VISIT = Event.EventStatus.SCHEDULED;
    public static final Event.EventStatus STATUS_SKIPPED = Event.EventStatus.SKIPPED;

    EventFlow event;

    public EventExtended(EventFlow event) {
        this.event = event;
    }

    public EventExtended(EventExtended event) {
        this.event = event.getEvent();
    }

    public EventExtended() {
        event = new EventFlow();
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public void setStatus(Event.EventStatus statusCompleted) {
        event.setStatus(statusCompleted);

    }

    public EventFlow getEvent() {
        return event;
    }

    /**
     * Returns the survey.creationDate associated with this event (created field)
     */
    public Date getCreationDate() {
        if (event == null) {
            return null;
        }
        return event.getCreated().toDate();
    }

    /**
     * Returns the survey.completionDate associated with this event (lastUpdated field)
     */
    public Date getLastUpdated() {
        if (event == null) {
            return null;
        }

        return event.getLastUpdated().toDate();
    }

    /**
     * Returns the survey.eventDate associated with this event (eventDate field)
     */
    public Date getEventDate() {
        return event.getEventDate().toDate();
    }

    /**
     * Returns the survey.eventDate associated with this event (dueDate field)
     */
    public Date getDueDate() {
        if (event == null) {
            return null;
        }

        return event.getDueDate().toDate();
    }

    public static Date parseDate(String dateAsString, String format) throws ParseException {
        if (dateAsString == null) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(dateAsString);
    }

    public static Date parseShortDate(String dateAsString) {
        try {
            return parseDate(dateAsString, AMERICAN_DATE_FORMAT);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static Date parseLongDate(String dateAsString) throws ParseException {
        return parseDate(dateAsString, DHIS2_GMT_DATE_FORMAT);
    }

    public static String formatLong(Date date) {
        return format(date, DHIS2_GMT_DATE_FORMAT);
    }

    public static String formatShort(Date date) {
        return format(date, AMERICAN_DATE_FORMAT);
    }

    /**
     * Turns a given date into a parseable String according to sdk date format
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }


    /**
     * Checks whether the given event contains errors in SDK FailedItemExtended table or has been
     * successful.
     * If not return null, it is becouse this item had a conflict.
     */
    public static FailedItemFlow hasConflict(long localId) {
        return new Select()
                .from(FailedItemFlow.class)
                .where(FailedItemFlow_Table.itemId
                        .is(localId)).querySingle();
    }

    public EventFlow removeDataValues() {
        //Remove all dataValues
        List<TrackedEntityDataValueFlow> dataValues = new Select().from(
                TrackedEntityDataValueFlow.class)
                .where(TrackedEntityDataValueFlow_Table.event.eq(event.getUId()))
                .queryList();
        if (dataValues != null) {
            for (int i = dataValues.size() - 1; i >= 0; i--) {
                TrackedEntityDataValueFlow dataValue = dataValues.get(i);
                dataValue.delete();
                dataValues.remove(i);
            }
        }
        //// TODO: 15/11/2016
        //event.setDataValues(null);
        event.save();
        return event;
    }

    public static long count() {
        return SQLite.selectCountOf()
                .from(EventFlow.class)
                .count();
    }

    public static List<EventFlow> getAllEvents() {
        return new Select().from(EventFlow.class).queryList();
    }

    public static EventFlow getEvent(String eventUid) {
        return new Select()
                .from(EventFlow.class)
                .where(EventFlow_Table.uId.eq(eventUid))
                .querySingle();
    }

    public void setOrganisationUnitId(String uid) {
        event.setOrgUnit(uid);
    }

    public void setProgramId(String uid) {
        event.setProgram(uid);
    }

    public void setProgramStageId(String uid) {
        event.setProgramStage(uid);
    }

    public void setLastUpdated(DateTime dateTime) {
        event.setLastUpdated(dateTime);
    }

    public void setEventDate(DateTime dateTime) {
        event.setEventDate(dateTime);
    }

    public void setDueDate(DateTime dateTime) {
        event.setEventDate(dateTime);
    }

    public long getLocalId() {
        return event.getId();
    }

    public String getUid() {
        return event.getUId();
    }

    public String getOrganisationUnitId() {
        return event.getOrgUnit();
    }

    public String getProgramStageId() {
        return event.getProgramStage();
    }

    public List<DataValueExtended> getDataValues() {
        Event eventModel = SdkQueries.getEvent(event.getUId());
        List<TrackedEntityDataValue> trackedEntityDataValues = eventModel.getDataValues();
        List<DataValueExtended> dataValueExtendeds = new ArrayList<>();
        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
            dataValueExtendeds.add(new DataValueExtended(
                    TrackedEntityDataValueFlow.MAPPER.mapToDatabaseEntity(trackedEntityDataValue)));
        }
        return dataValueExtendeds;
    }

    public String getProgramId() {
        return event.getProgram();
    }

    public void setLatitude(double latitude) {
        event.setLatitude(latitude);
    }

    public void setLongitude(double longitude) {
        event.setLongitude(longitude);
    }

    public void save() {
        event.save();
    }


    public static List<EventExtended> getExtendedList(List<EventFlow> events) {
        List<EventExtended> eventExtendeds = new ArrayList<>();
        for (EventFlow pojoFlow : events) {
            eventExtendeds.add(new EventExtended(pojoFlow));
        }
        return eventExtendeds;
    }
}
