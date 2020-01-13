package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.presentation.boundary.Executor

class LoginPresenter(
    private val executor: Executor,
    private val getServersUseCase: GetServersUseCase
) {

    private var view: View? = null

    fun attachView(view: View) {
        this.view = view

        loadServers()
    }

    fun detachView() {
        this.view = null
    }

    private fun loadServers() = executor.asyncExecute {
        showLoading()
        val servers = getServersUseCase.execute()
        hideLoading()
        showServers(servers)
    }

    private fun showServers(servers: List<Server>) = executor.uiExecute {
        view?.showServers(servers)
    }

    private fun showLoading() = executor.uiExecute {
        view?.showLoading()
    }

    private fun hideLoading() = executor.uiExecute {
        view?.hideLoading()
    }

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showServers(servers: List<@JvmSuppressWildcards Server>)
    }
}