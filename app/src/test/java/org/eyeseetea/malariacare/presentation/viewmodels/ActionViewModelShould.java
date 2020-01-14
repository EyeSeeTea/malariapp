package org.eyeseetea.malariacare.presentation.viewmodels;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;
import org.junit.Test;

import java.util.Date;

public class ActionViewModelShould {
    @Test
    public void return_is_empty_true_if_all_fields_is_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("", null, "", null);

        assertTrue(actionViewModel.isEmpty());
    }

    @Test
    public void return_is_empty_false_if_description_is_not_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("description", null, "", null);

        assertFalse(actionViewModel.isEmpty());
    }

    @Test
    public void return_is_empty_false_if_due_date_is_not_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("", new Date(), "", null);

        assertFalse(actionViewModel.isEmpty());
    }

    @Test
    public void return_is_empty_false_if_responsible_is_not_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("", null, "responsible", null);

        assertFalse(actionViewModel.isEmpty());
    }

    @Test
    public void return_is_valid_true_if_all_fields_is_not_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("description", new Date(),
                "responsible", null);

        assertTrue(actionViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_description_is_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("description", new Date(), "", null);

        assertFalse(actionViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_due_date_is_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("description", null, "responsible",
                null);

        assertFalse(actionViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_responsible_is_empty() {
        ActionViewModel actionViewModel = new ActionViewModel("", new Date(), "responsible", null);

        assertFalse(actionViewModel.isValid());
    }
}
