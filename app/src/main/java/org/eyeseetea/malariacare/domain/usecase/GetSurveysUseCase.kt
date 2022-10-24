package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.entity.Survey
import org.eyeseetea.malariacare.domain.entity.SurveyStatus

class GetSurveysUseCase(private val surveyRepository: ISurveyRepository) {
    @Throws(Exception::class)
    fun execute(status: SurveyStatus?, programUid:String?, orgUnitUid:String?): List<Survey> {
        return surveyRepository.getSurveys(status,programUid,orgUnitUid)
    }
}
