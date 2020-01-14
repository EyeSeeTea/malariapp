package org.eyeseetea.malariacare.presentation.boundary

interface Executor {
    fun uiExecute(uiFun: () -> Unit)
    fun asyncExecute(asyncFun: () -> Unit)
}