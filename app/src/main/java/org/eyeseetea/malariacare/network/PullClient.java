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

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by idelcano on 04/04/2016.
 */
public class PullClient {

    private static final String TAG = ".PullClient";

    Context applicationContext;
    NetworkUtils networkUtils;

    public static final String EVENT = "event";
    public static final String NO_EVENT_FOUND = "NO_EVENT_FOUND";
    public static final String ATTRIBUTEVALUES = "attributeValues";
    public static final String ATTRIBUTE = "attribute";
    public static final String VALUE = "value";
    public static final String CODE = "code";
    public static final String LAST_UPDATED = "lastUpdated";

    public PullClient(Context applicationContext) {
        this.applicationContext = applicationContext;
        networkUtils = new NetworkUtils(applicationContext);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                applicationContext);
        networkUtils.setDhisServer(sharedPreferences.getString(
                applicationContext.getResources().getString(R.string.dhis_url), ""));
        networkUtils.setUser(
                sharedPreferences.getString(applicationContext.getString(R.string.dhis_user), ""));
        networkUtils.setPassword(
                sharedPreferences.getString(applicationContext.getString(R.string.dhis_password),
                        ""));
    }

    /**
     * Find the last survey in the server for that orgunit and program (given a program) in the
     * last
     * month from now.
     */
    public EventExtended getLastEventInServerWith(OrgUnitDB orgUnit, ProgramDB program) {
        EventExtended lastEventInServer = null;
        Date oneMonthAgo = getOneMonthAgo();

        //Lets for a last event with that orgunit/program
        String data = QueryFormatterUtils.getInstance().prepareLastEventData(orgUnit.getUid(),
                program.getUid(), oneMonthAgo);
        try {
            JSONObject response = networkUtils.getData(data);
            JsonNode jsonNode = networkUtils.toJsonNode(response);
            List<EventExtended> eventsFromThatDate = SurveyChecker.getEvents(jsonNode);
            for (EventExtended event : eventsFromThatDate) {
                //First event or events without date so far
                if (lastEventInServer == null) {
                    lastEventInServer = event;
                    continue;
                }

                //Update event only if it comes afterwards
                Date lastEventInServerEventDate = lastEventInServer.getEventDate();
                Date eventDate = event.getEventDate();

                if (eventDate.after(lastEventInServerEventDate)) {
                    lastEventInServer = event;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG,
                    String.format("Cannot read last event from server with orgunit:%s | program:%s",
                            orgUnit.getUid(), program.getUid()));
            ex.printStackTrace();
        }

        return lastEventInServer;
    }

    private Date getOneMonthAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    /**
     * Find if the last updated for the current user was changed
     */
    public boolean isUserUpdated(UserDB user) {
        if (Session.getCredentials().isDemoCredentials()) {
            return false;
        }

        Date dataBaseLastUpdated = user.getLastUpdated();
        Date dhisLastUpdated = new Date();
        //Lets for a last event with that orgunit/program
        String data = QueryFormatterUtils.getInstance().getUserLastUpdatedApiCall(user.getUid());
        try {
            JSONObject response = networkUtils.getData(data);
            JsonNode jsonNode = networkUtils.toJsonNode(response);
            String dateAsString = jsonNode.get(LAST_UPDATED).textValue();
            dhisLastUpdated = EventExtended.parseNewLongDate(dateAsString);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
        }

        user.setLastUpdated(dhisLastUpdated);
        if (dataBaseLastUpdated == null) {
            return true;
        }
        return (dataBaseLastUpdated.before(dhisLastUpdated));
    }

    public UserDB pullUserAttributes(UserDB appUser) {
        String lastMessage = appUser.getAnnouncement();
        //Lets for a last event with that orgunit/program
        String data = QueryFormatterUtils.getInstance().getUserAttributesApiCall(appUser.getUid());
        try {
            JSONObject response = networkUtils.getData(data);
            JsonNode jsonNode = networkUtils.toJsonNode(response);
            JsonNode jsonNodeArray = jsonNode.get(ATTRIBUTEVALUES);
            String newMessage = "";
            String closeDate = "";
            for (int i = 0; i < jsonNodeArray.size(); i++) {
                if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                        UserDB.ATTRIBUTE_USER_ANNOUNCEMENT)) {
                    newMessage = jsonNodeArray.get(i).get(VALUE).textValue();
                }
                if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                        UserDB.ATTRIBUTE_USER_CLOSE_DATE)) {
                    closeDate = jsonNodeArray.get(i).get(VALUE).textValue();
                }
            }
            if ((lastMessage == null && newMessage != null) || (newMessage != null
                    && !newMessage.equals("") && !lastMessage.equals(newMessage))) {
                appUser.setAnnouncement(newMessage);
                PreferencesState.getInstance().setUserAccept(false);
            }
            if (closeDate == null || closeDate.equals("")) {
                appUser.setCloseDate(null);
            } else {
                appUser.setCloseDate(EventExtended.parseNewLongDate(closeDate));
            }

        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
        }
        return appUser;
    }

    public boolean isUserClosed(String userUid) {
        if (Session.getCredentials().isDemoCredentials()) {
            return false;
        }
        //Lets for a last event with that orgunit/program
        String data = QueryFormatterUtils.getInstance().getUserAttributesApiCall(userUid);
        Date closedDate = null;
        try {
            JSONObject response = networkUtils.getData(data);
            JsonNode jsonNode = networkUtils.toJsonNode(response);
            JsonNode jsonNodeArray = jsonNode.get(ATTRIBUTEVALUES);
            String closeDateAsString = "";
            for (int i = 0; i < jsonNodeArray.size(); i++) {
                if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                        UserDB.ATTRIBUTE_USER_CLOSE_DATE)) {
                    closeDateAsString = jsonNodeArray.get(i).get(VALUE).textValue();
                }
            }
            if (closeDateAsString == null || closeDateAsString.equals("")) {
                return false;
            }
            closedDate = EventExtended.parseNewLongDate(closeDateAsString);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
            return false;
        }
        return closedDate.before(new Date());
    }
}
