package org.eyeseetea.malariacare.domain.entity

data class AppSettings(
    val isEulaAccepted: Boolean,
    val isPullCompleted: Boolean,
    val credentials: Credentials?
)