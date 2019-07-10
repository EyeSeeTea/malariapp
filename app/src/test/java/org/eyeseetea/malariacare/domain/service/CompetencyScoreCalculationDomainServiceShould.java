package org.eyeseetea.malariacare.domain.service;

import static org.junit.Assert.assertThat;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

public class CompetencyScoreCalculationDomainServiceShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CompetencyScoreCalculationDomainService domainService =
            new CompetencyScoreCalculationDomainService();

    @Test
    public void return_not_competent_if_has_critical_steps_missed() {
        CompetencyScoreClassification classification =
                domainService.calculateClassification(true, 100f);

        assertThat(classification, is(CompetencyScoreClassification.NOT_COMPETENT));
    }

    @Test
    public void return_competent_needs_improvement_if_has_not_critical_steps_missed_and_non_critical_score_is_lower_than_limit() {
        CompetencyScoreClassification classification =
                domainService.calculateClassification(false,
                        CompetencyScoreCalculationDomainService.NON_CRITICAL_COMPETENT_SCORE_LIMIT
                                - 1);

        assertThat(classification, is(CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT));
    }

    @Test
    public void return_competent_if_has_not_critical_steps_missed_and_non_critical_score_is_equal_or_uppers_than_limit() {
        CompetencyScoreClassification classification =
                domainService.calculateClassification(false,
                        CompetencyScoreCalculationDomainService.NON_CRITICAL_COMPETENT_SCORE_LIMIT
                                + 1);

        assertThat(classification, is(CompetencyScoreClassification.COMPETENT));
    }
}
