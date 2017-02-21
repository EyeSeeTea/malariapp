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
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.ServerMetadata;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.event.Event;
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
            int quarantineSurveysSize = Survey.countQuarantineSurveys();
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
            Date maxDate) {
        try {
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
                Date minDate = Survey.getMinQuarantineEventDateByProgramAndOrgUnit(program,
                        orgUnit);
                Date maxDate = Survey.getMaxQuarantineEventDateByProgramAndOrgUnit(program,
                        orgUnit);
                List<EventExtended> events = getEvents(program.getUid(), orgUnit.getUid(), minDate,
                        maxDate);
                if (events != null && events.size() > 0) {
                    for (Survey survey : quarantineSurveys) {
                        updateQuarantineSurveysStatus(events, survey);
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
    private static void updateQuarantineSurveysStatus(List<EventExtended> events, Survey survey) {
        boolean isSent = false;
        for (EventExtended event : events) {
            isSent = surveyDateExistsInEventTimeCaptureControlDE(survey, event);
            if (isSent) {
                break;
            }
        }
        if (isSent) {
            Log.d(TAG, "Set quarantine survey as sent" + survey.getId_survey() + " date "
                    + EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
            survey.setStatus(Constants.SURVEY_SENT);
        } else {
            //When the completion date for a survey is not present in the server, this survey is
            // not in the server.
            //This survey is set as "completed" and will be send in the future.
            Log.d(TAG, "Set quarantine survey as completed" + survey.getId_survey() + " date "
                    + EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
            if (survey.isInQuarantine()) {
                survey.setStatus(Constants.SURVEY_COMPLETED);
            }
        }
        survey.save();
    }

    /**
     * Given an event, check through all its DVs if the survey completion date is present in the
     * event in the form of the control DE "Time Capture" whose UID is hardcoded
     */
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(Survey survey,
            EventExtended event) {
        for (DataValueExtended dataValue : DataValueExtended.getExtendedList(
                event.getDataValuesInMemory())) {
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
