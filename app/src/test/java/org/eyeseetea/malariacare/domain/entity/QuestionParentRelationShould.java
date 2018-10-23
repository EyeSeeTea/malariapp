package org.eyeseetea.malariacare.domain.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuestionParentRelationShould {

    @Test
    public void return_true_on_check_if_exist_a_question_option_after_added() {
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid", "uid");
        //when
        questionParentRelations.addQuestionOptionRelation(questionOption);
        //then
        assertThat(questionParentRelations.checkIfExist(questionOption), is(true));
    }

    @Test
    public void return_false_on_check_if_exist_a_question_option_after_added(){
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid","uid");

        //when
        questionParentRelations.addQuestionOptionRelation(questionOption);

        //then
        questionOption = new QuestionOption("uid2","uid2");
        assertThat(questionParentRelations.checkIfExist(questionOption), is(false));
    }

    @Test
    public void return_true_when_active_parent_has_an_active_parent_already_added(){
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid","uid");
        questionOption.doMatch();

        //when
        questionParentRelations.addQuestionOptionRelation(questionOption);

        //then
        assertThat(questionParentRelations.hasActiveMatches(), is(true));
    }

    @Test
    public void return_false_when_active_parent_is_not_added(){
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid","uid");
        questionOption.doUnMatch();

        //when
        questionParentRelations.addQuestionOptionRelation(questionOption);

        //then
        assertThat(questionParentRelations.hasActiveMatches(), is(false));
    }

    @Test
    public void return_true_when_add_unmatched_relation_and_update_that_relation_with_matched_relation(){
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid","uid");
        questionOption.doUnMatch();
        questionParentRelations.addQuestionOptionRelation(questionOption);

        //when
        questionOption.doMatch();
        questionParentRelations.updateQuestionOption(questionOption);

        //then
        assertThat(questionParentRelations.hasActiveMatches(), is(true));
    }

    @Test
    public void return_false_when_add_matched_relation_and_update_that_relation_with_unmatched_relation(){
        //given
        QuestionParentRelations questionParentRelations = new QuestionParentRelations();
        QuestionOption questionOption = new QuestionOption("uid","uid");
        questionOption.doMatch();
        questionParentRelations.addQuestionOptionRelation(questionOption);

        //when
        questionOption.doUnMatch();
        questionParentRelations.updateQuestionOption(questionOption);

        //then
        assertThat(questionParentRelations.hasActiveMatches(), is(false));
    }

}
