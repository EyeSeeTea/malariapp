package org.eyeseetea.malariacare.data.repositories

import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.data.WritableServerDataSource
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.common.fold
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServerFailure

class ServerRepository(
    private val writableServerLocalDataSource: WritableServerDataSource,
    private val readableServerLocalDataSource: ReadableServerDataSource,
    private val readableServerRemoteDataSource: ReadableServerDataSource,
    private val readableServerStaticDataSource: ReadableServerDataSource
) : IServerRepository {

    override fun getAll(readPolicy: ReadPolicy): List<Server> {
        var servers: List<Server>

        if (readPolicy == ReadPolicy.NETWORK_FIRST) {
            servers = readableServerRemoteDataSource.getAll()
            if (servers.isEmpty()) {
                servers = readableServerLocalDataSource.getAll()
            }
            if (servers.isEmpty()) {
                servers = readableServerStaticDataSource.getAll()
            }

            writableServerLocalDataSource.saveAll(servers)
        } else {
            servers = readableServerLocalDataSource.getAll()

            if (servers.isEmpty()) {
                servers = readableServerStaticDataSource.getAll()
            }
        }
        return servers
    }

    override fun save(server: Server) {
        writableServerLocalDataSource.save(server)
    }

    override fun getLoggedServer(): Either<GetServerFailure, Server> {
        val localServerResult = readableServerLocalDataSource.get()

        return localServerResult.fold(
            { getServerFromRemote(localServerResult) },
            { server ->
                if (server.isDataCompleted()) {
                    Either.Right(server)
                } else {
                    getServerFromRemote(localServerResult)
                }
            })
    }

    private fun getServerFromRemote(localResult: Either<ServerDataSourceFailure, Server>): Either<GetServerFailure, Server> {
        val remoteServerResult = readableServerRemoteDataSource.get()

        return remoteServerResult.fold(
            { handleRemoteFailure(localResult) },
            { server ->
                writableServerLocalDataSource.save(server)
                Either.Right(server)
            })
    }

    private fun handleRemoteFailure(
        localResult: Either<ServerDataSourceFailure, Server>
    ): Either<GetServerFailure, Server> {

        return localResult.fold(
            { Either.Left(GetServerFailure.ServerNotFoundFailure) },
            { server -> Either.Right(server) })
    }
}
