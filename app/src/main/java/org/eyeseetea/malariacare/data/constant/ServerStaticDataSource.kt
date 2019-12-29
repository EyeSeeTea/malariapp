package org.eyeseetea.malariacare.data.constant

import android.content.Context
import android.util.Log
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server

class ServerStaticDataSource(private val context: Context) :
    ReadableServerDataSource {
    override fun getAll(): List<Server> {
        Log.d(this.javaClass.simpleName, "Retrieving Servers from static array string")
        val serverUrls = context.resources.getStringArray(R.array.server_list)

        return serverUrls.map { url -> Server(url) }
    }

    override fun get(): Either<ServerDataSourceFailure, Server> { // not implemented
        return Either.Left(ServerDataSourceFailure.ServerNotFoundFailure)
    }
}
