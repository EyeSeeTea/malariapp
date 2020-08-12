package org.eyeseetea.malariacare.domain.usecase

import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server

class GetServerUseCase(private val serverRepository: IServerRepository) {
    fun execute(): Either<GetServerFailure, Server> {
        return serverRepository.getLoggedServer()
    }
}
