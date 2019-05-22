package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository
import org.eyeseetea.malariacare.domain.entity.Program

class GetProgramByUidUseCase(private val programRepository: IProgramRepository) {

    fun execute(programUid: String): Program {
        return programRepository.getByUid(programUid)
    }
}
