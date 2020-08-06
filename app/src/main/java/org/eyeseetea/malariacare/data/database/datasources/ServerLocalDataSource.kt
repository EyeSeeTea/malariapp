package org.eyeseetea.malariacare.data.database.datasources

import android.util.Log
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.sql.language.Delete
import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.data.WritableServerDataSource
import org.eyeseetea.malariacare.data.database.model.ServerDB
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.entity.ServerClassification

class ServerLocalDataSource : ReadableServerDataSource, WritableServerDataSource {
    override fun get(): Either<ServerDataSourceFailure, Server> {
        Log.d(this.javaClass.simpleName, "Retrieving connected server from local Database")
        val serverDB = ServerDB.getConnectedServerFromDB()

        return if (serverDB == null) {
            Either.Left(ServerDataSourceFailure.ServerNotFoundFailure)
        } else {
            Either.Right(mapServer(serverDB))
        }
    }

    override fun saveAll(servers: List<Server>) {
        Delete.table(ServerDB::class.java)
        for (server in servers) {
            save(server)
        }
    }

    override fun save(server: Server) {
        var serverDB = ServerDB.getServerFromDByUrl(server.url)

        if (serverDB == null) {
            serverDB = ServerDB()

            // server classification is assigned only first time when is saving all servers from
            // poeditor response
            serverDB.classification = server.classification.code
        }

        serverDB.url = server.url
        serverDB.name = server.name
        serverDB.isConnected = server.isConnected

        if (server.logo != null) {
            serverDB.logo = Blob(server.logo)
        }

        serverDB.save()
    }

    override fun getAll(): List<Server> {
        Log.d(this.javaClass.simpleName, "Retrieving Servers from local Database")
        return ServerDB.getAllServersFromDB().map { serverDB -> mapServer(serverDB) }
    }

    private fun mapServer(serverDB: ServerDB): Server {
        return Server(
            serverDB.url,
            serverDB.name,
            serverDB.logo?.blob,
            serverDB.isConnected,
            ServerClassification[serverDB.classification] ?: ServerClassification.COMPETENCIES
        )
    }
}
