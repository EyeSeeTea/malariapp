package org.eyeseetea.malariacare.domain.service;

import static org.junit.Assert.assertThat;

import org.eyeseetea.malariacare.domain.entity.CompetentScoreClassification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

public class CompetentScoreCalculationDomainServiceShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CompetentScoreCalculationDomainService domainService =
            new CompetentScoreCalculationDomainService();

    @Test
    public void return_not_competent_if_has_critical_steps_missed() {
        CompetentScoreClassification classification =
                domainService.calculateClassification(true, 100f);

        assertThat(classification, is(CompetentScoreClassification.NOT_COMPETENT));
    }

    @Test
    public void return_competent_needs_improvement_if_has_not_critical_steps_missed_and_non_critical_score_is_lower_than_limit() {
        CompetentScoreClassification classification =
                domainService.calculateClassification(false,
                        CompetentScoreCalculationDomainService.NON_CRITICAL_COMPETENT_SCORE_LIMIT
                                - 1);

        assertThat(classification, is(CompetentScoreClassification.COMPETENT_NEEDS_IMPROVEMENT));
    }

    @Test
    public void return_competent_if_has_not_critical_steps_missed_and_non_critical_score_is_equal_or_uppers_than_limit() {
        CompetentScoreClassification classification =
                domainService.calculateClassification(false,
                        CompetentScoreCalculationDomainService.NON_CRITICAL_COMPETENT_SCORE_LIMIT
                                + 1);

        assertThat(classification, is(CompetentScoreClassification.COMPETENT));
    }
}
