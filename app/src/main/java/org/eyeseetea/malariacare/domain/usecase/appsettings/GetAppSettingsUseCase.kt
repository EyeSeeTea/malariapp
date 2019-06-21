package org.eyeseetea.malariacare.domain.usecase.appsettings

import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.entity.AppSettings

class GetAppSettingsUseCase(private val appSettingsRepository: IAppSettingsRepository) {
    fun execute(): AppSettings {
        return appSettingsRepository.get()
    }
}
