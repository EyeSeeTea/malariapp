package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel

class GetOrgUnitLevelsUseCase(private val orgUnitRepository: IOrgUnitLevelRepository) {
    @Throws(Exception::class)
    fun execute(): List<OrgUnitLevel> {
        return orgUnitRepository.getAll()
    }
}
