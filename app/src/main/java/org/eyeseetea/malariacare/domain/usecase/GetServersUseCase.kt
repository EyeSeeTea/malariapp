package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.entity.Server

class GetServersUseCase(
    private val serverRepository: IServerRepository,
    private val mainExecutor: IMainExecutor,
    private val asyncExecutor: IAsyncExecutor
) : UseCase {
    interface Callback {
        fun onSuccess(servers: List<@JvmSuppressWildcards Server>)
    }

    private lateinit var mCallback: Callback

    fun execute(callback: Callback) {
        mCallback = callback
        asyncExecutor.run(this)
    }

    override fun run() {
        notifyComplete(serverRepository.getAll(ReadPolicy.CACHE))
        notifyComplete(serverRepository.getAll(ReadPolicy.NETWORK_FIRST))
    }

    private fun notifyComplete(servers: List<Server>) {
        mainExecutor.run { mCallback.onSuccess(servers) }
    }
}
