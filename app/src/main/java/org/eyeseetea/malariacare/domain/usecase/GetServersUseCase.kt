package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.ReadPolicy
import org.eyeseetea.malariacare.domain.entity.Server

class GetServersUseCase(private val serverRepository: IServerRepository) {
    fun execute(): List<Server> {
        return serverRepository.getAll(ReadPolicy.NETWORK_FIRST)
    }
}
