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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by idelcano on 04/04/2016.
 */
public class PullClient {

    private static final String TAG=".PullClient";

    Context applicationContext;
    NetworkUtils networkUtils;
    public static final String DATE_FIELD ="eventDate";
    public static final String EVENTS_FIELD ="events";

    public PullClient(Context applicationContext) {
        this.applicationContext = applicationContext;
        networkUtils=new NetworkUtils(applicationContext);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        networkUtils.setDhisServer(sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url), ""));
        networkUtils.setUser(sharedPreferences.getString(applicationContext.getString(R.string.dhis_user), ""));
        networkUtils.setPassword(sharedPreferences.getString(applicationContext.getString(R.string.dhis_password), ""));
    }

    public EventInfo getLastEventUid(OrgUnit orgUnit, TabGroup tabGroup){
        EventInfo eventInfo = null;
        String lastEventUid;
        Date lastUpdatedEventDate;
        Survey lastSurvey= Survey.getLastSurvey(orgUnit.getId_org_unit(), tabGroup.getProgram());
        if(lastSurvey!=null) {
            Date lastLocalDate = lastSurvey.getUploadDate();
            if(lastLocalDate==null) {
                //if is the first survey, its needed search in the server with the creation date
                lastLocalDate = lastSurvey.getCreationDate();
            }
            if (lastLocalDate != null) {
                //https://hnqis-dev-ci.psi-mis.org/api/events?orgUnit=QS7sK8XzdQc&program=wK0958s1bdj&startDate=2016-1-01&fields=[event,eventDate]
                String data = QueryFormatterUtils.getInstance().prepareLastEventData(orgUnit.getUid(), tabGroup.getProgram().getUid(), lastLocalDate);
                try {
                    JSONObject lastEventsList = networkUtils.getData(data);
                    String eventuid = "";
                    Date lastDate = null;
                    JSONArray jsonArrayResponse = new JSONArray(lastEventsList.getString(EVENTS_FIELD));
                    for (int i = 0; i < jsonArrayResponse.length(); i++) {
                        JSONObject event = new JSONObject(jsonArrayResponse.getString(i));
                        if (eventuid.equals("")) {
                            eventuid = event.getString("event");
                            lastDate = EventExtended.parseDate(event.getString(DATE_FIELD), EventExtended.DHIS2_DATE_FORMAT);
                        } else if (!event.getString(DATE_FIELD).equals("") && lastDate.before(EventExtended.parseDate(event.getString(DATE_FIELD), EventExtended.DHIS2_DATE_FORMAT))) {
                            eventuid = event.getString("event");
                            lastDate = EventExtended.parseDate(event.getString(DATE_FIELD), EventExtended.DHIS2_DATE_FORMAT);
                        }
                    }
                    lastEventUid = eventuid;
                    lastUpdatedEventDate = lastDate;
                    //If not have new events, it set the last event.
                    if (lastEventUid.equals("")) {
                        lastEventUid = lastSurvey.getEventUid();
                        lastUpdatedEventDate = lastSurvey.getCompletionDate();
                    }
                    eventInfo = new EventInfo(lastEventUid, lastUpdatedEventDate);
                    //Create fake event to can path event not pulled.
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error reading the lastevent server json " + data);
                }
            }
        }
        if(lastSurvey==null || lastSurvey.getUploadDate()==null){
            return EventInfo.noEventFound();
        }
        return eventInfo;
    }

}
