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
import java.util.TimeZone;

public class ObservationMapper {
    public static ObservationViewModel mapToViewModel(Observation observation,
            ServerMetadata serverMetadata) {
        String provider = "";
        String planAction = "";

        String activityAction1 = "";
        String subActivityAction1 = "";
        Date dueDateAction1 = null;
        String responsibleAction1 = "";
        boolean realizedAction1 = false;

        String activityAction2 = "";
        String subActivityAction2 = "";
        Date dueDateAction2 = null;
        String responsibleAction2 = "";
        boolean realizedAction2 = false;

        String activityAction3 = "";
        String subActivityAction3 = "";
        Date dueDateAction3 = null;
        String responsibleAction3 = "";
        boolean realizedAction3 = false;

        String surveyUid = observation.getSurveyUid();
        ObservationStatus status = observation.getStatus();

        for (ObservationValue value : observation.getValues()) {
            if (value.getValue() == null) {
                break;
            }

            if (isValueUidEqualsTo(value, serverMetadata.getProvider())) {
                provider = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getPlanAction())) {
                planAction = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getActivityAction1())) {
                activityAction1 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getSubActivityAction1())) {
                subActivityAction1 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction1())) {
                dueDateAction1 = parseValueToLocalDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction1())) {
                responsibleAction1 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getRealizedAction1())) {
                realizedAction1 = Boolean.parseBoolean(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getActivityAction2())) {
                activityAction2 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getSubActivityAction2())) {
                subActivityAction2 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction2())) {
                dueDateAction2 = parseValueToLocalDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction2())) {
                responsibleAction2 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getRealizedAction2())) {
                realizedAction2 = Boolean.parseBoolean(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getActivityAction3())) {
                activityAction3 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getSubActivityAction3())) {
                subActivityAction3 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getDueDateAction3())) {
                dueDateAction3 = parseValueToLocalDate(value.getValue());
            } else if (isValueUidEqualsTo(value, serverMetadata.getResponsibleAction3())) {
                responsibleAction3 = value.getValue();
            } else if (isValueUidEqualsTo(value, serverMetadata.getRealizedAction3())) {
                realizedAction3 = Boolean.parseBoolean(value.getValue());
            }
        }

        ObservationViewModel viewModel =
                new ObservationViewModel(surveyUid, provider, planAction,
                        new ActionViewModel(activityAction1, subActivityAction1, dueDateAction1,
                                responsibleAction1, realizedAction1),
                        new ActionViewModel(activityAction2, subActivityAction2, dueDateAction2,
                                responsibleAction2, realizedAction2),
                        new ActionViewModel(activityAction3, subActivityAction3, dueDateAction3,
                                responsibleAction3, realizedAction3),
                        status);

        return viewModel;
    }

    private static boolean isValueUidEqualsTo(
            ObservationValue observationValue, ServerMetadataItem serverMetadataItem) {
        return observationValue.getObservationValueUid().equals(serverMetadataItem.getUId());
    }

    private static Date parseValueToLocalDate(String value) {
        DateParser dateParser = new DateParser();

        Date utcDate = dateParser.parseDate(value,
                DateParser.LONG_DATE_FORMAT, TimeZone.getTimeZone("UTC"));

        Date localDate = dateParser.toLocalDate(utcDate);

        return localDate;
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

        convertActionItemsToValues(viewModel.getAction1(), serverMetadata, values);
        convertActionItemsToValues(viewModel.getAction2(), serverMetadata, values);
        convertActionItemsToValues(viewModel.getAction3(), serverMetadata, values);

        Observation observation = Observation.createStoredObservation(viewModel.getSurveyUid(),
                viewModel.getStatus(), values);

        return observation;
    }

    private static void convertActionItemsToValues(ActionViewModel action,
            ServerMetadata serverMetadata, List<ObservationValue> values) {
        if (!action.getActivityAction().isEmpty()) {
            values.add(new ObservationValue(action.getActivityAction(),
                    serverMetadata.getActivityAction1().getUId()));
        }

        if (!action.getSubActivityAction().isEmpty()) {
            values.add(new ObservationValue(action.getSubActivityAction(),
                    serverMetadata.getSubActivityAction1().getUId()));
        }

        if (action.getDueDateAction() != null) {
            String value = dateToUTCString(action.getDueDateAction());
            values.add(new ObservationValue(value, serverMetadata.getDueDateAction1().getUId()));
        }

        if (!action.getResponsibleAction().isEmpty()) {
            values.add(new ObservationValue(action.getResponsibleAction(),
                    serverMetadata.getResponsibleAction1().getUId()));
        }

        if (!action.getActivityAction().isEmpty()) {
            values.add(new ObservationValue(String.valueOf(action.isRealized()),
                    serverMetadata.getRealizedAction1().getUId()));
        }
    }

    private static String dateToUTCString(Date localDate) {
        DateParser dateParser = new DateParser();

        Date utcDate = dateParser.toUTCDate(localDate);

        return dateParser.format(utcDate, DateParser.LONG_DATE_FORMAT);
    }
}
