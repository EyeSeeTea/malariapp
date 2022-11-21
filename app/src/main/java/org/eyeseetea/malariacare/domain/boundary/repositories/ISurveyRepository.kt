package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.Survey
import org.eyeseetea.malariacare.domain.entity.SurveyStatus
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter
import org.eyeseetea.malariacare.domain.usecase.SurveysFilter

interface ISurveyRepository {
    @Throws(Exception::class)
    fun getSurveyByUid(uid: String): Survey

    @Throws(Exception::class)
    fun getSurveysByUIds(uids: List<String>): List<Survey>

    @Throws(Exception::class)
    fun getSurveysByFilter(filter: SurveysFilter): List<Survey>

    @Throws(Exception::class)
    fun save(surveys: List<@JvmSuppressWildcards Survey>)
}
