package org.eyeseetea.malariacare.factories

import android.content.Context

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase
import org.eyeseetea.malariacare.domain.usecase.useraccount.GetCurrentUserAccountUseCase
import org.eyeseetea.malariacare.domain.usecase.useraccount.LoginDemoUseCase
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor

object UserAccountFactory {
    private val mainExecutor = UIThreadExecutor()
    private val asyncExecutor = AsyncExecutor()

    fun provideGetCurrentUserAccountUseCase(context: Context): GetCurrentUserAccountUseCase {
        val userAccountRepository = provideUserAccountRepository(context)
        val appSettingsRepository = AppSettingsFactory.provideAppSettingsRepository(context)

        return GetCurrentUserAccountUseCase(userAccountRepository, appSettingsRepository)
    }

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

    fun provideLogoutUseCase(context: Context): LogoutUseCase {
        var mUserAccountRepository: IUserAccountRepository = provideUserAccountRepository(context)

        return LogoutUseCase(mUserAccountRepository)
    }

    fun provideLoginDemoUseCase(context: Context): LoginDemoUseCase {

        val userAccountRepository = provideUserAccountRepository(context)

        return LoginDemoUseCase(userAccountRepository)
    }

    private fun provideUserAccountRepository(context: Context) =
        UserAccountRepository(context)
}
