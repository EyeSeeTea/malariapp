package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository
import org.eyeseetea.malariacare.domain.entity.Survey

class GetSurveyByUidUseCase(private val surveyRepository: ISurveyRepository) {
    fun execute(uid: String): Survey {
        return surveyRepository.getSurveyByUid(uid)
    }
}
