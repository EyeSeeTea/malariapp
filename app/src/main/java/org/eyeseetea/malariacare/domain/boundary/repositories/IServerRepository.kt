package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.entity.Server

interface IServerRepository {
    fun getAll(readPolicy: ReadPolicy): List<Server>

    @get:Throws(Exception::class)
    val loggedServer: Server

    @Throws(Exception::class)
    fun save(server: Server)
}