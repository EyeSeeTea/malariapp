package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.Program

interface IProgramRepository {
    fun getAll(): List<Program>
    fun getByUid(uid: String): Program
}