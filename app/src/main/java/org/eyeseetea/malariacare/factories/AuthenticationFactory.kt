package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.LoginPresenter

object AuthenticationFactory {
    fun provideLoginPresenter(context: Context): LoginPresenter {
        val executor = WrapperExecutor()
        val getServersUseCase = ServerFactory.provideGetServersUseCase(context)

        return LoginPresenter(executor, getServersUseCase)
    }
}