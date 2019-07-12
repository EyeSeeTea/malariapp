package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScoreShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_score_with_mandatory_fields(){
        Score score = new Score("UID", 0f);
        Assert.assertNotNull(score);
        Assert.assertTrue(score.getUId().equals("UID"));
        Assert.assertTrue(score.getScore()==0f);
    }

    @Test
    public void throw_exception_when_create_a_score_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Score UID is required");
        Score score = new Score(null, 0f);
    }

    @Test
    public void create_a_score_with_minimun_valid_score(){
        Score score = new Score("UID", 0f);
        Assert.assertNotNull(score);
        Assert.assertTrue(score.getUId().equals("UID"));
        Assert.assertTrue(score.getScore()==0f);
    }

    @Test
    public void create_a_score_with_maximun_valid_score(){
        Score score = new Score("UID", 100f);
        Assert.assertNotNull(score);
        Assert.assertTrue(score.getUId().equals("UID"));
        Assert.assertTrue(score.getScore()==100f);
    }

    @Test
    public void create_a_score_with_negative_invalid_score(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid score. Score should be a number between 0 and 100");
        Score score = new Score("UID", -1f);
    }

    @Test
    public void create_a_score_with_positive_invalid_score(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid score. Score should be a number between 0 and 100");
        Score score = new Score("UID", 101f);
    }
}