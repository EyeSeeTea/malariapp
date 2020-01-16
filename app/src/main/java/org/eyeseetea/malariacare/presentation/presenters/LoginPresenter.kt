package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.presentation.boundary.Executor

class LoginPresenter(
    private val executor: Executor,
    private val getServersUseCase: GetServersUseCase
) {

    private var view: View? = null
    private lateinit var otherText: String
    private lateinit var server: Server

    fun attachView(view: View, otherText: String) {
        this.view = view
        this.otherText = otherText

        loadServers()
    }

    fun detachView() {
        this.view = null
    }

    fun selectServer(server: Server) {
        this.server = server

        if (server.url == otherText) {
            showManualServerUrlView()
        } else {
            hideManualServerUrlView(server.url)
        }
    }

    private fun loadServers() = executor.asyncExecute {
        showLoading()
        val servers = getServersUseCase.execute()
        hideLoading()
        showServers(servers + Server(otherText))
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

    private fun showManualServerUrlView() = executor.uiExecute {
        view?.showManualServerUrlView()
    }

    private fun hideManualServerUrlView(serverUrl: String) = executor.uiExecute {
        view?.hideManualServerUrlView(serverUrl)
    }

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showManualServerUrlView()
        fun hideManualServerUrlView(serverUrl: String)
        fun showServers(servers: List<@JvmSuppressWildcards Server>)
    }
}