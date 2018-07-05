package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

public class SurveyAnswerRatioShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void calculate_survey_ratio_when_add_normal_value(){
        //create survey
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        //add values
        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);

        //check calc
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);

        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && !survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }
    @Test
    public void complete_survey_ratio_when_add_all_compulsory_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();


        Question question = survey.getQuestion("QuestionUID2");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }
    @Test
    public void complete_survey_ratio_when_add_all__values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID2");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void increment_only_one_question_when_add_that_question_more_than_one_time_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();


        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && !survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_add_compulsory_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();


        Question question = survey.getQuestion("QuestionUID2");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==1);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && !survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_remove_normal_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();


        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);

        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);


        survey.removeValue(questionValue, question);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && !survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void calculate_survey_ratio_when_remove_compulsory_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID2");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==1);

        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && !survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void complete_survey_ratio_when_child_values_are_hidden(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID2");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);

        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void complete_survey_ratio_when_parents_and_children_values_are_filled(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);

        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void count_questions_when_child_values_are_filled_and_remove_parent_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());

        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==3);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }


    @Test
    public void count_questions_when_child_values_are_filled_with_more_than_one_level_and_remove_parent_values(){
        Survey survey = createQuestionTreeWithoutMultiparentButWithMultipleDepthLevels();

        Question question = survey.getQuestion("QuestionUID1");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD_level2_uid1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD_level2_uid2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==8);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==8);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());

        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==3);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==2);
        Assert.assertTrue(!survey.getSurveyAnsweredRatio().isCompleted() && survey.getSurveyAnsweredRatio().isCompulsoryCompleted());
    }

    @Test
    public void count_questions_when_create_survey_and_add_parent_values(){
        Survey survey = createQuestionTreeWithoutMultiparentChildren();

        Question question = survey.getQuestion("QuestionUID4");
        QuestionValue questionValue = QuestionValue.createSimpleValue(question.getUId(), "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==1);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);

        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==4);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
    }

    @Test
    public void count_children_only_one_time_when_create_survey_and_add_parents_with_common_child_with_multiparents(){
        Survey survey = createQuestionTreeWithMultipleParents();

        Question question = survey.getQuestion("QuestionUID4");
        QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==6);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_children_when_create_survey_and_add_parents_and_remove_one_with_multiparents(){
        Survey survey = createQuestionTreeWithMultipleParents();

        Question question = survey.getQuestion("QuestionUID3");
        QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID4");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_only_one_child_when_create_survey_and_add_parents_and_remove_one_with_multiparents(){
        Survey survey = createQuestionTreeWithMultipleParents();

        Question question = survey.getQuestion("QuestionUID4");
        QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.removeValue(questionValue, question);


        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompulsoryCompleted()==false);
    }

    @Test
    public void count_all_children_when_create_survey_and_add_two_parent_and_remove_only_one_with_multiparents(){
        Survey survey = createQuestionTreeWithMultipleParents();

        Question question = survey.getQuestion("QuestionUID4");
        QuestionValue questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD1");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUIDCHILD2");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        question = survey.getQuestion("QuestionUID3");
        questionValue = QuestionValue.createOptionValue(question.getUId(), "OPTIONUID1", "dummyValue");
        survey.addValue(questionValue, question);
        survey.removeValue(questionValue, question);

        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotal()==5);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getTotalCompulsory()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getAnswered()==2);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().getCompulsoryAnswered()==0);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompleted()==false);
        Assert.assertTrue(survey.getSurveyAnsweredRatio().isCompulsoryCompleted()==false);
    }

    private Survey createQuestionTreeWithoutMultiparentChildren() {
        HashMap<String, Question> questions = new HashMap<>();
        Question question1 = new Question("QuestionUID1", 2, false);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, true);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addParent(question4, "OPTIONUID1");
        question4.addChildren(question5);

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addParent(question4, "OPTIONUID1");
        question4.addChildren(question6);

        questions.put(question1.getUId(), question1);
        questions.put(question2.getUId(), question2);
        questions.put(question3.getUId(), question3);
        questions.put(question4.getUId(), question4);
        questions.put(question5.getUId(), question5);
        questions.put(question6.getUId(), question6);
        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
        return survey;
    }

    private Survey createQuestionTreeWithoutMultiparentButWithMultipleDepthLevels() {
        HashMap<String, Question> questions = new HashMap<>();
        Question question1 = new Question("QuestionUID1", 2, false);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, true);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addParent(question4, "OPTIONUID1");
        question4.addChildren(question5);

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addParent(question4, "OPTIONUID1");
        question4.addChildren(question6);

        Question question7 = new Question("QuestionUIDCHILD_level2_uid1", 2, false);
        question7.addParent(question6, "OPTIONUID1");
        question6.addChildren(question7);

        Question question8 = new Question("QuestionUIDCHILD_level2_uid2", 2, false);
        question8.addParent(question6, "OPTIONUID1");
        question6.addChildren(question8);

        questions.put(question1.getUId(), question1);
        questions.put(question2.getUId(), question2);
        questions.put(question3.getUId(), question3);
        questions.put(question4.getUId(), question4);
        questions.put(question5.getUId(), question5);
        questions.put(question6.getUId(), question6);
        questions.put(question7.getUId(), question7);
        questions.put(question8.getUId(), question8);
        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
        return survey;
    }
    private Survey createQuestionTreeWithMultipleParents() {
        HashMap<String, Question> questions = new HashMap<>();
        Question question1 = new Question("QuestionUID1", 2, true);
        Question question2 = new Question("QuestionUID2", 2, true);
        Question question3 = new Question("QuestionUID3", 2, false);
        Question question4 = new Question("QuestionUID4", 2, false);

        Question question5 = new Question("QuestionUIDCHILD1", 2, false);
        question5.addParent(question3, "OPTIONUID1");
        question3.addChildren(question5);
        question5.addParent(question4, "OPTIONUID2");
        question4.addChildren(question5);

        Question question6 = new Question("QuestionUIDCHILD2", 2, false);
        question6.addParent(question4, "OPTIONUID1");
        question4.addChildren(question6);

        questions.put(question1.getUId(), question1);
        questions.put(question2.getUId(), question2);
        questions.put(question3.getUId(), question3);
        questions.put(question4.getUId(), question4);
        questions.put(question5.getUId(), question5);
        questions.put(question6.getUId(), question6);
        Survey survey = Survey.createEmptySurvey(
                "UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", questions);
        return survey;
    }
}
