package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QuestionValueShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_question_value_with_mandatory_fields_without_option(){
        QuestionValue questionValue = QuestionValue.createSimpleValue("UID", "value");

        Assert.assertNotNull(questionValue);
        Assert.assertTrue(questionValue.getQuestionUId().equals("UID"));
        Assert.assertTrue(questionValue.getValue().equals("value"));
    }

    @Test
    public void create_a_question_value_with_mandatory_fields_with_option(){
        QuestionValue questionValue = QuestionValue.createOptionValue("UID", "OptionUId", "value");

        Assert.assertNotNull(questionValue);
        Assert.assertTrue(questionValue.getQuestionUId().equals("UID"));
        Assert.assertTrue(questionValue.getOptionUId().equals("OptionUId"));
        Assert.assertTrue(questionValue.getValue().equals("value"));
    }

    @Test
    public void throw_exception_when_create_a_question_value_with_mandatory_fields_without_option_and_null_question(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QuestionUID is required");

        QuestionValue.createSimpleValue(null, "value");
    }
    @Test
    public void throw_exception_when_create_a_question_value_with_mandatory_fields_with_option_and_null_question(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("QuestionUID is required");

        QuestionValue.createOptionValue(null, "OptionUId", "value");
    }

    @Test
    public void throw_exception_when_create_a_question_value_with_mandatory_fields_without_option_and_null_value(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value is required");

        QuestionValue.createSimpleValue("UID", null);
    }

    @Test
    public void throw_exception_when_create_a_question_value_with_mandatory_fields_with_option_and_null_value(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value is required");

        QuestionValue.createOptionValue("UID", "OptionUId", null);
    }

    @Test
    public void throw_exception_when_create_a_question_value_with_mandatory_fields_with_option_and_null_option(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("OptionUID is required");

        QuestionValue.createOptionValue("UID", null, "value");
    }
}