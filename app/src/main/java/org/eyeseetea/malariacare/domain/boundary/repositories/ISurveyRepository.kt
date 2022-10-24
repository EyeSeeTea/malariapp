package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.Survey
import org.eyeseetea.malariacare.domain.entity.SurveyStatus

interface ISurveyRepository {
    @Throws(Exception::class)
    fun getSurveyByUid(uid: String): Survey

    @Throws(Exception::class)
    fun getSurveysByUIds(uids: List<String>): List<Survey>

    @Throws(Exception::class)
    fun getSurveys(status: SurveyStatus?, programUId:String?, orgUnitUId:String?): List<Survey>

    @Throws(Exception::class)
    fun save(surveys: List<@JvmSuppressWildcards Survey>)
}
