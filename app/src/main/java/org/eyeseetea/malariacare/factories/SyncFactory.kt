package org.eyeseetea.malariacare.factories

import android.content.Context
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.LocalPullController
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController
import org.eyeseetea.malariacare.domain.usecase.pull.PullDemoUseCase
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase

object SyncFactory {
    fun providePullDemoUseCase(context: Context): PullDemoUseCase {
        val localPullController = LocalPullController(context)
        val appSettingsRepository = AppSettingsFactory.provideAppSettingsRepository(context)

        return PullDemoUseCase(localPullController, appSettingsRepository)
    }

    fun providePullUseCase(): PullUseCase {
        val pullController = PullController()

        return PullUseCase(pullController)
    }
}
