package org.eyeseetea.malariacare.domain.usecase.pull

import org.eyeseetea.malariacare.domain.boundary.IPullDemoController
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppSettingsRepository

class PullDemoUseCase(
    private val pullDemoController: IPullDemoController,
    private val appSettingsRepository: IAppSettingsRepository
) {
    fun execute() {
        pullDemoController.pull()

        savePullCompleted()
    }

    private fun savePullCompleted() {
        val appSettings = appSettingsRepository.get()

        val editedAppSettings = appSettings.copy(isPullCompleted = true)

        appSettingsRepository.save(editedAppSettings)
    }
}