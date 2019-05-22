package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository
import org.eyeseetea.malariacare.domain.entity.Observation

class SaveObservationUseCase(private val mObservationRepository: IObservationRepository) {

    fun execute(observation: Observation) {
        mObservationRepository.save(observation)
    }
}
