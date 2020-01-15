package org.eyeseetea.malariacare.data.database.repositories

import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.data.WritableServerDataSource
import org.eyeseetea.malariacare.data.repositories.ServerRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.common.fold
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServerFailure
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ServerRepositoryShould {
    @Rule
    @JvmField
    var rule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var serverLocalDataSource: ReadableServerDataSource

    @Mock
    lateinit var serverRemoteDataSource: ReadableServerDataSource

    @Mock
    lateinit var serverStaticDataSource: ReadableServerDataSource

    @Mock
    lateinit var writableServerDataSource: WritableServerDataSource

    @Test
    fun `return local server if exists and it's completed`() {
        val repository = givenAServerRepository()
        val localServer = givenACompleteServerInLocalDataSource()

        val serverResult = repository.getLoggedServer()

        serverResult.fold(
            { Assert.fail() },
            { server -> Assert.assertEquals(localServer, server) }
        )
    }

    @Test
    fun `return remote server if local is incomplete`() {
        val repository = givenAServerRepository()
        givenAIncompleteServerInLocalDataSource()
        val remoteServer = givenACompleteServerInRemoteDataSource()

        val serverResult = repository.getLoggedServer()

        serverResult.fold(
            { Assert.fail() },
            { server -> Assert.assertEquals(remoteServer, server) }
        )
    }

    @Test
    fun `return incomplete local server if remote return NetworkFailure`() {
        val repository = givenAServerRepository()
        val localServer = givenAIncompleteServerInLocalDataSource()
        givenANetworkFailureInRemoteDataSource()

        val serverResult = repository.getLoggedServer()

        serverResult.fold(
            { Assert.fail() },
            { server -> Assert.assertEquals(localServer, server) }
        )
    }

    @Test
    fun `return not found failure if local does not exists and remote return NetworkFailure`() {
        val repository = givenAServerRepository()
        givenANotFoundFailureInLocalDataSource()
        givenANetworkFailureInRemoteDataSource()

        val serverResult = repository.getLoggedServer()

        serverResult.fold(
            { failure ->
                Assert.assertEquals(
                    GetServerFailure.ServerNotFoundFailure,
                    failure
                )
            },
            { Assert.fail() }
        )
    }

    @Test
    fun `return static server list if there are a network error and empty local database`() {
        val repository = givenAServerRepository()
        givenEmptyServersInLocalDataSource()
        val serverList = givenAServerListInStaticDataSource()
        givenANetworkFailureInRemoteDataSource()

        val serversResult = repository.getAll(ReadPolicy.NETWORK_FIRST)

        Assert.assertEquals(serverList, serversResult)
    }

    @Test
    fun `return local server list if not empty and there are a network error`() {
        val repository = givenAServerRepository()
        val serverList = givenAServerListInLocalDataSource()
        givenAServerListInStaticDataSource()
        givenANetworkFailureInRemoteDataSource()

        val serversResult = repository.getAll(ReadPolicy.NETWORK_FIRST)

        Assert.assertEquals(serverList, serversResult)
    }

    @Test
    fun `return remote server list if not empty and there are a not network error`() {
        val repository = givenAServerRepository()
        givenAServerListInLocalDataSource()
        givenAServerListInStaticDataSource()
        val serverList = givenAServerListInRemoteDataSource()

        val serversResult = repository.getAll(ReadPolicy.NETWORK_FIRST)

        Assert.assertEquals(serverList, serversResult)
    }

    @Test
    fun `return static server list if remote and local server list is empty`() {
        val repository = givenAServerRepository()
        givenEmptyServersInLocalDataSource()
        givenEmptyServersInRemoteDataSource()
        val serverList = givenAServerListInStaticDataSource()

        val serversResult = repository.getAll(ReadPolicy.NETWORK_FIRST)

        Assert.assertEquals(serverList, serversResult)
    }

    @Test
    fun `return local server list if remote sever list is empty`() {
        val repository = givenAServerRepository()
        givenAServerListInStaticDataSource()
        givenEmptyServersInRemoteDataSource()
        val serverList = givenAServerListInLocalDataSource()

        val serversResult = repository.getAll(ReadPolicy.NETWORK_FIRST)

        Assert.assertEquals(serverList, serversResult)
    }

    private fun givenAServerRepository(): ServerRepository {
        return ServerRepository(
            writableServerDataSource,
            serverLocalDataSource,
            serverRemoteDataSource,
            serverStaticDataSource
        )
    }

    private fun givenACompleteServerInLocalDataSource(): Server {
        val server = Server("fake url", "fake name", ByteArray(45), true)

        Mockito.`when`(serverLocalDataSource.get()).thenReturn(Either.Right(server))

        return server
    }

    private fun givenAIncompleteServerInLocalDataSource(): Server {
        val server = Server("fake url", null, null, true)

        Mockito.`when`(serverLocalDataSource.get()).thenReturn(Either.Right(server))

        return server
    }

    private fun givenACompleteServerInRemoteDataSource(): Server {
        val server = Server("fake url", null, null, true)

        Mockito.`when`(serverRemoteDataSource.get()).thenReturn(Either.Right(server))

        return server
    }

    private fun givenANetworkFailureInRemoteDataSource() {
        Mockito.`when`(serverRemoteDataSource.get())
            .thenReturn(Either.Left(ServerDataSourceFailure.NetworkFailure))
    }

    private fun givenANotFoundFailureInLocalDataSource() {
        Mockito.`when`(serverLocalDataSource.get())
            .thenReturn(Either.Left(ServerDataSourceFailure.ServerNotFoundFailure))
    }

    private fun givenAServerListInStaticDataSource(): List<Server> {
        val serverList = listOf(
            Server("https://myanmar.psi-mis.org/"),
            Server("https://sfhza.psi-mis.org/")
        )

        Mockito.`when`(serverStaticDataSource.getAll()).thenReturn(serverList)

        return serverList
    }

    private fun givenAServerListInLocalDataSource(): List<Server> {
        val serverList = listOf(
            Server("https://data.psi-mis.org/"),
            Server("https://clone.psi-mis.org/")
        )

        Mockito.`when`(serverLocalDataSource.getAll()).thenReturn(serverList)

        return serverList
    }

    private fun givenAServerListInRemoteDataSource(): List<Server> {
        val serverList = listOf(
            Server("https://sfhza.psi-mis.org/"),
            Server("https://zw.psi-mis.org/")
        )

        Mockito.`when`(serverRemoteDataSource.getAll()).thenReturn(serverList)

        return serverList
    }

    private fun givenEmptyServersInLocalDataSource(): List<Server> {
        val serverList = listOf<Server>()
        Mockito.`when`(serverLocalDataSource.getAll()).thenReturn(serverList)

        return serverList
    }

    private fun givenEmptyServersInRemoteDataSource(): List<Server> {
        val serverList = listOf<Server>()
        Mockito.`when`(serverRemoteDataSource.getAll()).thenReturn(serverList)

        return serverList
    }
}