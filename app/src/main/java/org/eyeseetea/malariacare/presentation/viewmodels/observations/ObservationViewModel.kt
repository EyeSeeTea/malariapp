package org.eyeseetea.malariacare.presentation.viewmodels.observations

import org.eyeseetea.malariacare.domain.entity.ObservationStatus

data class ObservationViewModel(val surveyUid: String) {
    var status = ObservationStatus.IN_PROGRESS

    var provider = ""
    var action1: ActionViewModel
    var action2: ActionViewModel
    var action3: ActionViewModel

    init {
        action1 = ActionViewModel("", null, "", null)
        action2 = ActionViewModel("", null, "", null)
        action3 = ActionViewModel("", null, "", null)
    }

    constructor(
        surveyUid: String,
        provider: String,
        action1: ActionViewModel,
        action2: ActionViewModel,
        action3: ActionViewModel,
        status: ObservationStatus
    ) : this(surveyUid) {
        this.provider = provider
        this.action1 = action1
        this.action2 = action2
        this.action3 = action3
        this.status = status
    }

    val isValid: Boolean
        get() {
            val atLeastOneFilled = !(action1.isEmpty && action2.isEmpty && action3.isEmpty)

            return atLeastOneFilled &&
                (action1.isValid || action1.isEmpty) &&
                (action2.isValid || action2.isEmpty) &&
                (action3.isValid || action3.isEmpty)
        }

    val allActionsCompleted: Boolean
        get() {
            val atLeastOneFilled = !(action1.isEmpty && action2.isEmpty && action3.isEmpty)

            return atLeastOneFilled &&
                (action1.isCompleted || action1.isEmpty) &&
                (action2.isCompleted || action2.isEmpty) &&
                (action3.isCompleted || action3.isEmpty)
        }
}
