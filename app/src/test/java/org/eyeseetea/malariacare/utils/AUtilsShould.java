package org.eyeseetea.malariacare.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Test;

public class AUtilsShould {

    @Test
    public void return_always_two_decimals_if_both_are_zero() {
        String value = AUtils.round(12.00283848f, 2);

        assertThat(value, is("12.00"));
    }

    @Test
    public void return_always_two_decimals_if_one_is_0() {
        String value = AUtils.round(12.20398398f, 2);

        assertThat(value, is("12.20"));
    }

    @Test
    public void return_always_two_decimals_to_0_value() {
        String value = AUtils.round(0f, 2);

        assertThat(value, is("0.00"));
    }

    @Test
    public void return_always_two_decimals_if_does_not_exit_0() {
        String value = AUtils.round(34.5848586f, 2);

        assertThat(value, is("34.58"));
    }
}