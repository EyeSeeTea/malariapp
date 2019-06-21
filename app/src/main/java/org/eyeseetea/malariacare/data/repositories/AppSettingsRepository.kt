package org.eyeseetea.malariacare.data.repositories

import android.content.Context
import android.preference.PreferenceManager
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.entity.AppSettings

class AppSettingsRepository(private val context: Context) : IAppSettingsRepository {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun get(): AppSettings {
        val isEulaAccepted = getBooleanPreference()

        return AppSettings(isEulaAccepted)
    }

    override fun save(appSettings: AppSettings) {
        val editor = sharedPreferences.edit()

        editor.putBoolean(context.getString(R.string.eula_accepted), appSettings.isEulaAccepted)

        editor.apply()
    }

    private fun getBooleanPreference(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.eula_accepted), false)
    }
}