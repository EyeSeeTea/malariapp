package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.domain.usecase.appsettings.GetAppSettingsUseCase
import org.eyeseetea.malariacare.domain.usecase.appsettings.SaveAppSettingsUseCase
import org.eyeseetea.malariacare.domain.usecase.useraccount.GetCurrentUserAccountUseCase

class LoginPresenter(
    private val otherText: String,
    private val getServersUseCase: GetServersUseCase,
    private val getCurrentUserAccountUseCase: GetCurrentUserAccountUseCase,
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val saveAppSettingsUseCase: SaveAppSettingsUseCase
) {
    private var view: View? = null

    private lateinit var selectedServer: Server

    fun attachView(view: View) {

        this.view = view

        if (existsLoginAndPull()) {
            view.navigateToDashboard()
        } else {
            loadServers()
        }
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

    private fun existsLoginAndPull(): Boolean {
        val userAccount = getCurrentUserAccountUseCase.execute()
        val appSettings = getAppSettingsUseCase.execute()

        return userAccount != null && appSettings.isPullCompleted
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

        fun navigateToDashboard()
    }
}