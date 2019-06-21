package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase

class LoginPresenter(
    private val otherText: String,
    private val getServersUseCase: GetServersUseCase
) {
    private var view: View? = null

    private lateinit var selectedServer: Server

    fun attachView(view: View) {

        this.view = view

        loadServers()
    }

    fun detachView() {
        view = null
    }

    fun selectServer(server: Server) {
        if (server.url != selectedServer.url) {
            selectedServer = server

            if (server.url == otherText) {
                showServerViews()
            } else {
                hideServerViews()
            }
        }
    }

    private fun loadServers() {
        getServersUseCase.execute { servers ->
            selectedServer = servers.first()
            renderServers(servers)
        }
    }

    private fun renderServers(servers: List<Server>) {
        view?.renderServers(servers)
    }

    private fun showServerViews() {
        view?.showServerViews()
    }

    private fun hideServerViews() {
        view?.hideServerViews()
    }

    interface View {
        fun renderServers(servers: List<@JvmSuppressWildcards Server>)
        fun showServerViews()
        fun hideServerViews()
    }
}