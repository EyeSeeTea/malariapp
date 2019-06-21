package org.eyeseetea.malariacare.factories

import android.content.Context

import org.eyeseetea.malariacare.data.repositories.ServerRepository
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor

object ServerFactory {
    fun provideGetServersUseCase(context: Context): GetServersUseCase {
        val mainExecutor = UIThreadExecutor()
        val asyncExecutor = AsyncExecutor()

        val serverRepository = ServerRepository(context)

        return GetServersUseCase(serverRepository, mainExecutor, asyncExecutor)
    }
}
