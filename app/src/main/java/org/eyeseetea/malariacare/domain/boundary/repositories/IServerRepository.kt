package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServerFailure

interface IServerRepository {
    fun getAll(readPolicy: ReadPolicy): List<Server>

    fun getLoggedServer(): Either<GetServerFailure, Server>

    fun save(server: Server)
}