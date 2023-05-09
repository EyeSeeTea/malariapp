package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.entity.Survey
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter
import java.util.Date

data class SurveysFilter(val status: SurveyStatusFilter?, val programUid:String?, val orgUnitUid:String?, val startDate:Date? = null)

class GetSurveysUseCase(private val surveyRepository: ISurveyRepository) {
    @Throws(Exception::class)
    fun execute(filter:SurveysFilter): List<Survey> {
        return surveyRepository.getSurveysByFilter(filter)
    }
}
