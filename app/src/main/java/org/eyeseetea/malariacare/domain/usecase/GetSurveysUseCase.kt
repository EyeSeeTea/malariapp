package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.entity.Survey

class GetSurveysUseCase(private val surveyRepository: ISurveyRepository) {

    @Throws(Exception::class)
    fun execute(uids: List<String>): List<Survey> {
        return surveyRepository.getSurveysByUIds(uids)
    }
}
