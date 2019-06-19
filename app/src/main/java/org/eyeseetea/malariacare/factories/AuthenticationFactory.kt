package org.eyeseetea.malariacare.factories

import android.content.Context

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor

object AuthenticationFactory {
    private val mainExecutor = UIThreadExecutor()
    private val asyncExecutor = AsyncExecutor()

    fun provideLoginUseCase(context: Context): LoginUseCase {

        val userAccountRepository = provideUserAccountRepository(context)
        val serverLocalDataSource = ServerInfoLocalDataSource(context)
        val serverRemoteDataSource = ServerInfoRemoteDataSource(context)
        val serverInfoRepository = ServerInfoRepository(
            serverLocalDataSource,
            serverRemoteDataSource
        )

        return LoginUseCase(
            userAccountRepository, serverInfoRepository,
            mainExecutor, asyncExecutor
        )
    }

    fun provideLogoutUseCase(context: Context): LogoutUseCase{
        var mUserAccountRepository: IUserAccountRepository = provideUserAccountRepository(context)

        return LogoutUseCase(mUserAccountRepository)
    }

    private fun provideUserAccountRepository(context: Context) =
        UserAccountRepository(context)
}
