package org.eyeseetea.malariacare.presentation.executors

import org.eyeseetea.malariacare.presentation.boundary.Executor

class WrapperExecutor : Executor {

    private val asyncExecutor = AsyncExecutor()
    private val mainExecutor = UIThreadExecutor()

    override fun uiExecute(uiFun: () -> Unit) {
        mainExecutor.run(uiFun)
    }

    override fun asyncExecute(asyncFun: () -> Unit) {
        asyncExecutor.run(asyncFun)
    }
}