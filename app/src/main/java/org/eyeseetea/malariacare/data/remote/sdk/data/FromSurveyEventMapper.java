package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;

import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.ArrayList;
import java.util.List;

public class FromSurveyEventMapper extends EventMapper {
    public FromSurveyEventMapper(Context context, String username,
            List<Option> options, ServerMetadata serverMetadata) {
        super(context,username,options, serverMetadata);
    }

    public List<Event> map(List<Survey> surveys) throws ConversionException {
        List<Event> events = new ArrayList<>();

        for (Survey survey : surveys) {
            try {
                Event event = super.mapFromSurvey(survey);

                events.add(event);
            } catch (Exception e) {
                System.out.println("An error occurred converting Survey " +
                        survey.getSurveyUid() + " to Event:" + e.getMessage());
                throw e;
            }
        }

        return events;
    }
}
