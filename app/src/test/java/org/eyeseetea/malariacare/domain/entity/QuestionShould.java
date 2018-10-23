package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QuestionShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_question_with_mandatory_fields(){
        Question question = new Question("UID",1, true);
        Assert.assertNotNull(question);
        Assert.assertTrue(question.getUId().equals("UID"));
        Assert.assertTrue(question.isCompulsory());
        Assert.assertTrue(question.isComputable());
    }

    @Test
    public void create_a_question_with_mandatory_fields_with_not_computable_question_type(){
        Question question = new Question("UID",7, true);
        Assert.assertNotNull(question);
        Assert.assertTrue(question.getUId().equals("UID"));
        Assert.assertTrue(question.isCompulsory());
        Assert.assertTrue(!question.isComputable());
    }

    @Test
    public void create_a_question_with_mandatory_fields_with_answer_name(){
        Question question = new Question("UID",7, true,"answerName");
        Assert.assertNotNull(question);
        Assert.assertTrue(question.getUId().equals("UID"));
        Assert.assertTrue(question.isCompulsory());
        Assert.assertTrue(!question.isComputable());
        Assert.assertTrue(question.getAnswerName().equals("answerName"));
    }

    @Test
    public void throw_exception_when_create_a_question_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("question uId is required");
        new Question(null, 1, false);
    }

    @Test
    public void throw_exception_when_create_a_question_with_invalid_question_type(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("valid question type is required");
        new Question("UId", 0, false);
    }

    @Test
    public void return_has_parent_and_has_children_are_correct_when_relation_parent_child_is_created(){
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        assertThat(questionChild.hasParents(), is(true));
        assertThat(questionParent.hasChildren(), is(true));
    }

    @Test
    public void return_is_visible_false_when_relation_parent_child_is_created(){
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        assertThat(questionChild.isVisible(), is(false));
    }

    @Test
    public void return_is_visible_true_when_relation_parent_child_is_created_and_answered_question_options_do_match(){
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        QuestionValue questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_MATCH_UID", "Dummy value");

        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        assertThat(questionChild.isVisible(), is(true));
    }

    @Test
    public void return_is_visible_false_when_relation_parent_child_is_created_and_answered_question_options_not_match(){
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionGrandParent = new Question("UID_parent", 2, false,"Do you have grandchildren?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        questionGrandParent.addQuestionParentAndOptionMatch(questionGrandParent, "OPTION_GRANDPARENT_MATCH_UID");
        questionGrandParent.addChildren(questionChild.getUId());

        QuestionValue questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_WITHOUT_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        assertThat(questionChild.isVisible(), is(false));
    }

    @Test
    public void return_is_visible_true_false_relation_parent_child_with_multiple_parents_is_created_and_answered_question_options_not_match(){
        //given
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionGrandParent = new Question("UID_parent", 2, false,"Do you have grandchildren?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        questionGrandParent.addQuestionParentAndOptionMatch(questionGrandParent, "OPTION_GRANDPARENT_MATCH_UID");
        questionGrandParent.addChildren(questionChild.getUId());

        //when
        QuestionValue questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_WITHOUT_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        //then
        assertThat(questionChild.isVisible(), is(false));
    }

    @Test
    public void return_is_visible_true_when_relation_parent_child_with_multiple_parents_is_created_and_answered_question_options_do_match(){
        //given
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionGrandParent = new Question("UID_parent", 2, false,"Do you have grandchildren?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        questionGrandParent.addQuestionParentAndOptionMatch(questionGrandParent, "OPTION_GRANDPARENT_MATCH_UID");
        questionGrandParent.addChildren(questionChild.getUId());

        //when
        QuestionValue questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_GRANDPARENT_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        //then
        assertThat(questionChild.isVisible(), is(true));
    }

    @Test
    public void return_is_visible_true_when_relation_parent_child_with_multiple_parents_is_created_and_answered_question_options_deactivate_only_one_parent_do_match(){
        //given
        Question questionParent = new Question("UID_parent", 2, false,"Do you have children?");
        Question questionGrandParent = new Question("UID_parent", 2, false,"Do you have grandchildren?");
        Question questionChild = new Question("UID_child", 2, false,"How many children do you have?");

        questionChild.addQuestionParentAndOptionMatch(questionParent, "OPTION_MATCH_UID");
        questionParent.addChildren(questionChild.getUId());

        questionGrandParent.addQuestionParentAndOptionMatch(questionGrandParent, "OPTION_GRANDPARENT_MATCH_UID");
        questionGrandParent.addChildren(questionChild.getUId());

        //when
        QuestionValue questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_GRANDPARENT_MATCH_UID", "Dummy value");
        questionChild.activateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        questionValue = QuestionValue.createOptionValue("UID_parent", "OPTION_GRANDPARENT_MATCH_UID", "Dummy value");
        questionChild.deactivateQuestionOptionMatch(questionValue.getQuestionUId(), questionValue.getOptionUId());

        //then
        assertThat(questionChild.isVisible(), is(true));
    }
}
