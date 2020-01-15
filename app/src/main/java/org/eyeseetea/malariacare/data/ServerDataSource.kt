package org.eyeseetea.malariacare.data

import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server

sealed class ServerDataSourceFailure {
    object NetworkFailure : ServerDataSourceFailure()
    object ServerNotFoundFailure : ServerDataSourceFailure()
}

interface ReadableServerDataSource {
    fun getAll(): List<Server>
    fun get(): Either<ServerDataSourceFailure, Server>
}

interface WritableServerDataSource {
    fun save(server: Server)
    fun saveAll(servers: List<Server>)
}