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

package org.eyeseetea.malariacare.network;

import java.util.Date;

/**
 * Created by arrizabalaga on 21/04/16.
 */
public class EventInfo {
    String eventUid;
    Date eventDate;

    public static final String NO_EVENT_FOUND ="NO_EVENT_FOUND";

    public EventInfo(String eventUid, Date eventDate) {
        this.eventUid = eventUid;
        this.eventDate = eventDate;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isEventFound(){
        return !NO_EVENT_FOUND.equals(this.eventUid);
    }
    public static EventInfo noEventFound() {
        return new EventInfo(NO_EVENT_FOUND, new Date());
    }


}
