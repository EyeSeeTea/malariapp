package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.repositories.AppSettingsRepository
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.usecase.appsettings.GetAppSettingsUseCase
import org.eyeseetea.malariacare.domain.usecase.appsettings.SaveAppSettingsUseCase

object AppSettingsFactory {
    fun provideGetAppSettingsUseCase(context: Context): GetAppSettingsUseCase {
        val appSettingsRepository = provideAppSettingsRepository(context)

        return GetAppSettingsUseCase(appSettingsRepository)
    }

    fun provideSaveAppSettingsUseCase(context: Context): SaveAppSettingsUseCase {
        val appSettingsRepository = provideAppSettingsRepository(context)

        return SaveAppSettingsUseCase(appSettingsRepository)
    }

    fun provideAppSettingsRepository(context: Context): IAppSettingsRepository {
        return AppSettingsRepository(context)
    }
}
