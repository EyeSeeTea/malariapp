package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.Credentials
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase
import org.eyeseetea.malariacare.domain.usecase.appsettings.GetAppSettingsUseCase
import org.eyeseetea.malariacare.domain.usecase.appsettings.SaveAppSettingsUseCase
import org.eyeseetea.malariacare.domain.usecase.pull.PullDemoUseCase
import org.eyeseetea.malariacare.domain.usecase.useraccount.GetCurrentUserAccountUseCase
import org.eyeseetea.malariacare.domain.usecase.useraccount.LoginDemoUseCase

class LoginPresenter(
    private val otherText: String,
    private val loginDemoUseCase: LoginDemoUseCase,
    private val pullDemoUseCase: PullDemoUseCase,
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
            navigateToDashboard()
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

    fun loginDemo() {
        try {
            loginDemoUseCase.execute(Credentials.createDemoCredentials())
            pullDemo()
        } catch (e: Exception) {
            println(this.javaClass.simpleName + "An error has occurred realizing login demo")
        }
    }

    private fun pullDemo() {
        try {
            pullDemoUseCase.execute()
            navigateToDashboard()
        } catch (e: Exception) {
            println(this.javaClass.simpleName + "Pull error")
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

    private fun navigateToDashboard() {
        view?.navigateToDashboard()
    }

    interface View {
        fun renderServers(servers: List<@JvmSuppressWildcards Server>)
        fun showServerViews()
        fun hideServerViews()

        fun navigateToDashboard()
    }
}