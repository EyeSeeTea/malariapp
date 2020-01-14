package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository
import org.eyeseetea.malariacare.domain.entity.OrgUnit

class GetOrgUnitsUseCase(private val orgUnitRepository: IOrgUnitRepository) {

    @Throws(Exception::class)
    fun execute(): List<OrgUnit> {
        return orgUnitRepository.getAll()
    }
}
