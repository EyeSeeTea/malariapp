package org.eyeseetea.malariacare.data

import org.eyeseetea.malariacare.domain.entity.Server

interface ReadableServerDataSource {
    fun getAll(): List<Server>

    @Throws(Exception::class)
    fun get(): Server
}

interface WritableServerDataSource {
    @Throws(Exception::class)
    fun save(server: Server)

    @Throws(Exception::class)
    fun saveAll(servers: List<Server>)
}