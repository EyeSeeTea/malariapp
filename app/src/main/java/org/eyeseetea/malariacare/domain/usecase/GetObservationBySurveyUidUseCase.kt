package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository
import org.eyeseetea.malariacare.domain.entity.Observation

class GetObservationBySurveyUidUseCase(
    private val mObservationRepository: IObservationRepository
) {

    @Throws(Exception::class)
    fun execute(surveyUid: String): Observation {
        return mObservationRepository.getObservation(surveyUid)
    }
}
