package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository
import org.eyeseetea.malariacare.domain.entity.OrgUnit

class GetOrgUnitByUidUseCase(private val orgUnitRepository: IOrgUnitRepository) {

    fun execute(uid: String): OrgUnit {
        return orgUnitRepository.getByUid(uid)
    }
}
