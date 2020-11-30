package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository
import org.eyeseetea.malariacare.data.repositories.UserD2ApiRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.LoginPresenter

object AuthenticationFactory {
    private val mainExecutor = UIThreadExecutor()
    private val asyncExecutor = AsyncExecutor()

    fun provideLoginPresenter(context: Context): LoginPresenter {
        val executor = WrapperExecutor()
        val getServersUseCase = ServerFactory.provideGetServersUseCase(context)

        return LoginPresenter(executor, getServersUseCase)
    }

    fun provideLoginUseCase(context: Context): LoginUseCase {

        val userAccountRepository = provideUserAccountRepository(context)
        val serverLocalDataSource = ServerInfoLocalDataSource(context)
        val serverRemoteDataSource = ServerInfoRemoteDataSource(context)
        val serverInfoRepository = ServerInfoRepository(
            serverLocalDataSource,
            serverRemoteDataSource
        )
        val userRepository = UserD2ApiRepository()

        val serverRepository: IServerRepository = ServerFactory.provideServerRepository(context)

        return LoginUseCase(
            userAccountRepository,
            serverRepository,
            serverInfoRepository,
            userRepository,
            mainExecutor,
            asyncExecutor
        )
    }

    fun provideLogoutUseCase(context: Context): LogoutUseCase {
        var mUserAccountRepository: IUserAccountRepository = provideUserAccountRepository(context)

        return LogoutUseCase(mUserAccountRepository)
    }

    private fun provideUserAccountRepository(context: Context) =
        UserAccountRepository(context)
}