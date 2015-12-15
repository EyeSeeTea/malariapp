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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer.models;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.android.sdk.persistence.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class EventExtended implements VisitableFromSDK {

    private final static String TAG=".EventExtended";
    private final static String COMPLETION_DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    Event event;

    public EventExtended(Event event){
        this.event =event;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public Event getEvent() {
        return event;
    }

    /**
     * Returns the survey.creationDate associated with this event (created field)
     * @return
     */
    public Date getCreationDate(){
        if(event==null){
            return null;
        }

        return parseDate(event.getCreated());
    }

    /**
     * Returns the survey.completionDate associated with this event (lastUpdated field)
     * @return
     */
    public Date getCompletionDate(){
        if(event==null){
            return null;
        }

        return parseDate(event.getLastUpdated());
    }

    /**
     * Returns the survey.eventDate associated with this event (eventDate field)
     * @return
     */
    public Date getEventDate(){
        if(event==null){
            return null;
        }

        return parseDate(event.getEventDate());
    }

    /**
     * Returns the survey.eventDate associated with this event (dueDate field)
     * @return
     */
    public Date getScheduledDate(){
        if(event==null){
            return null;
        }

        return parseDate(event.getDueDate());
    }

    private Date parseDate(String dateAsString){
        if(dateAsString==null){
            return null;
        }

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(COMPLETION_DATE_FORMAT);
        try {
            return simpleDateFormat.parse(dateAsString);
        }catch (ParseException e){
            Log.e(TAG,String.format("Event (%s) cannot parse date %s",event.getUid(),e.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * Turns a given date into a parseable String according to sdk date format
     * @param date
     * @return
     */
    public static String format(Date date){
        if(date==null){
            return null;
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(COMPLETION_DATE_FORMAT);
        return simpleDateFormat.format(date);
    }

}
