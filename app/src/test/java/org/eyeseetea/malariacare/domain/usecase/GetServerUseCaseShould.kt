package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.data.WritableServerDataSource
import org.eyeseetea.malariacare.data.repositories.ServerRepository
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.common.fold
import org.eyeseetea.malariacare.domain.entity.Server
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class GetServerUseCaseShould {
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
        val useCase = givenAGetServerUseCase()
        val localServer = givenACompleteServerInLocalDataSource()

        useCase.execute(object :
            GetServerUseCase.Callback {
            override fun onSuccess(serverResult: Either<GetServerFailure, Server>) {
                serverResult.fold(
                    { Assert.fail() },
                    { server -> Assert.assertEquals(localServer, server) }
                )
            }
        })
    }

    @Test
    fun `return remote server if local is incomplete`() {
        val useCase = givenAGetServerUseCase()
        givenAIncompleteServerInLocalDataSource()
        val remoteServer = givenACompleteServerInRemoteDataSource()

        useCase.execute(object :
            GetServerUseCase.Callback {
            override fun onSuccess(serverResult: Either<GetServerFailure, Server>) {
                serverResult.fold(
                    { Assert.fail() },
                    { server -> Assert.assertEquals(remoteServer, server) }
                )
            }
        })
    }

    @Test
    fun `return incomplete local server if remote return NetworkFailure`() {
        val useCase = givenAGetServerUseCase()
        val localServer = givenAIncompleteServerInLocalDataSource()
        givenANetworkFailureInRemoteDataSource()

        useCase.execute(object :
            GetServerUseCase.Callback {
            override fun onSuccess(serverResult: Either<GetServerFailure, Server>) {
                serverResult.fold(
                    { Assert.fail() },
                    { server -> Assert.assertEquals(localServer, server) }
                )
            }
        })
    }

    @Test
    fun `return not found failure if local does not exists and remote return NetworkFailure`() {
        val useCase = givenAGetServerUseCase()
        givenANotFoundFailureInLocalataSource()
        givenANetworkFailureInRemoteDataSource()

        useCase.execute(object :
            GetServerUseCase.Callback {
            override fun onSuccess(serverResult: Either<GetServerFailure, Server>) {
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
        })
    }

    private fun givenAGetServerUseCase(): GetServerUseCase {
        val serverRepository = ServerRepository(
            writableServerDataSource,
            serverLocalDataSource,
            serverRemoteDataSource,
            serverStaticDataSource
        )

        val mainExecutor = IMainExecutor { runnable -> runnable.run() }
        val asyncExecutor = IAsyncExecutor { runnable -> runnable.run() }

        return GetServerUseCase(serverRepository, mainExecutor, asyncExecutor)
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

    private fun givenANotFoundFailureInLocalataSource() {
        Mockito.`when`(serverLocalDataSource.get())
            .thenReturn(Either.Left(ServerDataSourceFailure.ServerNotFoundFailure))
    }
}