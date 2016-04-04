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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Event$Table;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by idelcano on 04/04/2016.
 */
public class PullClient {

    private static final String TAG=".PullClient";
    Context applicationContext;
    NetworkUtils networkUtils;
    public static String lastEventUid;
    public static Date lastUpdatedEventDate;
    private final String DATE_FIELD="eventDate";

    public PullClient(Context applicationContext, String user, String password) {
        this.applicationContext = applicationContext;
        networkUtils=new NetworkUtils(applicationContext);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        networkUtils.setDhisServer(sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url), ""));
        networkUtils.setUser(user);
        networkUtils.setPassword(password);
    }

    public void getLastEventUid(OrgUnit orgUnit, Program program){
        Event lastEvent=new Select().from(Event.class)
                .where(Condition.column(Event$Table.PROGRAMID).eq(program.getUid()))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID).eq(orgUnit.getUid()))
                .groupBy(new QueryBuilder().appendQuotedArray(Event$Table.PROGRAMID, Event$Table.ORGANISATIONUNITID))
                .having(Condition.columnsWithFunction("max", DATE_FIELD)).querySingle();
        Date lastLocalDate = null;
        try {
            lastLocalDate= EventExtended.parseDate(lastEvent.getLastUpdated(), EventExtended.DHIS2_DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(lastLocalDate!=null) {
            //https://hnqis-dev-ci.psi-mis.org/api/events?orgUnit=QS7sK8XzdQc&program=wK0958s1bdj&startDate=2016-1-01&fields=[event,eventDate]
            String data = QueryFormatterUtils.getInstance().prepareLastEventData(orgUnit.getUid(), program.getUid(), lastLocalDate);
            try {
                JSONObject lastEventsList= networkUtils.getData(data);
                String eventuid="";
                Date lastDate=null;
                JSONArray jsonArrayResponse=new JSONArray(lastEventsList.getString("events"));
                for(int i=0;i<jsonArrayResponse.length();i++) {
                    JSONObject event = new JSONObject(jsonArrayResponse.getString(i));
                    if(eventuid.equals("")){
                        eventuid=event.getString("event");
                        lastDate=EventExtended.parseDate(event.getString(DATE_FIELD),EventExtended.DHIS2_DATE_FORMAT);
                    }
                    else if(lastDate.before(EventExtended.parseDate(event.getString(DATE_FIELD),EventExtended.DHIS2_DATE_FORMAT))){
                        eventuid=event.getString("event");
                        lastDate=EventExtended.parseDate(event.getString(DATE_FIELD), EventExtended.DHIS2_DATE_FORMAT);
                    }
                }
                lastEventUid=eventuid;
                lastUpdatedEventDate=lastDate;
                //If not have new events, it set the last event.
                if(lastEventUid.equals("")){
                    lastEventUid=lastEvent.getUid();
                    lastUpdatedEventDate=EventExtended.parseDate(lastEvent.getEventDate(), EventExtended.DHIS2_DATE_FORMAT);
                }
                //Create fake event to can path event not pulled.
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error getting the data " + data);
            }
        }
    }

}
