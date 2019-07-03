package org.eyeseetea.malariacare.data.repositories

import android.content.Context
import android.preference.PreferenceManager
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository
import org.eyeseetea.malariacare.domain.entity.AppSettings
import org.eyeseetea.malariacare.domain.entity.Credentials

class AppSettingsRepository(private val context: Context) : IAppSettingsRepository {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun get(): AppSettings {
        val isEulaAccepted = getBooleanPreference(R.string.eula_accepted)
        val isPullCompleted = getBooleanPreference(R.string.pull_metadata)
        val credentials = getCredentials()

        return AppSettings(isEulaAccepted, isPullCompleted, credentials)
    }

    override fun save(appSettings: AppSettings) {
        with(sharedPreferences.edit()) {
            putBoolean(context.getString(R.string.eula_accepted), appSettings.isEulaAccepted)
            putBoolean(context.getString(R.string.pull_metadata), appSettings.isPullCompleted)

            putString(context.getString(R.string.dhis_url), appSettings.credentials?.serverURL)
            putString(context.getString(R.string.dhis_user), appSettings.credentials?.username)
            putString(context.getString(R.string.dhis_password), appSettings.credentials?.password)

            apply()
        }
    }

    private fun getCredentials(): Credentials? {
        var credentials: Credentials? = null

        val serverURL = sharedPreferences.getString(context.getString(R.string.dhis_url), "")
        val username = sharedPreferences.getString(context.getString(R.string.dhis_user), "")
        val password = sharedPreferences.getString(context.getString(R.string.dhis_password), "")

        if (serverURL.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
            credentials = Credentials(serverURL, username, password)
        }

        return credentials
    }

    private fun getBooleanPreference(stringKey: Int): Boolean {
        return sharedPreferences.getBoolean(context.getString(stringKey), false)
    }
}