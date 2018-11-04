package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FromObservationEventMapper extends EventMapper {
    private String TAG = "FromObservationEventMapper";
    private Map<String, Survey> surveysMap;

    public FromObservationEventMapper(Context context, String username, List<Survey> surveys) {
        super(context,username);

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
            Log.d(TAG, "build event " + observation.getSurveyUid());
            Event event = buildEvent(observation.getSurveyUid(),relatedSurvey.getOrgUnitUId(),
                    relatedSurvey.getProgramUId(), false);

            event.setCreated(new DateTime(relatedSurvey.getCreationDate()));
            event.setEventDate(new DateTime(relatedSurvey.getCreationDate()));
            event.setLastUpdated(new DateTime(relatedSurvey.getUploadDate()));

            event.setDataValues(new ArrayList<TrackedEntityDataValue>());

            for (ObservationValue observationValue : observation.getValues()) {
                TrackedEntityDataValue dataValue =
                        createDataValue(event,observationValue.getObservationValueUid(),
                                observationValue.getValue());

                event.getDataValues().add(dataValue);
            }

            return event;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = createErrorConversionMessage(errorMessage, observation,
                    relatedSurvey.getOrgUnitUId(), relatedSurvey.getProgramUId());
            throw new ConversionException(observation, errorMessage);
        }
    }

    private void createMaps(List<Survey> surveys) {
        surveysMap = new HashMap<>();
        for (Survey survey : surveys) {
            surveysMap.put(survey.getUId(), survey);
        }
    }
}
