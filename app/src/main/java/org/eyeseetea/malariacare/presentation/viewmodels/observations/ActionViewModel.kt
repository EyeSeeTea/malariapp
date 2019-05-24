package org.eyeseetea.malariacare.presentation.viewmodels.observations

import java.util.Date

data class ActionViewModel(
    var description: String = "",
    var dueDate: Date?,
    var responsible: String = "",
    private val completionDateField: Date?
) {

    var completionDate: Date? = null
        private set

    var isCompleted: Boolean
        get() = completionDate != null
        set(completed) = if (completed) {
            completionDate = Date()
        } else {
            completionDate = null
        }

    val isEmpty: Boolean
        get() = description.isEmpty() && dueDate == null && responsible.isEmpty()

    val isValid: Boolean
        get() = description.isNotEmpty() && dueDate != null && responsible.isNotEmpty()

    init {
        this.completionDate = completionDateField
    }
}
