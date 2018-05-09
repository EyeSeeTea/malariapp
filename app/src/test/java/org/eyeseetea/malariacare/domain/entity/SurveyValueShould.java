package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SurveyValueShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_survey_value_with_mandatory_fields_without_option(){
        SurveyValue surveyValue = new SurveyValue("UID", "value");
        Assert.assertNotNull(surveyValue);
        Assert.assertTrue(surveyValue.getQuestionUId().equals("UID"));
        Assert.assertTrue(surveyValue.getValue().equals("value"));
    }

    @Test
    public void create_a_survey_value_with_mandatory_fields_with_option(){
        SurveyValue surveyValue = new SurveyValue("UID", "OptionUId", "value");
        Assert.assertNotNull(surveyValue);
        Assert.assertTrue(surveyValue.getQuestionUId().equals("UID"));
        Assert.assertTrue(surveyValue.getOptionUId().equals("OptionUId"));
        Assert.assertTrue(surveyValue.getValue().equals("value"));
    }

    @Test
    public void throw_exception_when_create_a_survey_value_with_mandatory_fields_without_option_and_null_question(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SurveyValue question UID is required");
        SurveyValue surveyValue = new SurveyValue(null, "value");
    }
    @Test
    public void throw_exception_when_create_a_survey_value_with_mandatory_fields_with_option_and_null_question(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SurveyValue question UID is required");
        SurveyValue surveyValue = new SurveyValue(null, "OptionUId", "value");
    }

    @Test
    public void throw_exception_when_create_a_survey_value_with_mandatory_fields_without_option_and_null_value(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SurveyValue not null value is required");
        SurveyValue surveyValue = new SurveyValue("UID", null);
    }

    @Test
    public void throw_exception_when_create_a_survey_value_with_mandatory_fields_with_option_and_null_value(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SurveyValue not null value is required");
        SurveyValue surveyValue = new SurveyValue("UID", "OptionUId", null);
    }

    @Test
    public void throw_exception_when_create_a_survey_value_with_mandatory_fields_with_option_and_null_option(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SurveyValue option UID is required");
        SurveyValue surveyValue = new SurveyValue("UID", null, "value");
    }
}
