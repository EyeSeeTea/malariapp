package org.eyeseetea.malariacare.presentation.presenters.surveys

import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItemBuilder
import org.eyeseetea.malariacare.presentation.boundary.Executor

open class PlannedSurveysPresenter(
    private val executor: Executor,
) {
    private var view: View? = null

    fun attachView(view: View) {
        this.view = view

        load()
    }

    fun detachView() {
        this.view = null
    }

    fun reload() {
        load()
    }

    private fun load() = executor.asyncExecute {
        //TODO: First phase to bring code from old PlannedSurveyService
        // second phase uncouple from session,dbflow
        try {
            val plannedItems = PlannedItemBuilder().buildPlannedItems()

            showData(plannedItems)
        } catch (e: Exception) {
            showNetworkError()

            println(
                "An error has occur retrieving planned surveys: " + e.message
            )
        }
    }

    private fun showData(plannedItems: MutableList<PlannedItem>) =
        executor.uiExecute {
            view?.let { view ->
                view.showData(plannedItems)
            }
        }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    interface View {
        fun showData(plannedItems: List<@JvmSuppressWildcards  PlannedItem>)

        fun showNetworkError()
    }
}
