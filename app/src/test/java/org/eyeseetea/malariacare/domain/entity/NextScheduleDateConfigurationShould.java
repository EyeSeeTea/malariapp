package org.eyeseetea.malariacare.domain.entity;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class NextScheduleDateConfigurationShould {

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_for_empty_matrix() {
        new NextScheduleDateConfiguration("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_for_null_matrix() {
        new NextScheduleDateConfiguration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_matrix_has_less_of_three_competency_items() {
        new NextScheduleDateConfiguration("4,4;3,3");

    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_matrix_has_more_of_three_competency_items() {
        new NextScheduleDateConfiguration("4,4;3,3;3,1;5,2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_competent() {
        new NextScheduleDateConfiguration("4;3,3;3,1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_competent_improv() {
        new NextScheduleDateConfiguration("4,4;3;3,1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_not_competent() {
        new NextScheduleDateConfiguration("4,4;3,3;1");
    }

    @Test
    public void create_next_schedule_configuration_successfully() {
        NextScheduleDateConfiguration nextScheduleDateConfiguration =
                new NextScheduleDateConfiguration("6,5;4,3;2,1");

        assertEquals(6, nextScheduleDateConfiguration.getCompetentOrALowProductivityMonths());
        assertEquals(5, nextScheduleDateConfiguration.getCompetentOrAHighProductivityMonths());

        assertEquals(4,
                nextScheduleDateConfiguration.getCompetentNeedsImprovementOrBLowProductivityMonths());
        assertEquals(3,
                nextScheduleDateConfiguration.getCompetentNeedsImprovementOrBHighProductivityMonths());

        assertEquals(2, nextScheduleDateConfiguration.getNotCompetentOrCLowProductivityMonths());
        assertEquals(1, nextScheduleDateConfiguration.getNotCompetentOrCHighProductivityMonths());
    }
}
