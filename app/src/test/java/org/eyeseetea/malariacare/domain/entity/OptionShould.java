package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OptionShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_option_value_with_mandatory_fields(){
        Option option = new Option("UID", "CODE", "NAME", 1.0f, "ANSWER_UID");

        Assert.assertNotNull(option);
        Assert.assertTrue(option.getUId().equals("UID"));
        Assert.assertTrue(option.getCode().equals("CODE"));
        Assert.assertTrue(option.getName().equals("NAME"));
        Assert.assertTrue(option.getFactor()==1.0f);
        Assert.assertTrue(option.getAnswerName().equals("ANSWER_UID"));
    }

    @Test
    public void throw_exception_when_create_a_option_with_mandatory_fields_without_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UId is required");

        new Option("", "CODE", "NAME", 1.0f, "ANSWER_UID");
    }

    @Test
    public void throw_exception_when_create_a_option_with_mandatory_fields_without_code(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Code is required");

        new Option("UId", "", "NAME", 1.0f, "ANSWER_UID");
    }

    @Test
    public void throw_exception_when_create_a_option_with_mandatory_fields_without_name(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name is required");

        new Option("UId", "code", "", 1.0f, "ANSWER_UID");
    }

    @Test
    public void throw_exception_when_create_a_option_with_mandatory_fields_without_answer(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Answer is required");

        new Option("UId", "code", "NAME", 1.0f, "");
    }

    @Test
    public void throw_exception_when_create_a_option_with_mandatory_fields_with_invalid_factor(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid factor");

        new Option("UId", "code", "NAME", -0.1f, "");
    }

}
