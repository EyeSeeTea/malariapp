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

import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class EventExtended {

    private final static String TAG = ".EventExtended";

    List<TrackedEntityDataValueFlow> dataValuesInMemory;

    EventFlow event;

    public EventExtended(EventFlow event) {
        this.event = event;
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
     * Returns the survey.eventDate associated with this event (dueDate field)
     */
    public Date getDueDate() {
        return (event != null) ? event.getDueDate().toDate() : null;
    }

    public static long count() {
        return SQLite.selectCountOf()
                .from(EventFlow.class)
                .count();
    }

    public void setDueDate(DateTime dateTime) {
        event.setDueDate(dateTime);
    }

    public String getUid() {
        return event.getUId();
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
}
