package org.eyeseetea.malariacare.data.repositories

import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.WritableServerDataSource
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.entity.Server

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

    override val getLoggedServer: Server
        get() {
            val cachedServer = readableServerLocalDataSource.get()

            return if (cachedServer != null && cachedServer.url != null && cachedServer.name != null && cachedServer.logo != null
            ) {
                cachedServer
            } else {
                try {
                    val remoteServer = readableServerRemoteDataSource.get()
                    writableServerLocalDataSource.save(remoteServer)
                    remoteServer
                } catch (e: Exception) {
                    cachedServer
                }
            }
        }

    override fun save(server: Server) {
        writableServerLocalDataSource.save(server)
    }
}
