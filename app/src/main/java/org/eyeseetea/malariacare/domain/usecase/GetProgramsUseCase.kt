package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository
import org.eyeseetea.malariacare.domain.entity.Program

class GetProgramsUseCase(private val programRepository: IProgramRepository) {

    @Throws(Exception::class)
    fun execute(): List<Program> {
        return programRepository.getAll()
    }
}
