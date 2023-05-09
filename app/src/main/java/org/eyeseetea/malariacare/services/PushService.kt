package org.eyeseetea.malariacare.services

import android.content.Context
import androidx.work.*
import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository
import org.eyeseetea.malariacare.domain.boundary.IPushController
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor
import org.eyeseetea.malariacare.domain.usecase.PushUseCase
import org.eyeseetea.malariacare.factories.AuthenticationFactory.provideUserRepository
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor
import org.eyeseetea.malariacare.strategies.PushServiceStrategy

class PushService(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    var pushController: IPushController? = null
    var pushUseCase: PushUseCase? = null
    var mPushServiceStrategy = PushServiceStrategy()

    init {
        pushController = PushDataController(applicationContext)
        val mainExecutor: IMainExecutor = UIThreadExecutor()
        val asyncExecutor: IAsyncExecutor = AsyncExecutor()
        val serverInfoRemoteDataSource = ServerInfoRemoteDataSource(appContext)
        val serverInfoLocalDataSource = ServerInfoLocalDataSource(appContext)
        val userRepository = provideUserRepository()
        pushUseCase = PushUseCase(
            pushController, mainExecutor, asyncExecutor,
            ServerInfoRepository(serverInfoLocalDataSource, serverInfoRemoteDataSource),
            userRepository
        )
    }

    override fun doWork(): Result {
        mPushServiceStrategy.push(pushUseCase)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "PushService"

        fun buildWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<PushService>().build()
        }
    }
}