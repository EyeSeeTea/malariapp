package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server

sealed class GetServerFailure {
    object ServerNotFoundFailure : GetServerFailure()
}

// TODO: this use case should not use executors, we must use sync version
// because presenters should execute use case using executors
class GetServerUseCase(
    private val serverRepository: IServerRepository,
    private val mainExecutor: IMainExecutor,
    private val asyncExecutor: IAsyncExecutor
) : UseCase {
    interface Callback {
        fun onSuccess(serverResult: Either<GetServerFailure, Server>)
    }

    private lateinit var mCallback: Callback

    fun execute(callback: Callback) {
        mCallback = callback
        asyncExecutor.run(this)
    }

    override fun run() {
        notifyComplete(serverRepository.getLoggedServer())
    }

    private fun notifyComplete(serverResult: Either<GetServerFailure, Server>) {
        mainExecutor.run { mCallback.onSuccess(serverResult) }
    }
}
