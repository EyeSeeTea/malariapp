package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ObservationValueShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_observation_value_with_mandatory_fields(){
        String value = "Provider 1";
        String observationValueUid = "VIFKfFTVLcg";

        ObservationValue observationValue = new ObservationValue(value, observationValueUid);

        assertThat(observationValue, is(notNullValue()));
        assertThat(observationValue.getObservationValueUid(),is(observationValueUid));
        assertThat(observationValue.getValue(),is(value));
    }

    @Test
    public void throw_exception_when_create_survey_without_value(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value is required");

        new ObservationValue(null, "VIFKfFTVLcg");
    }

    @Test
    public void throw_exception_when_create_survey_without_observation_value_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("observationValueUid is required");

        new ObservationValue("Provider 1", null);
    }
}