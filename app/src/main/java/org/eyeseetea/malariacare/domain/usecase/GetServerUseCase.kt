package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.entity.Server

class GetServerUseCase(
    private val serverRepository: IServerRepository,
    private val mainExecutor: IMainExecutor,
    private val asyncExecutor: IAsyncExecutor
) : UseCase {
    interface Callback {
        fun onSuccess(server: Server)
    }

    private lateinit var mCallback: Callback

    fun execute(callback: Callback) {
        mCallback = callback
        asyncExecutor.run(this)
    }

    override fun run() {
        notifyComplete(serverRepository.getLoggedServer)
    }

    private fun notifyComplete(server: Server) {
        mainExecutor.run { mCallback.onSuccess(server) }
    }
}
