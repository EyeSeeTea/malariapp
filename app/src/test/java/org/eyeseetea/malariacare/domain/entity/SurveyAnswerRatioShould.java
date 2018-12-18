package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SurveyAnswerRatioShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Test
    public void return_100_when_get_compulsory_answered_percentage_after_fill_all_the_compulsory_questions(){
        //given
        int total = 10;
        int totalAnswered = 0;
        int totalCompulsoryAnswered = 15;
        int totalCompulsory = 15;

        //when
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory, totalAnswered, totalCompulsoryAnswered);
        //then
        Assert.assertTrue(answeredRatio.getCompulsoryAnsweredPercentage()==100);
    }

    @Test
    public void return_100_when_get_answered_percentage_after_fill_all_the_normal_questions(){
        //given
        int total = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        int totalCompulsory = 15;

        //when
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory, totalAnswered, totalCompulsoryAnswered);
        //then
        Assert.assertTrue(answeredRatio.getAnsweredPercentage()==100);
    }

    @Test
    public void return_0_percentages_when_havent_filled_questions(){
        //given
        int total = 1;
        int totalAnswered = 0;
        int totalCompulsoryAnswered = 0;
        int totalCompulsory = 0;

        //when
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory, totalAnswered, totalCompulsoryAnswered);
        //then
        Assert.assertTrue(answeredRatio.getAnsweredPercentage()==0);
        Assert.assertTrue(answeredRatio.getCompulsoryAnsweredPercentage()==0);
    }

    @Test
    public void create_answered_ratio_and_check_default_values(){
        //given
        int total = 10;
        int totalCompulsory = 15;

        //when
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==15);
        Assert.assertTrue(answeredRatio.getAnswered()==0);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==0);
        Assert.assertTrue(answeredRatio.getTotalStatus()==0);
    }

    @Test
    public void create_answered_ratio_and_check_default_values_with_0_compulsory(){
        //given
        int total = 10;
        int totalCompulsory = 0;

        //when
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==0);
        Assert.assertTrue(answeredRatio.getAnswered()==0);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==true);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==100);
        Assert.assertTrue(answeredRatio.getTotalStatus()==0);
    }

    @Test
    public void create_surveyAnswerRatio_and_add_all_the_total_of_compulsory_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 0;
        int totalCompulsoryAnswered = 10;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==totalCompulsory);
        Assert.assertTrue(answeredRatio.getAnswered()==totalCompulsory);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==totalCompulsory);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==true);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==100);
        Assert.assertTrue(answeredRatio.getTotalStatus()==100);
    }
    @Test
    public void create_surveyAnswerRatio_and_add_all_the_total_of_compulsory_questions_and_remove_one(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 0;
        int totalCompulsoryAnswered = 10;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory, totalAnswered, totalCompulsoryAnswered);

        //when
        answeredRatio.removeQuestion(true);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==total);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==totalCompulsory);
        Assert.assertTrue(answeredRatio.getAnswered()==total-1);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==totalCompulsory-1);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==90);
        Assert.assertTrue(answeredRatio.getTotalStatus()==90);
    }

    @Test
    public void create_surveyAnswerRatio_and_add_all_the_total_not_compulsory_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==100);
        Assert.assertTrue(answeredRatio.getTotalStatus()==100);
    }

    @Test
    public void create_surveyAnswerRatio_and_add_all_the_total_not_compulsory_questions_and_remove_one(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        answeredRatio.removeQuestion(false);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total-1);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==0);
        Assert.assertTrue(answeredRatio.getTotalStatus()==90);
    }

    @Test
    public void create_surveyAnswerRatio_with_all_questions_answered_activate_three_children_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        answeredRatio.fixTotalQuestion(false, true);
        answeredRatio.fixTotalQuestion(false, true);
        answeredRatio.fixTotalQuestion(false, true);

        //then

        Assert.assertTrue(answeredRatio.getTotal()==10+3);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==0);
        Assert.assertTrue(answeredRatio.getTotalStatus()==76);
    }

    @Test
    public void create_surveyAnswerRatio_with_all_questions_answered_activate_three_mandatory_children_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        answeredRatio.fixTotalQuestion(true, true);
        answeredRatio.fixTotalQuestion(true, true);
        answeredRatio.fixTotalQuestion(true, true);

        //then

        Assert.assertTrue(answeredRatio.getTotal()==10+3);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10+3);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==0);
        Assert.assertTrue(answeredRatio.getTotalStatus()==76);
    }

    @Test
    public void create_surveyAnswerRatio_and_activate_and_deactivate_three_mandatory_children_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 10;
        int totalCompulsoryAnswered = 0;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        answeredRatio.fixTotalQuestion(false, true);
        answeredRatio.fixTotalQuestion(false, true);
        answeredRatio.fixTotalQuestion(false, true);
        answeredRatio.fixTotalQuestion(false, false);
        answeredRatio.fixTotalQuestion(false, false);
        answeredRatio.fixTotalQuestion(false, false);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==100);
        Assert.assertTrue(answeredRatio.getTotalStatus()==100);
    }

    @Test
    public void create_surveyAnswerRatio_and_activate_and_deactivate_some_children_mandatory_questions(){
        //given
        int total = 10;
        int totalCompulsory = 10;
        int totalAnswered = 0;
        int totalCompulsoryAnswered = 10;
        SurveyAnsweredRatio answeredRatio = givenAAnsweredRatio(total, totalCompulsory);

        //when
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        answeredRatio.fixTotalQuestion(true, true);
        answeredRatio.fixTotalQuestion(true, true);
        answeredRatio.fixTotalQuestion(true, true);
        answeredRatio.fixTotalQuestion(true, false);
        answeredRatio.fixTotalQuestion(true, false);
        answeredRatio.fixTotalQuestion(true, false);

        //then
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==10);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==true);
        Assert.assertTrue(answeredRatio.getMandatoryStatus()==100);
        Assert.assertTrue(answeredRatio.getTotalStatus()==100);
    }

    @Test
    public void throw_exception_when_survey_answer_ratio_doesnt_have_questions(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The total of question must be up than 0");
        SurveyAnsweredRatio.startSurvey(0, 10);
    }

    private SurveyAnsweredRatio givenAAnsweredRatio(int total, int totalCompulsory) {
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(total, totalCompulsory);
        return answeredRatio;
    }

    private SurveyAnsweredRatio givenAAnsweredRatio(int total, int totalCompulsory, int totalAnswered, int totalCompulsoryAnswered) {
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(total, totalCompulsory);
        answeredRatio = fillQuestions(totalAnswered, totalCompulsoryAnswered, answeredRatio);
        return answeredRatio;
    }

    private SurveyAnsweredRatio fillQuestions(int totalAnswered, int totalCompulsoryAnswered, SurveyAnsweredRatio answeredRatio) {
        for(int i = 0; i<totalAnswered; i++){
            answeredRatio.addQuestion(false);
        }
        for(int i = 0; i<totalCompulsoryAnswered; i++){
            answeredRatio.addQuestion(true);
        }
        return answeredRatio;
    }
}
