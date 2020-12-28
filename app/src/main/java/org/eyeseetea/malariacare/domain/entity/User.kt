package org.eyeseetea.malariacare.domain.entity

const val REQUIRED_AUTHORITY = "F_IGNORE_TRACKER_REQUIRED_VALUE_VALIDATION"

data class User(val uid: String, val name: String, val authorities: List<String>)