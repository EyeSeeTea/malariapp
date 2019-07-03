package org.eyeseetea.malariacare.domain.usecase.useraccount

import org.eyeseetea.malariacare.data.database.model.UserDB
import org.eyeseetea.malariacare.data.database.utils.Session
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository
import org.eyeseetea.malariacare.domain.entity.UserAccount

class GetCurrentUserAccountUseCase(
    private val userAccountRepository: IUserAccountRepository,
    private val getAppSettingsRepository: IAppSettingsRepository
) {
    fun execute(): UserAccount? {
        val userAccount = userAccountRepository.currentUserAccount

        if (userAccount != null) {
            // TODO: Refactor, this is necessary for the moment because load user and credentials
            // in session. When session has not callers then remove this
            loadUserInSession()
            loadCredentialsInSession()
        }

        return userAccount
    }

    private fun loadUserInSession() {
        Session.setUser(UserDB.getLoggedUser())
    }

    private fun loadCredentialsInSession() {
        val appSettings = getAppSettingsRepository.get()

        Session.setCredentials(appSettings.credentials)
    }
}
