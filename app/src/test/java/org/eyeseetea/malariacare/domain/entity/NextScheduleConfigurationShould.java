package org.eyeseetea.malariacare.domain.entity;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class NextScheduleConfigurationShould {

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_for_empty_matrix() {
        new NextScheduleConfiguration("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_for_null_matrix() {
        new NextScheduleConfiguration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_matrix_has_less_of_three_competency_items() {
        new NextScheduleConfiguration("4,4;3,3");

    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_matrix_has_more_of_three_competency_items() {
        new NextScheduleConfiguration("4,4;3,3;3,1;5,2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_competent() {
        new NextScheduleConfiguration("4;3,3;3,1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_competent_improv() {
        new NextScheduleConfiguration("4,4;3;3,1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_does_not_exists_one_item_for_every_productivity_in_not_competent() {
        new NextScheduleConfiguration("4,4;3,3;1");
    }

    @Test
    public void create_next_schedule_configuration_successfully() {
        NextScheduleConfiguration nextScheduleConfiguration =
                new NextScheduleConfiguration("4,4;3,3;3,1");

        assertEquals(4, nextScheduleConfiguration.getCompetentHighProductivityMonths());
        assertEquals(4, nextScheduleConfiguration.getCompetentLowProductivityMonths());

        assertEquals(3,
                nextScheduleConfiguration.getCompetentNeedsImprovementHighProductivityMonths());
        assertEquals(3,
                nextScheduleConfiguration.getCompetentNeedsImprovementLowProductivityMonths());

        assertEquals(3, nextScheduleConfiguration.getNotCompetentHighProductivityMonths());
        assertEquals(1, nextScheduleConfiguration.getNotCompetentLowProductivityMonths());
    }
}
