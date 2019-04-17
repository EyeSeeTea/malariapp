package org.eyeseetea.malariacare.presentation.mapper;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.presentation.viewmodels.ObservationViewModel;

import java.util.ArrayList;
import java.util.List;

public class ObservationMapper {
    public static ObservationViewModel mapToViewModel(Observation observation,
            ServerMetadata serverMetadata) {
        String provider = "";
        String planAction = "";
        String action1 = "";
        String action2 = "";

        String surveyUid = observation.getSurveyUid();
        ObservationStatus status = observation.getStatus();

        for (ObservationValue observationValue : observation.getValues()) {
            if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getProvider().getUId()) && observationValue.getValue() != null) {
                provider = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getPlanAction().getUId()) && observationValue.getValue() != null) {
                planAction = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getAction1().getUId()) && observationValue.getValue() != null) {
                action1 = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getAction2().getUId()) && observationValue.getValue() != null) {
                action2 = observationValue.getValue();
            }
        }


        ObservationViewModel viewModel =
                new ObservationViewModel(surveyUid, provider, planAction, action1, action2,
                        status);

        return viewModel;
    }

    public static Observation mapToObservation(
            ObservationViewModel viewModel, ServerMetadata serverMetadata) {

        List<ObservationValue> values = new ArrayList<>();

        if (!viewModel.getProvider().isEmpty()) {
            values.add(new ObservationValue(viewModel.getProvider(),
                    serverMetadata.getProvider().getUId()));
        }

        if (!viewModel.getActionPlan().isEmpty()) {
            values.add(new ObservationValue(viewModel.getActionPlan(),
                    serverMetadata.getPlanAction().getUId()));
        }

        if (!viewModel.getAction1().isEmpty()) {
            values.add(new ObservationValue(viewModel.getAction1(),
                    serverMetadata.getAction1().getUId()));
        }

        if (!viewModel.getAction2().isEmpty()) {
            values.add(new ObservationValue(viewModel.getAction2(),
                    serverMetadata.getAction2().getUId()));
        }

        Observation observation = Observation.createStoredObservation(viewModel.getSurveyUid(),
                viewModel.getStatus(), values);


        return observation;
    }
}
