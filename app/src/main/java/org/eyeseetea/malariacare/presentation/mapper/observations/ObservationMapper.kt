package org.eyeseetea.malariacare.presentation.mapper.observations

import org.eyeseetea.malariacare.domain.entity.Observation
import org.eyeseetea.malariacare.domain.entity.ObservationValue
import org.eyeseetea.malariacare.domain.entity.ServerMetadata
import org.eyeseetea.malariacare.domain.entity.ServerMetadataItem
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ObservationViewModel
import org.eyeseetea.malariacare.utils.DateParser

import java.util.ArrayList
import java.util.Date

object ObservationMapper {
    fun mapToViewModel(
        observation: Observation,
        serverMetadata: ServerMetadata
    ): ObservationViewModel {
        var provider = ""

        var action1 = ""
        var dueDateAction1: Date? = null
        var responsibleAction1 = ""
        var completionDateAction1: Date? = null

        var action2 = ""
        var dueDateAction2: Date? = null
        var responsibleAction2 = ""
        var completionDateAction2: Date? = null

        var action3 = ""
        var dueDateAction3: Date? = null
        var responsibleAction3 = ""
        var completionDateAction3: Date? = null

        val surveyUid = observation.surveyUid
        val status = observation.status

        for (value in observation.values) {
            if (value.value == null) {
                break
            }

            if (isValueUidEqualsTo(value, serverMetadata.provider)) {
                provider = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.action1)) {
                action1 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.dueDateAction1)) {
                dueDateAction1 = parseValueToDate(value.value)
            } else if (isValueUidEqualsTo(value, serverMetadata.responsibleAction1)) {
                responsibleAction1 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.completionDateAction1)) {
                completionDateAction1 = parseValueToDate(value.value)
            } else if (isValueUidEqualsTo(value, serverMetadata.action2)) {
                action2 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.dueDateAction2)) {
                dueDateAction2 = parseValueToDate(value.value)
            } else if (isValueUidEqualsTo(value, serverMetadata.responsibleAction2)) {
                responsibleAction2 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.completionDateAction2)) {
                completionDateAction2 = parseValueToDate(value.value)
            } else if (isValueUidEqualsTo(value, serverMetadata.action3)) {
                action3 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.dueDateAction3)) {
                dueDateAction3 = parseValueToDate(value.value)
            } else if (isValueUidEqualsTo(value, serverMetadata.responsibleAction3)) {
                responsibleAction3 = value.value
            } else if (isValueUidEqualsTo(value, serverMetadata.completionDateAction3)) {
                completionDateAction3 = parseValueToDate(value.value)
            }
        }

        return ObservationViewModel(
            surveyUid, provider,
            ActionViewModel(action1, dueDateAction1, responsibleAction1, completionDateAction1),
            ActionViewModel(action2, dueDateAction2, responsibleAction2, completionDateAction2),
            ActionViewModel(action3, dueDateAction3, responsibleAction3, completionDateAction3),
            status
        )
    }

    fun mapToObservation(
        viewModel: ObservationViewModel,
        serverMetadata: ServerMetadata
    ): Observation {

        val values = ArrayList<ObservationValue>()

        if (viewModel.provider.isNotEmpty()) {
            values.add(
                ObservationValue(
                    viewModel.provider,
                    serverMetadata.provider.uId
                )
            )
        }

        convertAction1ItemsToValues(viewModel.action1, serverMetadata, values)
        convertAction2ItemsToValues(viewModel.action2, serverMetadata, values)
        convertAction3ItemsToValues(viewModel.action3, serverMetadata, values)

        return Observation.createStoredObservation(
            viewModel.surveyUid,
            viewModel.status, values
        )
    }

    private fun parseValueToDate(value: String): Date? {
        return DateParser().parseDate(value, DateParser.AMERICAN_DATE_FORMAT)
    }

    private fun isValueUidEqualsTo(
        observationValue: ObservationValue,
        serverMetadataItem: ServerMetadataItem
    ): Boolean {
        return observationValue.observationValueUid == serverMetadataItem.uId
    }

    private fun convertAction1ItemsToValues(
        action: ActionViewModel,
        serverMetadata: ServerMetadata,
        values: MutableList<ObservationValue>
    ) {
        if (action.description.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.description,
                    serverMetadata.action1.uId
                )
            )
        }

        if (action.dueDate != null) {
            val value = DateParser().format(
                action.dueDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.dueDateAction1.uId))
        }

        if (action.responsible.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.responsible,
                    serverMetadata.responsibleAction1.uId
                )
            )
        }

        if (action.completionDate != null) {
            val value = DateParser().format(
                action.completionDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.completionDateAction1.uId))
        }
    }

    private fun convertAction2ItemsToValues(
        action: ActionViewModel,
        serverMetadata: ServerMetadata,
        values: MutableList<ObservationValue>
    ) {
        if (action.description.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.description,
                    serverMetadata.action2.uId
                )
            )
        }

        if (action.dueDate != null) {
            val value = DateParser().format(
                action.dueDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.dueDateAction2.uId))
        }

        if (action.responsible.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.responsible,
                    serverMetadata.responsibleAction2.uId
                )
            )
        }

        if (action.completionDate != null) {
            val value = DateParser().format(
                action.completionDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.completionDateAction2.uId))
        }
    }

    private fun convertAction3ItemsToValues(
        action: ActionViewModel,
        serverMetadata: ServerMetadata,
        values: MutableList<ObservationValue>
    ) {
        if (action.description.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.description,
                    serverMetadata.action3.uId
                )
            )
        }

        if (action.dueDate != null) {
            val value = DateParser().format(
                action.dueDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.dueDateAction3.uId))
        }

        if (action.responsible.isNotEmpty()) {
            values.add(
                ObservationValue(
                    action.responsible,
                    serverMetadata.responsibleAction3.uId
                )
            )
        }

        if (action.completionDate != null) {
            val value = DateParser().format(
                action.completionDate,
                DateParser.AMERICAN_DATE_FORMAT
            )
            values.add(ObservationValue(value, serverMetadata.completionDateAction3.uId))
        }
    }
}
