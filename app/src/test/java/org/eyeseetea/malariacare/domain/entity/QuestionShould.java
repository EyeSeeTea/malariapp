package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

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
}
