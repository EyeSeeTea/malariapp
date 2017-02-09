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

package org.eyeseetea.malariacare.data.remote;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;

import java.util.List;


public abstract class SdkController {

    public final static String TAG = ".sdkController";


    public static List<EventExtended> getEventsFromEventsWrapper(JsonNode jsonNode) {
        /*
        List<EventExtended> eventExtendeds = new ArrayList<>();
        List<EventFlow> eventFlows = EventsWrapper.getEvents(jsonNode);
        for (EventFlow eventFlow:eventFlows){
            eventExtendeds.add(new EventExtended(eventFlow));
        }
        return eventExtendeds;
        */
        return null;
    }


}
