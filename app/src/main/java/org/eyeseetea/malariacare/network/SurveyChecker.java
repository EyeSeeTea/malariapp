package org.eyeseetea.malariacare.network;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveyChecker {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
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
            int quarantineSurveysSize = SurveyDB.countQuarantineSurveys();
            Log.d(TAG, "Quarantine size: " + quarantineSurveysSize);
            if (quarantineSurveysSize > 0) {
                checkAllQuarantineSurveys();
            }
        } finally {
            Log.d(TAG, "Quarantine thread finished");
        }
    }

    /**
     * Get events filtered by program orgUnit and between dates.
     */
    public static List<EventExtended> getEvents(String program, String orgUnit, Date minDate,
            Date maxDate) throws IOException, JSONException {
            Response response;

            String startDate = EventExtended.format(minDate, EventExtended.AMERICAN_DATE_FORMAT);
            String endDate = EventExtended.format(
                    new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                    EventExtended.AMERICAN_DATE_FORMAT);
            String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
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

            return getEvents(jsonNode);
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
        List<ProgramDB> programs = ProgramDB.getAllPrograms();
        for (ProgramDB program : programs) {
            for (OrgUnitDB orgUnit : program.getOrgUnits()) {
                List<SurveyDB> quarantineSurveys = SurveyDB.getAllQuarantineSurveysByProgramAndOrgUnit(
                        program, orgUnit);
                if (quarantineSurveys.size() == 0) {
                    continue;
                }
                Date minDate = SurveyDB.getMinQuarantineCompletionDateByProgramAndOrgUnit(program,
                        orgUnit);//The start date is the first ascending completion date of all the quarantine surveys
                Date maxDate = SurveyDB.getMaxQuarantineUpdatedDateByProgramAndOrgUnit(program,
                        orgUnit);//The last date is the first descending updated date of all the quarantine surveys
                List<EventExtended> events = null;
                try {
                    events = getEvents(program.getUid(), orgUnit.getUid(),
                            minDate,
                            maxDate);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (events == null){
                    return;
                }
                for (SurveyDB survey : quarantineSurveys) {
                    if (events.size() > 0) {
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
    private static void updateQuarantineSurveysStatus(List<EventExtended> events, SurveyDB survey) {
        boolean isSent = false;
        for (EventExtended event : events) {
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

    private static void changeSurveyStatusFromQuarantineTo(SurveyDB survey, int status){
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
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(SurveyDB survey,
            EventExtended event) {
        for (DataValueExtended dataValue : DataValueExtended.getExtendedList(
                event.getDataValuesInMemory())) {
            String uid = ServerMetadataDB.findControlDataElementUid(
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

    public static List<EventExtended> getEvents(JsonNode jsonNode) {
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
