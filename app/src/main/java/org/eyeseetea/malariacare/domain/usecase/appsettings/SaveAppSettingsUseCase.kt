package org.eyeseetea.malariacare.domain.usecase.appsettings

import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.entity.AppSettings

class SaveAppSettingsUseCase(private val appSettingsRepository: IAppSettingsRepository) {

    fun execute(appSettings: AppSettings) {
        appSettingsRepository.save(appSettings)
    }
}
