package org.eyeseetea.malariacare.presentation.presenters.surveys

import org.eyeseetea.malariacare.data.database.utils.Session
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback
import org.eyeseetea.malariacare.data.database.utils.feedback.FeedbackBuilder
import org.eyeseetea.malariacare.presentation.boundary.Executor
import org.eyeseetea.malariacare.utils.Constants

open class FeedbackPresenter(
    private val executor: Executor,
) {
    private var view: View? = null
    private var moduleName: String = defaultModuleName

    fun attachView(
        view: View,
        moduleName: String
    ) {
        this.view = view
        this.moduleName = moduleName

        load()
    }

    fun detachView() {
        this.view = null
    }

    fun reload() {
        load()
    }

    private fun load() = executor.asyncExecute {
        //TODO: First phase to bring code from old SurveyService
        // second phase uncouple from session,dbflow
        try {
            val feedbackList = FeedbackBuilder.build(Session.getSurveyByModule(moduleName), moduleName)

            showData(feedbackList)
        } catch (e: Exception) {
            showNetworkError()

            println(
                "An error has occur retrieving the surveys: " + e.message
            )
        }
    }

    private fun showData(feedbackList: MutableList<Feedback>) =
        executor.uiExecute {
            view?.let { view ->
                view.showData(feedbackList)
            }
        }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    interface View {
        fun showData(feedbackList: List<@JvmSuppressWildcards  Feedback>)

        fun showNetworkError()
    }

    companion object {
        var defaultModuleName = Constants.FRAGMENT_SURVEY_KEY
    }
}
