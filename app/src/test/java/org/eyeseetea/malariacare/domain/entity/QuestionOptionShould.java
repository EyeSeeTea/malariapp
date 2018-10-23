package org.eyeseetea.malariacare.domain.entity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuestionOptionShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void throw_exception_when_create_question_option_with_null_question_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Question uid is required");

        new QuestionOption(null, "uid");
    }

    @Test
    public void throw_exception_when_create_question_option_with_null_option_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Option uid is required");

        new QuestionOption("uid", null);
    }

    @Test
    public void return_false_on_is_match_when_create_question_option() {
        QuestionOption questionOption = new QuestionOption("uid", "optionuid");

        assertThat(questionOption.isMatch(), is(false));
    }

    @Test
    public void return_true_when_do_match() {
        //given
        QuestionOption questionOption = new QuestionOption("uid", "optionuid");
        //when
        questionOption.doMatch();

        //then
        assertThat(questionOption.isMatch(), is(true));
    }

    @Test
    public void return_false_when_do_un_match_after_do_match() {
        //given
        QuestionOption questionOption = new QuestionOption("uid", "optionuid");
        //when
        questionOption.doMatch();
        questionOption.doUnMatch();

        //then
        assertThat(questionOption.isMatch(), is(false));
    }
}
