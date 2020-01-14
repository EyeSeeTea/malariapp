package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.OrgUnit

interface IOrgUnitRepository {
    fun getAll(): List<OrgUnit>
    fun getByUid(uid: String): OrgUnit
}