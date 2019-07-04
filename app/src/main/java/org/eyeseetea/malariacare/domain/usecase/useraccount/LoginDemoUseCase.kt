package org.eyeseetea.malariacare.domain.usecase.useraccount

import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.entity.Credentials

class LoginDemoUseCase(private val userAccountRepository: IUserAccountRepository) {
    fun execute(credentials: Credentials) {
        userAccountRepository.login(credentials)
    }
}