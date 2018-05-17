package org.eyeseetea.malariacare.domain.entity;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScoreTypeShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void return_a_type_with_a_max_high_classification() {
        ScoreType scoreType = new ScoreType(100f);

        assertThat(scoreType.isTypeA(), is(true));
        assertThat(scoreType.isTypeB(), is(false));
        assertThat(scoreType.isTypeC(), is(false));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(true));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(false));
    }

    @Test
    public void return_A_type_with_a_min_high_classification() {
        ScoreType scoreType = new ScoreType(90f);

        assertThat(scoreType.isTypeA(), is(true));
        assertThat(scoreType.isTypeB(), is(false));
        assertThat(scoreType.isTypeC(), is(false));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(true));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(false));
    }

    @Test
    public void return_B_type_with_a_max_medium_classification() {
        ScoreType scoreType = new ScoreType(89f);

        assertThat(scoreType.isTypeA(), is(false));
        assertThat(scoreType.isTypeB(), is(true));
        assertThat(scoreType.isTypeC(), is(false));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(true));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(false));
    }

    @Test
    public void return_B_type_with_a_min_medium_classification() {
        ScoreType scoreType = new ScoreType(80f);

        assertThat(scoreType.isTypeA(), is(false));
        assertThat(scoreType.isTypeB(), is(true));
        assertThat(scoreType.isTypeC(), is(false));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(true));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(false));
    }

    @Test
    public void return_B_type_with_a_max_low_classification() {
        ScoreType scoreType = new ScoreType(79f);

        assertThat(scoreType.isTypeA(), is(false));
        assertThat(scoreType.isTypeB(), is(false));
        assertThat(scoreType.isTypeC(), is(true));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(true));
    }

    @Test
    public void return_B_type_with_a_min_low_classification() {
        ScoreType scoreType = new ScoreType(0f);

        assertThat(scoreType.isTypeA(), is(false));
        assertThat(scoreType.isTypeB(), is(false));
        assertThat(scoreType.isTypeC(), is(true));
        assertThat(scoreType.getType().equals(scoreType.HighKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.MediumKey), is(false));
        assertThat(scoreType.getType().equals(scoreType.LowKey), is(true));
    }

    @Test
    public void throw_exception_if_score_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Score is required");

        ScoreType scoreType = new ScoreType(null);
    }
}
