package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.hisp.dhis.client.sdk.models.common.Coordinates;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FromObservationEventMapper {
    private String TAG = "FromObservationEventMapper";
    private final Context mContext;
    private final String mUsername;
    private Map<String, Survey> surveysMap;

    public FromObservationEventMapper(Context context, String username, List<Survey> surveys) {
        mContext = context;
        mUsername = username;

        createMaps(surveys);
    }

    public List<Event> map(List<Observation> observations) throws ConversionException {
        List<Event> events = new ArrayList<>();

        for (Observation observation : observations) {
            try {
                Event event = mapFromObservation(observation);

                events.add(event);
            } catch (Exception e) {
                System.out.println("An error occurred converting Observation " +
                        observation.getSurveyUid() + " to Event:" + e.getMessage());
                throw e;
            }
        }

        return events;
    }

    public Event mapFromObservation(Observation observation) throws ConversionException {
        String errorMessage = "Exception creating a new event from observation.";

        Survey relatedSurvey = surveysMap.get(observation.getSurveyUid());

        try {
            Event event = buildEvent(observation,relatedSurvey, false);
            event.setCreated(new DateTime(relatedSurvey.getCreationDate()));
            event.setUId(observation.getSurveyUid());
            event.setEventDate(new DateTime(relatedSurvey.getCreationDate()));
            event.setLastUpdated(new DateTime(relatedSurvey.getUploadDate()));

            event.setDataValues(new ArrayList<TrackedEntityDataValue>());

            for (ObservationValue observationValue : observation.getValues()) {
                TrackedEntityDataValue dataValue = new TrackedEntityDataValue();
                dataValue.setDataElement(observationValue.getObservationValueUid());
                dataValue.setEvent(event);
                dataValue.setStoredBy(mUsername);
                dataValue.setValue (observationValue.getValue());
                event.getDataValues().add(dataValue);
            }

            return event;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = createErrorConversionMessage(errorMessage, observation, relatedSurvey);
            throw new ConversionException(observation, errorMessage);
        }
    }

    private String createErrorConversionMessage(String errorMessageBase, Observation observation,
            Survey relatedSurvey) {
        String program = "";
        String orgUnit = "";

        program = relatedSurvey.getProgramUId();
        orgUnit = relatedSurvey.getOrgUnitUId();

        if (observation.getValues() != null) {
            Log.d(TAG, "DataValues:" + observation.getValues().toString());
        }

        return ": " + errorMessageBase + " surveyUid: " + observation.getSurveyUid()
                + "program: " + program + " OrgUnit: "
                + orgUnit + "data: " + observation.toString();
    }

    private Event buildEvent(Observation observation,
            Survey relatedSurvey, boolean isNew) throws Exception {
        Event event = new Event();
        event.setStatus(Event.EventStatus.COMPLETED);
        event.setOrgUnit(relatedSurvey.getOrgUnitUId());
        event.setProgram(relatedSurvey.getProgramUId());
        event.setProgramStage(relatedSurvey.getProgramUId());

        if (isNew) {
            updateEventLocation(event);
        }

        Log.d(TAG, "build event " + event.getUId());
        return event;
    }

    private void updateEventLocation(Event event) throws Exception {
        Location lastLocation = LocationMemory.get(event.getUId());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(
                    mContext.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if (lastLocation == null) {
            return;
        }

        //location -> set lat/lng
        Coordinates coordinates =
                new Coordinates(lastLocation.getLatitude(),lastLocation.getLongitude());

        event.setCoordinate(coordinates);
    }

    private void createMaps(List<Survey> surveys) {
        surveysMap = new HashMap<>();
        for (Survey survey : surveys) {
            surveysMap.put(survey.getUId(), survey);
        }
    }
}
