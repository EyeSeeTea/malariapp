package org.eyeseetea.malariacare.domain.entity

data class User(val uid: String, val name: String, val authorities: List<String>)