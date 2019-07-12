package org.eyeseetea.malariacare.presentation.mapper.observations;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.ServerMetadataItem;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ObservationViewModel;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ObservationMapper {
    public static ObservationViewModel mapToViewModel(Observation observation,
            ServerMetadata serverMetadata) {
        String provider = "";

        String action1 = "";
        Date dueDateAction1 = null;
        String responsibleAction1 = "";
        Date completionDateAction1 = null;

        String action2 = "";
        Date dueDateAction2 = null;
        String responsibleAction2 = "";
        Date completionDateAction2 = null;

        String action3 = "";
        Date dueDateAction3 = null;
        String responsibleAction3 = "";
        Date completionDateAction3 = null;

        String surveyUid = observation.getSurveyUid();
        ObservationStatus status = observation.getStatus();

        for (ObservationValue value : observation.getValues()) {
            if (value.getValue() == null) {
                break;
            }

            if (isValueUidEqualsTo(value, serverMetadata.getProvider())) {
                provider = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getAction1())) {
                action1 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction1())) {
                dueDateAction1 = parseValueToDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction1())) {
                responsibleAction1 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getCompletionDateAction1())) {
                completionDateAction1 = parseValueToDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getAction2())) {
                action2 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction2())) {
                dueDateAction2 = parseValueToDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction2())) {
                responsibleAction2 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getCompletionDateAction2())) {
                completionDateAction2 = parseValueToDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getAction3())) {
                action3 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction3())) {
                dueDateAction3 = parseValueToDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction3())) {
                responsibleAction3 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getCompletionDateAction3())) {
                completionDateAction3 = parseValueToDate(value.getValue());
            }
        }

        ObservationViewModel viewModel =
                new ObservationViewModel(surveyUid, provider,
                        new ActionViewModel(
                                action1, dueDateAction1, responsibleAction1, completionDateAction1),
                        new ActionViewModel(
                                action2, dueDateAction2, responsibleAction2, completionDateAction2),
                        new ActionViewModel(
                                action3, dueDateAction3, responsibleAction3, completionDateAction3),
                        status);

        return viewModel;
    }

    private static Date parseValueToDate(String value) {
        return new DateParser().parseDate(value, DateParser.AMERICAN_DATE_FORMAT);
    }

    private static boolean isValueUidEqualsTo(
            ObservationValue observationValue, ServerMetadataItem serverMetadataItem) {
        return observationValue.getObservationValueUid().equals(serverMetadataItem.getUId());
    }

    public static Observation mapToObservation(
            ObservationViewModel viewModel, ServerMetadata serverMetadata) {

        List<ObservationValue> values = new ArrayList<>();

        if (!viewModel.getProvider().isEmpty()) {
            values.add(new ObservationValue(viewModel.getProvider(),
                    serverMetadata.getProvider().getUId()));
        }

        convertAction1ItemsToValues(viewModel.getAction1(), serverMetadata, values);
        convertAction2ItemsToValues(viewModel.getAction2(), serverMetadata, values);
        convertAction3ItemsToValues(viewModel.getAction3(), serverMetadata, values);

        Observation observation = Observation.createStoredObservation(viewModel.getSurveyUid(),
                viewModel.getStatus(), values);

        return observation;
    }

    private static void convertAction1ItemsToValues(ActionViewModel action,
            ServerMetadata serverMetadata, List<ObservationValue> values) {
        if (!action.getDescription().isEmpty()) {
            values.add(new ObservationValue(action.getDescription(),
                    serverMetadata.getAction1().getUId()));
        }

        if (action.getDueDate() != null) {
            String value = new DateParser().format(action.getDueDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getDueDateAction1().getUId()));
        }

        if (!action.getResponsible().isEmpty()) {
            values.add(new ObservationValue(action.getResponsible(),
                    serverMetadata.getResponsibleAction1().getUId()));
        }

        if (action.getCompletionDate() != null) {
            String value = new DateParser().format(action.getCompletionDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getCompletionDateAction1().getUId()));
        }
    }

    private static void convertAction2ItemsToValues(ActionViewModel action,
            ServerMetadata serverMetadata, List<ObservationValue> values) {
        if (!action.getDescription().isEmpty()) {
            values.add(new ObservationValue(action.getDescription(),
                    serverMetadata.getAction2().getUId()));
        }

        if (action.getDueDate() != null) {
            String value = new DateParser().format(action.getDueDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getDueDateAction2().getUId()));
        }

        if (!action.getResponsible().isEmpty()) {
            values.add(new ObservationValue(action.getResponsible(),
                    serverMetadata.getResponsibleAction2().getUId()));
        }

        if (action.getCompletionDate() != null) {
            String value = new DateParser().format(action.getCompletionDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getCompletionDateAction2().getUId()));
        }
    }

    private static void convertAction3ItemsToValues(ActionViewModel action,
            ServerMetadata serverMetadata, List<ObservationValue> values) {
        if (!action.getDescription().isEmpty()) {
            values.add(new ObservationValue(action.getDescription(),
                    serverMetadata.getAction3().getUId()));
        }

        if (action.getDueDate() != null) {
            String value = new DateParser().format(action.getDueDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getDueDateAction3().getUId()));
        }

        if (!action.getResponsible().isEmpty()) {
            values.add(new ObservationValue(action.getResponsible(),
                    serverMetadata.getResponsibleAction3().getUId()));
        }

        if (action.getCompletionDate() != null) {
            String value = new DateParser().format(action.getCompletionDate(),
                    DateParser.AMERICAN_DATE_FORMAT);
            values.add(new ObservationValue(value, serverMetadata.getCompletionDateAction3().getUId()));
        }
    }
}
