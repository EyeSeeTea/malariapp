package org.eyeseetea.malariacare.presentation.viewmodels;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ObservationViewModel;
import org.junit.Test;

import java.util.Date;

public class ObservationViewModelShould {
    @Test
    public void return_is_valid_true_if_all_actions_are_valid() {
        ActionViewModel action1ViewModel = givenAValidActionViewModel();
        ActionViewModel action2ViewModel = givenAValidActionViewModel();
        ActionViewModel action3ViewModel = givenAValidActionViewModel();

        ObservationViewModel observationViewModel = new
                ObservationViewModel("surveyUid", "Provider",
                action1ViewModel, action2ViewModel, action3ViewModel,
                ObservationStatus.IN_PROGRESS);

        assertTrue(observationViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_all_actions_are_empty() {
        ActionViewModel action1ViewModel = givenAEmptyActionViewModel();
        ActionViewModel action2ViewModel = givenAEmptyActionViewModel();
        ActionViewModel action3ViewModel = givenAEmptyActionViewModel();

        ObservationViewModel observationViewModel = new
                ObservationViewModel("surveyUid", "Provider",
                action1ViewModel, action2ViewModel, action3ViewModel,
                ObservationStatus.IN_PROGRESS);

        assertFalse(observationViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_all_actions_are_invalid() {
        ActionViewModel action1ViewModel = givenAInvalidValidActionViewModel();
        ActionViewModel action2ViewModel = givenAInvalidValidActionViewModel();
        ActionViewModel action3ViewModel = givenAInvalidValidActionViewModel();

        ObservationViewModel observationViewModel = new
                ObservationViewModel("surveyUid", "Provider",
                action1ViewModel, action2ViewModel, action3ViewModel,
                ObservationStatus.IN_PROGRESS);

        assertFalse(observationViewModel.isValid());
    }

    @Test
    public void return_is_valid_true_if_one_action_is_valid_and_rest_are_empty() {
        ActionViewModel action1ViewModel = givenAValidActionViewModel();
        ActionViewModel action2ViewModel = givenAEmptyActionViewModel();
        ActionViewModel action3ViewModel = givenAEmptyActionViewModel();

        ObservationViewModel observationViewModel = new
                ObservationViewModel("surveyUid", "Provider",
                action1ViewModel, action2ViewModel, action3ViewModel,
                ObservationStatus.IN_PROGRESS);

        assertTrue(observationViewModel.isValid());
    }

    @Test
    public void return_is_valid_false_if_one_action_is_valid_and_rest_are_invalid() {
        ActionViewModel action1ViewModel = givenAValidActionViewModel();
        ActionViewModel action2ViewModel = givenAInvalidValidActionViewModel();
        ActionViewModel action3ViewModel = givenAInvalidValidActionViewModel();

        ObservationViewModel observationViewModel = new
                ObservationViewModel("surveyUid", "Provider",
                action1ViewModel, action2ViewModel, action3ViewModel,
                ObservationStatus.IN_PROGRESS);

        assertFalse(observationViewModel.isValid());
    }

    private ActionViewModel givenAValidActionViewModel() {
        return new ActionViewModel("description", new Date(), "responsible", new Date());
    }

    private ActionViewModel givenAInvalidValidActionViewModel() {
        return new ActionViewModel("", new Date(), "responsible", new Date());
    }

    private ActionViewModel givenAEmptyActionViewModel() {
        return new ActionViewModel("", null, "", new Date());
    }
}
