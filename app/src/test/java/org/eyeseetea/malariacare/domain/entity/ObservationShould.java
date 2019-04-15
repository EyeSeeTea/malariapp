package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

public class ObservationShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_new_observation_with_mandatory_fields(){
        String surveyId = "CBFKAFTVLcg";

        Observation observation = Observation.createNewObservation(surveyId);

        assertThat(observation, is(notNullValue()));
        assertThat(observation.getSurveyUid(),is(surveyId));
        assertThat(observation.getStatus(),is(ObservationStatus.IN_PROGRESS));
        assertThat(observation.getValues(), is(notNullValue()));
    }

    @Test
    public void create_stored_observation_with_mandatory_fields(){
        String surveyId = "CBFKAFTVLcg";
        ObservationStatus status = ObservationStatus.SENT;
        List<ObservationValue> values = givenAObservationValues();

        Observation observation =
                Observation.createStoredObservation(surveyId, status, values);

        Assert.assertNotNull(observation);
        assertThat(observation, is(notNullValue()));
        assertThat(observation.getSurveyUid(),is(surveyId));
        assertThat(observation.getStatus(),is(status));
        assertThat(observation.getValues(),is(values));
    }

    private List<ObservationValue> givenAObservationValues() {
        List<ObservationValue> values = new ArrayList<>();

        values.add(new ObservationValue("Provider 1", "VIFKfFTVLcg"));
        values.add(new ObservationValue("gaps 1", "lI09tJv3h4z"));
        values.add(new ObservationValue( "action plan 1", "Im5C86I2ObV"));
        values.add(new ObservationValue("Refresher training during SSV", "bwwyHVzxnTZ"));
        values.add(new ObservationValue( "Watch video","ibpjjNJLn44"));

        return values;
    }

    @Test
    public void throw_exception_when_create_new_observation_without_survey_id(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("surveyUid is required");

        Observation.createNewObservation(null);
    }

    @Test
    public void throw_exception_when_create_stored_observation_without_survey_id(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("surveyUid is required");

        String surveyId = null;
        ObservationStatus status = ObservationStatus.SENT;
        List<ObservationValue> values = givenAObservationValues();

        Observation.createStoredObservation(surveyId, status, values);
    }

    @Test
    public void throw_exception_when_create_stored_observation_without_status(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("status is required");

        String surveyId = "CBFKAFTVLcg";
        ObservationStatus status = null;
        List<ObservationValue> values = givenAObservationValues();

        Observation.createStoredObservation(surveyId, status, values);
    }

    @Test
    public void throw_exception_when_create_stored_observation_without_values(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("values is required");

        String surveyId = "CBFKAFTVLcg";
        ObservationStatus status = ObservationStatus.SENT;
        List<ObservationValue> values = null;

        Observation.createStoredObservation(surveyId, status, values);
    }
}

