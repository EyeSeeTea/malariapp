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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow_Table;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class EventExtended implements VisitableFromSDK {

    private final static String TAG = ".EventExtended";


    public static final Event.EventStatus STATUS_ACTIVE = Event.EventStatus.ACTIVE;
    public static final Event.EventStatus STATUS_COMPLETED = Event.EventStatus.COMPLETED;
    public static final Event.EventStatus STATUS_FUTURE_VISIT = Event.EventStatus.SCHEDULED;
    public static final Event.EventStatus STATUS_SKIPPED = Event.EventStatus.SKIPPED;

    List<TrackedEntityDataValueFlow> dataValuesInMemory;

    EventFlow event;

    public EventExtended(EventFlow event) {
        this.event = event;
    }

    public EventExtended(String eventUId) {
        event = new EventFlow();
        event.setUId(eventUId);
    }

    public EventExtended(EventExtended event) {
        this.event = event.getEvent();
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public List<TrackedEntityDataValueFlow> getDataValuesInMemory() {
        return dataValuesInMemory;
    }

    public void setDataValuesInMemory(
            List<TrackedEntityDataValueFlow> dataValuesInMemory) {
        this.dataValuesInMemory = dataValuesInMemory;
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
        return (event.getEventDate() != null) ? event.getEventDate().toDate() : null;
    }

    /**
     * Returns the survey.eventDate associated with this event (dueDate field)
     */
    public Date getDueDate() {
        return (event != null) ? event.getDueDate().toDate() : null;
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
        event.setDueDate(dateTime);
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

    public void setCreationDate(Date creationDate) {
        event.setCreated(new DateTime(creationDate));
    }

    public static List<EventExtended> fromJsonToEvents(JsonNode jsonNode) {
        TypeReference<List<Event>> typeRef =
                new TypeReference<List<Event>>() {
                };
        List<Event> events;
        try {
            if (jsonNode.has("events")) {
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule());
                events = objectMapper.
                        readValue(jsonNode.get("events").traverse(), typeRef);
            } else {
                events = new ArrayList<>();
            }
        } catch (IOException e) {
            events = new ArrayList<>();
            e.printStackTrace();
        }
        List<EventExtended> eventExtendedList = new ArrayList<>();
        for (Event event : events) {
            EventFlow eventFlow = EventFlow.MAPPER.mapToDatabaseEntity(event);
            EventExtended eventExtended = new EventExtended(eventFlow);
            if (event.getDataValues() != null && event.getDataValues().size() > 0) {
                List<TrackedEntityDataValueFlow> trackedEntityDataValueFlows =
                        TrackedEntityDataValueFlow.MAPPER.mapToDatabaseEntities(
                                event.getDataValues());
                eventExtended.setDataValuesInMemory(trackedEntityDataValueFlows);
            }
            eventExtendedList.add(eventExtended);
        }
        return eventExtendedList;
    }

    public boolean hasObservations(ServerMetadata serverMetadata) {
        boolean hasOverallCompetencyCode = false;

        List<DataValueExtended> dataValues = getDataValues();

        for (DataValueExtended dataValue : dataValues) {
            if (serverMetadata.getObservationsDataElementUids()
                    .contains(dataValue.getDataValue().getDataElement())){
                hasOverallCompetencyCode = true;
                break;
            }
        }

        return hasOverallCompetencyCode;
    }
}
