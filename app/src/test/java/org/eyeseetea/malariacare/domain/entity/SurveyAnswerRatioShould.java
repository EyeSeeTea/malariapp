package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

public class SurveyAnswerRatioShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_answered_ratio_and_check_default_values(){
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(10, 15);
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==15);
        Assert.assertTrue(answeredRatio.getAnswered()==0);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
    }

    @Test
    public void create_answered_ratio_and_check_default_values_with_0_compulsory(){
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(10, 0);
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==0);
        Assert.assertTrue(answeredRatio.getAnswered()==0);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==false);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==true);
    }

    @Test
    public void create_surveys_and_add_the_total_of_compulsory_questions(){
        int totalCompulsory = 10;
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(10, totalCompulsory);
        for(int i = 0; i<totalCompulsory; i++){
            answeredRatio.addQuestion(true);
        }
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==totalCompulsory);
        Assert.assertTrue(answeredRatio.getAnswered()==totalCompulsory);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==totalCompulsory);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==true);
    }

    @Test
    public void create_surveys_and_add_the_total_not_compulsory_questions(){
        int total = 10;
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(total, 10);
        for(int i = 0; i<total; i++){
            answeredRatio.addQuestion(false);
        }
        Assert.assertTrue(answeredRatio.getTotal()==10);
        Assert.assertTrue(answeredRatio.getTotalCompulsory()==10);
        Assert.assertTrue(answeredRatio.getAnswered()==total);
        Assert.assertTrue(answeredRatio.getCompulsoryAnswered()==0);
        Assert.assertTrue(answeredRatio.isCompleted()==true);
        Assert.assertTrue(answeredRatio.isCompulsoryCompleted()==false);
    }

    @Test
    public void throw_exception_when_survey_answer_ratio_doesnt_have_questions(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The total of question must be up than 0");
        SurveyAnsweredRatio answeredRatio = SurveyAnsweredRatio.startSurvey(0, 10);
    }
}
