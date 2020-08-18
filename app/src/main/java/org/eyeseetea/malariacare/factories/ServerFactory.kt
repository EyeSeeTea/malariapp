package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.authentication.CredentialsFailure
import org.eyeseetea.malariacare.data.authentication.CredentialsReader.poEditorCredentials
import org.eyeseetea.malariacare.data.constant.ServerStaticDataSource
import org.eyeseetea.malariacare.data.database.datasources.ServerLocalDataSource
import org.eyeseetea.malariacare.data.remote.api.ServerRemoteDataSource
import org.eyeseetea.malariacare.data.remote.poeditor.PoEditorApiClient
import org.eyeseetea.malariacare.data.repositories.ServerRepository
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.fold
import org.eyeseetea.malariacare.domain.usecase.GetServerUseCase
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor

object ServerFactory {
    var mainExecutor: IMainExecutor = UIThreadExecutor()
    var asyncExecutor: IAsyncExecutor = AsyncExecutor()

    fun provideGetServersUseCase(context: Context): GetServersUseCase {
        val serverRepository = provideServerRepository(context)
        return GetServersUseCase(serverRepository)
    }

    fun provideGetServerUseCase(context: Context): GetServerUseCase {
        val serverRepository = provideServerRepository(context)
        return GetServerUseCase(serverRepository, mainExecutor, asyncExecutor)
    }

    fun provideServerRepository(context: Context): IServerRepository {
        val result = poEditorCredentials(context)

        return result.fold(
            { left ->
                when (left) {
                    is CredentialsFailure.ParseFailure -> {
                        throw Exception("An error has occurred getting PoEditor credentials")
                    }
                }
            },
            { right ->
                val poEditorApiClient = PoEditorApiClient(right.projectId, right.token)
                val serverLocalDataSource = ServerLocalDataSource()
                val serverRemoteDataSource = ServerRemoteDataSource(poEditorApiClient)
                val serverStaticDataSource = ServerStaticDataSource(context)

                ServerRepository(
                    serverLocalDataSource,
                    serverLocalDataSource,
                    serverRemoteDataSource,
                    serverStaticDataSource
                )
            })
    }
}