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

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.DataValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Event$Table;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem$Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class EventExtended implements VisitableFromSDK {

    private final static String TAG=".EventExtended";
    public final static String DHIS2_DATE_FORMAT ="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final static String AMERICAN_DATE_FORMAT ="yyyy-MM-dd";

    Event event;

    public EventExtended(){}

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

        try {
            return parseDate(event.getCreated(), DHIS2_DATE_FORMAT);
        }
        catch (ParseException e){
            Log.e(TAG,String.format("Event (%s) cannot parse date %s",event.getUid(),e.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * Returns the survey.completionDate associated with this event (lastUpdated field)
     * @return
     */
    public Date getCompletionDate(){
        if(event==null){
            return null;
        }

        try {
            return parseDate(event.getLastUpdated(), DHIS2_DATE_FORMAT);
        }
        catch (ParseException e){
            Log.e(TAG,String.format("Event (%s) cannot parse date %s",event.getUid(),e.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * Returns the survey.eventDate associated with this event (eventDate field)
     * @return
     */
    public Date getEventDate(){
        if(event==null){
            return null;
        }

        try {
            return parseDate(event.getEventDate(), DHIS2_DATE_FORMAT);
        }
        catch (ParseException e){
            Log.e(TAG,String.format("Event (%s) cannot parse date %s",event.getUid(),e.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * Returns the survey.eventDate associated with this event (dueDate field)
     * @return
     */
    public Date getScheduledDate(){
        if(event==null){
            return null;
        }

        try {
            return parseDate(event.getDueDate(), DHIS2_DATE_FORMAT);
        }
        catch (ParseException e){
            Log.e(TAG,String.format("Event (%s) cannot parse date %s",event.getUid(),e.getLocalizedMessage()));
            return null;
        }
    }

    public static  Date parseDate(String dateAsString,String format) throws  ParseException{
        if(dateAsString==null){
            return null;
        }

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(format);
        return simpleDateFormat.parse(dateAsString);
    }

    /**
     * Turns a given date into a parseable String according to sdk date format
     * @param date
     * @return
     */
    public static String format(Date date, String format){
        if(date==null){
            return null;
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }


    /**
     * Checks whether the given event contains errors in SDK FailedItem table or has been successful.
     * If not return null, it is becouse this item had a conflict.
     * @param localId
     * @return
     */
    public static FailedItem hasConflict(long localId){
        return  new Select()
                .from(FailedItem.class)
                .where(Condition.column(FailedItem$Table.ITEMID)
                        .is(localId)).querySingle();
    }

    public Event removeDataValues() {
        //Remove all dataValues
        List<DataValue> dataValues = new Select().from(DataValue.class)
                .where(Condition.column(DataValue$Table.EVENT).eq(event.getUid()))
                .queryList();
        if(dataValues!=null) {
            for (int i=dataValues.size()-1;i>=0;i--) {
                DataValue dataValue= dataValues.get(i);
                dataValue.delete();
                dataValues.remove(i);
            }
        }
        event.setDataValues(null);
        event.save();
        return event;
    }

    public static Event getLastEvent(Long id_org_unit, String programUid, String dateField) {
        return  new Select().from(Event.class)
                .where(Condition.column(Event$Table.PROGRAMID).eq(programUid))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID).eq(id_org_unit))
                .groupBy(new QueryBuilder().appendQuotedArray(Event$Table.PROGRAMID, Event$Table.ORGANISATIONUNITID))
                .having(Condition.columnsWithFunction("max", dateField)).querySingle();
    }
    public static long count(){
        return new Select().count()
                .from(Event.class)
                .count();
    }

    public static List<Event> getAllEvents() {
        return new Select().all().from(org.hisp.dhis.android.sdk.persistence.models.Event.class).queryList();
    }

    public static Event getEvent(String eventUid){
        return new Select()
                .from(Event.class)
                .where(Condition.column(Event$Table.ID).eq(eventUid))
                .querySingle();
    }

}
