package org.eyeseetea.malariacare.domain.boundary.repositories

import org.eyeseetea.malariacare.domain.entity.AppSettings

interface IAppSettingsRepository {
    fun get(): AppSettings
    fun save(appSettings: AppSettings)
}
