package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel

interface IOrgUnitLevelRepository {
    fun getAll(): List<OrgUnitLevel>
}