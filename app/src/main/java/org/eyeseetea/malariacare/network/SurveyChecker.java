/*
 * Copyright (c) 2017.
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

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.ServerMetadata;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.controllers.wrappers.EventsWrapper;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SurveyChecker {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";
    private static String TAG = ".CheckSurveysB&D";

    /**
     * Launch a new thread to checks all the quarantine surveys
     */
    public static void launchQuarantineChecker() {
        if (!AUtils.isNetworkAvailable()) {
            return;
        }
        try {
            int quarantineSurveysSize = Survey.countQuarantineSurveys();
            Log.d(TAG, "Quarantine size: " + quarantineSurveysSize);
            if (quarantineSurveysSize > 0) {
                checkAllQuarantineSurveys();
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Quarantine thread finished");
        }
    }

    /**
     * Get events filtered by program orgUnit and between dates.
     */
    public static List<Event> getEvents(String orgUnit, Date minDate,
            Date maxDate) {
        try {
            Response response;

            String startDate = EventExtended.format(minDate, EventExtended.AMERICAN_DATE_FORMAT);
            String endDate = EventExtended.format(
                    new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                    EventExtended.AMERICAN_DATE_FORMAT);
            String url = String.format(DHIS_CHECK_EVENT_API, orgUnit, startDate,
                    endDate);
            Log.d(TAG, url);
            url = encodeBlanks(url);
            PullClient pullClient = new PullClient(PreferencesState.getInstance().getContext());
            NetworkUtils networkUtils = pullClient.networkUtils;
            response = networkUtils.executeCall(null, url, "GET");
            if (!response.isSuccessful()) {
                Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
                throw new IOException(response.message());
            }
            JSONObject events = new JSONObject(response.body().string());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                    JsonNode.class);

            return EventsWrapper.getEvents(jsonNode);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static String encodeBlanks(String endpoint) {
        return endpoint.replace(" ", "%20");
    }

    /**
     * Download the related events. and checks all the quarantine surveys.
     * If a survey is in the server, the survey should be set as sent. Else, the survey should be
     * set as completed and it will be resend.
     */
    public static void checkAllQuarantineSurveys() {
        List<Program> programs = Program.getAllPrograms();
        for (Program program : programs) {
            for (OrgUnit orgUnit : program.getOrgUnits()) {
                List<Survey> quarantineSurveys = Survey.getAllQuarantineSurveysByProgramAndOrgUnit(
                        program, orgUnit);
                if (quarantineSurveys.size() == 0) {
                    continue;
                }
                Date minDate = Survey.getMinQuarantineCompletionDateByProgramAndOrgUnit(program,
                        orgUnit);//The start date is the first ascending completion date of all the quarantine surveys
                Calendar c = Calendar.getInstance();
                c.setTime(minDate);
                c.add(Calendar.DATE, -1);
                minDate = c.getTime();

                Date maxDate = Survey.getMaxQuarantineUpdatedDateByProgramAndOrgUnit(program,
                        orgUnit);//The last date is the first descending updated date of all the quarantine surveys
                c = Calendar.getInstance();
                c.setTime(maxDate);
                c.add(Calendar.DATE, 1);
                maxDate = c.getTime();

                List<Event> events = getEvents(orgUnit.getUid(), minDate,
                        maxDate);
                    for (Survey survey : quarantineSurveys) {
                        if (events != null && events.size() > 0) {
                        updateQuarantineSurveysStatus(events, survey);
                        } else {
                            changeSurveyStatusFromQuarantineTo(survey, Constants.SURVEY_COMPLETED);
                        }
                    }
            }
        }
    }

    /**
     * Given a list of events, check for the presence of that survey among the events, and update
     * consequently their status. If the survey exist (checked by completion date) then it's
     * considered as sent, otherwise it will be considered as just completed and awaiting to be
     * sent
     */
    private static void updateQuarantineSurveysStatus(List<Event> events, Survey survey) {
        boolean isSent = false;
        for (Event event : events) {
            isSent = surveyDateExistsInEventTimeCaptureControlDE(survey, event);
            if (isSent) {
                break;
            }
        }
        //When the completion date for a survey is not present in the server, this survey is
        // not in the server.
        //This survey is set as "completed" and will be send in the future.
        changeSurveyStatusFromQuarantineTo(survey, (isSent) ? Constants.SURVEY_SENT : Constants.SURVEY_COMPLETED);
    }

    private static void changeSurveyStatusFromQuarantineTo(Survey survey, int status){
        try {
            Log.d(TAG, "Set quarantine survey as " + ((status == Constants.SURVEY_SENT) ? "sent "
                    : "complete ") + survey.getId_survey() + " date "
                    + EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if(survey.isInQuarantine()){
            survey.setStatus(status);
            survey.save();
        }
    }

    /**
     * Given an event, check through all its DVs if the survey completion date is present in the
     * event in the form of the control DE "Time Capture" whose UID is hardcoded
     */
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(Survey survey,
            Event event) {
        for (DataValue dataValue : event.getDataValues()) {
            String uid = ServerMetadata.findControlDataElementUid(
                    PreferencesState.getInstance().getContext().getString(
                            R.string.created_on_code));
            if (dataValue.getDataElement().equals(uid)
                    && dataValue.getValue().equals(EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT))) {
                Log.d(TAG, "Found survey" + survey.getId_survey() + "date "
                        + survey.getCreationDate() + "dateevent" + dataValue.getValue());
                return true;
            }
        }
        return false;
    }
}
