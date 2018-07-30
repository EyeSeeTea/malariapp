package org.eyeseetea.malariacare.presentation.mapper;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.presentation.viewModels.ObservationsViewModel;

import java.util.ArrayList;
import java.util.List;

public class ObservationMapper {
    public static ObservationsViewModel mapToViewModel(Observation observation,
            ServerMetadata serverMetadata) {
        String provider = "";
        String gaps = "";
        String planAction = "";
        String action1 = "";
        String action2 = "";

        String surveyUid = observation.getSurveyUid();
        ObservationStatus status = observation.getStatus();

        for (ObservationValue observationValue : observation.getValues()) {
            if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getProvider().getUId())) {
                provider = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getGaps().getUId())) {
                gaps = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getPlanAction().getUId())) {
                planAction = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getAction1().getUId())) {
                action1 = observationValue.getValue();
            } else if (observationValue.getObservationValueUid().equals(
                    serverMetadata.getAction2().getUId())) {
                action2 = observationValue.getValue();
            }
        }


        ObservationsViewModel viewModel =
                new ObservationsViewModel(surveyUid, provider, gaps, planAction, action1, action2,
                        status);

        return viewModel;
    }

    public static Observation mapToObservation(
            ObservationsViewModel viewModel, ServerMetadata serverMetadata) {

        List<ObservationValue> values = new ArrayList<>();

        if (viewModel.getProvider() != null && !viewModel.getProvider().isEmpty()) {
            values.add(new ObservationValue(viewModel.getProvider(),
                    serverMetadata.getProvider().getUId()));
        }

        if (viewModel.getGaps() != null && !viewModel.getGaps().isEmpty()) {
            values.add(new ObservationValue(viewModel.getGaps(),
                    serverMetadata.getGaps().getUId()));
        }

        if (viewModel.getPlanAction() != null && !viewModel.getPlanAction().isEmpty()) {
            values.add(new ObservationValue(viewModel.getPlanAction(),
                    serverMetadata.getPlanAction().getUId()));
        }

        if (viewModel.getAction1() != null && !viewModel.getAction1().isEmpty()) {
            values.add(new ObservationValue(viewModel.getAction1(),
                    serverMetadata.getAction1().getUId()));
        }

        if (viewModel.getAction2() != null && !viewModel.getAction2().isEmpty()) {
            values.add(new ObservationValue(viewModel.getAction2(),
                    serverMetadata.getAction1().getUId()));
        }

        Observation observation = Observation.createStoredObservation(viewModel.getSurveyUid(),
                viewModel.getStatus(), values);


        return observation;
    }
}
