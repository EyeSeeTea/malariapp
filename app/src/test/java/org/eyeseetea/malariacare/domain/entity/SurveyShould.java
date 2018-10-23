package org.eyeseetea.malariacare.domain.entity;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveyShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_survey_with_mandatory_fields(){
        List<Question> questions = new ArrayList<>();
        Survey survey = givenASurveyWithTwoCompulsoryQuestions(questions);

        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getUId().equals("UID"));
        Assert.assertTrue(survey.getProgramUId().equals("PROGRAM_UID"));
        Assert.assertTrue(survey.getOrgUnitUId().equals("ORG_UNIT_UID"));
        Assert.assertTrue(survey.getUserUId().equals("USER_UID"));
    }

    @Test
    public void create_empty_survey(){
        List<Question> questions = new ArrayList<>();
        Survey survey = givenASurveyWithTwoCompulsoryQuestions(questions);

        Assert.assertNotNull(survey);
        Assert.assertNotNull(survey.getCreationDate());
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getStatus().equals(SurveyStatus.IN_PROGRESS));
    }

    @Test
    public void create_pulled_survey(){
        Date creationDate = new Date();
        Date uploadDate = new Date();
        Date scheduledDate = new Date();
        Date completionDate = new Date();
        List<QuestionValue> values = new ArrayList<QuestionValue>();
        Score score = new Score("ScoreUId", 100.0f);
        Survey survey = givenAPulledSurveyWithSimpleAndOptionValues(creationDate, uploadDate, scheduledDate, completionDate, values, score);

        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getStatus().equals(SurveyStatus.SENT));
        Assert.assertTrue(survey.getCreationDate().equals(creationDate));
        Assert.assertTrue(survey.getCompletionDate().equals(completionDate));
        Assert.assertTrue(survey.getScheduledDate().equals(scheduledDate));
        Assert.assertTrue(survey.getUploadDate().equals(uploadDate));
        Assert.assertTrue(survey.getValues().equals(values));
        Assert.assertTrue(survey.getScore().equals(score));
    }

    @Test
    public void throw_exception_when_create_survey_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey uid is required");

        List<Question> questions = givenAListOfTwoCompulsoryQuestions();

        Survey.createEmptySurvey(null, "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
    }

    @Test
    public void throw_exception_when_create_survey_with_null_program_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey programUId is required");

        List<Question> questions = givenAListOfTwoCompulsoryQuestions();

        Survey.createEmptySurvey("UID", null, "ORG_UNIT_UID", "USER_UID", questions);
    }

    @Test
    public void throw_exception_when_create_survey_with_null_org_unit_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey orgUnitUId is required");

        List<Question> questions = givenAListOfTwoCompulsoryQuestions();
        Survey.createEmptySurvey("UID", "PROGRAM_UID", null, "USER_UID", questions);
    }

    @Test
    public void throw_exception_when_create_survey_with_null_user_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey userUId is required");

        List<Question> questions = givenAListOfTwoCompulsoryQuestions();

        Survey.createEmptySurvey("UID", "PROGRAM_UID", "ORG_UNIT_UID", null, questions);
    }

    @Test
    public void calculate_survey_ratio_when_add_normal_value(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);

        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && !survey.getAnsweredRatio().isCompulsoryCompleted());
    }
    @Test
    public void complete_survey_ratio_when_add_all_compulsory_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID2");
        simpleQuestionToBeAdded.add("QuestionUID3");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }
    @Test
    public void complete_survey_ratio_when_add_all__values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        simpleQuestionToBeAdded.add("QuestionUID1");
        simpleQuestionToBeAdded.add("QuestionUID2");
        simpleQuestionToBeAdded.add("QuestionUID3");
        simpleQuestionToBeAdded.add("QuestionUIDCHILD1");
        simpleQuestionToBeAdded.add("QuestionUIDCHILD2");
        List<String> optionQuestionToBeAdded = new ArrayList<>();
        optionQuestionToBeAdded.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(optionQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void increment_only_one_question_when_add_that_question_more_than_one_time_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && !survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_add_compulsory_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID2");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==1);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && !survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_remove_normal_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        List<String> simpleQuestionToBeRemoved = new ArrayList<>();
        simpleQuestionToBeRemoved.add("QuestionUID1");
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //When
        survey = removeDummyQuestionValuesFromSurvey(simpleQuestionToBeRemoved, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && !survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_remove_compulsory_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID2");
        List<String> simpleQuestionToBeRemoved = new ArrayList<>();
        simpleQuestionToBeRemoved.add("QuestionUID2");
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //When
        survey = removeDummyQuestionValuesFromSurvey(simpleQuestionToBeRemoved, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && !survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void complete_survey_ratio_when_child_values_are_hidden(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        simpleQuestionToBeAdded.add("QuestionUID2");
        simpleQuestionToBeAdded.add("QuestionUID3");
        simpleQuestionToBeAdded.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void complete_survey_ratio_when_parents_and_children_values_are_filled(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        List<String> optionQuestionToBeAdded = new ArrayList<>();
        optionQuestionToBeAdded.add("QuestionUID2");
        optionQuestionToBeAdded.add("QuestionUID3");
        optionQuestionToBeAdded.add("QuestionUID4");
        optionQuestionToBeAdded.add("QuestionUIDCHILD1");
        optionQuestionToBeAdded.add("QuestionUIDCHILD2");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(optionQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void count_questions_when_child_values_are_filled_and_remove_parent_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        List<String> optionQuestionToBeAdded = new ArrayList<>();
        optionQuestionToBeAdded.add("QuestionUID2");
        optionQuestionToBeAdded.add("QuestionUID3");
        optionQuestionToBeAdded.add("QuestionUID4");
        optionQuestionToBeAdded.add("QuestionUIDCHILD1");
        optionQuestionToBeAdded.add("QuestionUIDCHILD2");
        List<String> optionQuestionToBeRemoved = new ArrayList<>();
        optionQuestionToBeRemoved.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(optionQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());

        //When
        survey = removeDummyQuestionValuesFromSurvey(optionQuestionToBeRemoved, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==3);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }


    @Test
    public void count_questions_when_child_values_are_filled_with_more_than_one_level_and_remove_parent_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentButWithMultipleDepthLevels();

        List<String> simpleQuestionToBeAdded = new ArrayList<>();
        simpleQuestionToBeAdded.add("QuestionUID1");
        List<String> optionQuestionToBeAdded = new ArrayList<>();
        optionQuestionToBeAdded.add("QuestionUID4");
        optionQuestionToBeAdded.add("QuestionUID2");
        optionQuestionToBeAdded.add("QuestionUID3");
        optionQuestionToBeAdded.add("QuestionUID4");
        optionQuestionToBeAdded.add("QuestionUIDCHILD1");
        optionQuestionToBeAdded.add("QuestionUIDCHILD2");
        optionQuestionToBeAdded.add("QuestionUIDCHILD_level2_uid1");
        optionQuestionToBeAdded.add("QuestionUIDCHILD_level2_uid2");
        List<String> optionQuestionToBeRemoved = new ArrayList<>();
        optionQuestionToBeRemoved.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(simpleQuestionToBeAdded, survey);
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(optionQuestionToBeAdded, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==8);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==8);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());

        //When
        survey = removeDummyQuestionValuesFromSurvey(optionQuestionToBeRemoved, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==3);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getAnsweredRatio().isCompleted() && survey.getAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void count_questions_when_create_survey_and_add_parent_values(){
        //Given
        Survey survey = createQuestionTreeWithoutMultiparentChildren();
        List<String> questionToBeAdded = new ArrayList<>();
        questionToBeAdded.add("QuestionUID4");

        List<String> questionToBeRemoved = new ArrayList<>();
        questionToBeRemoved.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithSimpleValuesIntoSurvey(questionToBeAdded, survey);
        survey = removeDummyQuestionValuesFromSurvey(questionToBeRemoved, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
    }

    @Test
    public void count_children_only_one_time_when_create_survey_and_add_parents_with_common_child_with_multiparents(){
        //Given
        Survey survey = createQuestionTreeWithMultipleParents();

        List<String> questionToBeAdded = new ArrayList<>();
        questionToBeAdded.add("QuestionUID4");
        questionToBeAdded.add("QuestionUID3");

        //When
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(questionToBeAdded, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_children_when_create_survey_and_add_parents_and_remove_one_with_multiparents(){
        //Given
        Survey survey = createQuestionTreeWithMultipleParents();
        List<String> questionToBeAdded = new ArrayList<>();
        questionToBeAdded.add("QuestionUID3");
        questionToBeAdded.add("QuestionUID4");
        questionToBeAdded.add("QuestionUIDCHILD1");
        questionToBeAdded.add("QuestionUIDCHILD2");
        questionToBeAdded.add("QuestionUID4");
        List<String> questionToBeRemoved = new ArrayList<>();
        questionToBeRemoved.add("QuestionUID4");

        //When
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(questionToBeAdded, survey);
        survey = removeDummyQuestionValuesFromSurvey(questionToBeRemoved, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_only_one_child_when_create_survey_and_add_parents_and_remove_one_with_multiparents(){
        //Given
        Survey survey = createQuestionTreeWithMultipleParents();

        List<String> questionToBeAdded = new ArrayList<>();
        questionToBeAdded.add("QuestionUID4");
        questionToBeAdded.add("QuestionUID3");
        questionToBeAdded.add("QuestionUIDCHILD1");
        questionToBeAdded.add("QuestionUIDCHILD2");
        questionToBeAdded.add("QuestionUID3");
        List<String> questionToBeRemoved = new ArrayList<>();
        questionToBeRemoved.add("QuestionUID3");

        //When
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(questionToBeAdded, survey);
        survey = removeDummyQuestionValuesFromSurvey(questionToBeRemoved, survey);


        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_all_children_when_create_survey_and_add_two_parent_and_remove_only_one_with_multiparents(){
        //Given
        Survey survey = createQuestionTreeWithMultipleParents();
        List<String> questionToBeAdded = new ArrayList<>();
        questionToBeAdded.add("QuestionUID4");
        questionToBeAdded.add("QuestionUIDCHILD1");
        questionToBeAdded.add("QuestionUIDCHILD2");
        questionToBeAdded.add("QuestionUID3");
        List<String> questionToBeRemoved = new ArrayList<>();
        questionToBeRemoved.add("QuestionUID3");


        //When
        survey = addDummyQuestionsWithOptionValuesIntoSurvey(questionToBeAdded, survey);
        survey = removeDummyQuestionValuesFromSurvey(questionToBeRemoved, survey);

        //Then
        Assert.assertTrue(survey.getAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getAnsweredRatio().isCompulsoryCompleted()==false);
    }

    private Survey addDummyQuestionsWithSimpleValuesIntoSurvey(List<String> questionUids, Survey survey) {
        for (String uid : questionUids) {
            Question question = survey.getQuestion(uid);
            QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
            survey.addValue(questionValue);
        }
        return survey;
    }

    private Survey addDummyQuestionsWithOptionValuesIntoSurvey(List<String> questionUids, Survey survey) {
        for (String uid : questionUids) {
            Question question = survey.getQuestion(uid);
            QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
            survey.addValue(questionValue);
        }
        return survey;
    }

    private Survey removeDummyQuestionValuesFromSurvey(List<String> questionUids, Survey survey) {
        for (String uid : questionUids) {
            Question question = survey.getQuestion(uid);
            QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
            survey.removeValue(questionValue);
        }
        return survey;
    }

    @NonNull
    private List<Question> givenAListOfTwoCompulsoryQuestions() {
        List<Question> questions = new ArrayList<>();
        Question question = new Question("QuestionUID1", 2, true);
        questions.add(question);
        question = new Question("QuestionUID2", 2, true);
        questions.add(question);
        return questions;
    }

    private Survey givenASurveyWithTwoCompulsoryQuestions(List<Question> questions) {
        Question question = new Question("QuestionUID1", 2, true);
        questions.add(question);
        question = new Question("QuestionUID2", 2, true);
        questions.add(question);
        return Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
    }

    private Survey givenAPulledSurveyWithSimpleAndOptionValues(Date creationDate, Date uploadDate, Date scheduledDate, Date completionDate, List<QuestionValue> values, Score score) {
        values.add(QuestionValue.createSimpleValue("UId", "value"));
        values.add(QuestionValue.createOptionValue("UId2", "optionUId", "value2"));

        return Survey.createSentSurvey("UID", "PROGRAM_UID", "ORG_UNIT_UID",
                "USER_UID", creationDate, uploadDate, scheduledDate, completionDate, values, score);
    }

    private Survey createQuestionTreeWithoutMultiparentChildren() {
        List<Question> questions = new ArrayList<>();
        Question question1 = new Question("QuestionUID1", 2, false);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, true);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addQuestionParentAndOptionMatch(question4, "OPTIONUID1");
        question4.addChildren(question5.getUId());

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addQuestionParentAndOptionMatch(question4, "OPTIONUID1");
        question4.addChildren(question6.getUId());

        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        questions.add(question4);
        questions.add(question5);
        questions.add(question6);
        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
        return survey;
    }

    private Survey createQuestionTreeWithoutMultiparentButWithMultipleDepthLevels() {
        List<Question> questions = new ArrayList<>();
        Question question1 = new Question("QuestionUID1", 2, false);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, true);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addQuestionParentAndOptionMatch(question4, "OPTIONUID1");
        question4.addChildren(question5.getUId());

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addQuestionParentAndOptionMatch(question4, "OPTIONUID1");
        question4.addChildren(question6.getUId());

        Question question7 = new Question("QuestionUIDCHILD_level2_uid1", 2, false);
        question7.addQuestionParentAndOptionMatch(question6, "OPTIONUID1");
        question6.addChildren(question7.getUId());

        Question question8 = new Question("QuestionUIDCHILD_level2_uid2", 2, false);
        question8.addQuestionParentAndOptionMatch(question6, "OPTIONUID1");
        question6.addChildren(question8.getUId());

        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        questions.add(question4);
        questions.add(question5);
        questions.add(question6);
        questions.add(question7);
        questions.add(question8);
        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
        return survey;
    }

    private Survey createQuestionTreeWithMultipleParents() {
        List<Question> questions = givenQuestionsTreeWithMultipleParents();

        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);

        return survey;
    }

    @NonNull
    private List<Question> givenQuestionsTreeWithMultipleParents() {
        List<Question> questions = new ArrayList<>();
        Question question1 = new Question("QuestionUID1", 2, true);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, false);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addQuestionParentAndOptionMatch(question3, "OPTIONUID1");
        question3.addChildren(question5.getUId());
        question5.addQuestionParentAndOptionMatch(question4, "OPTIONUID2");
        question4.addChildren(question5.getUId());

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addQuestionParentAndOptionMatch(question4, "OPTIONUID1");
        question4.addChildren(question6.getUId());

        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        questions.add(question4);
        questions.add(question5);
        questions.add(question6);
        return questions;
    }
}
