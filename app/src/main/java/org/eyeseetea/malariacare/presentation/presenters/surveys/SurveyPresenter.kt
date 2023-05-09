package org.eyeseetea.malariacare.presentation.presenters.surveys

import android.util.Log
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB
import org.eyeseetea.malariacare.data.database.model.TabDB
import org.eyeseetea.malariacare.data.database.utils.Session
import org.eyeseetea.malariacare.layout.score.ScoreRegister
import org.eyeseetea.malariacare.presentation.boundary.Executor
import org.eyeseetea.malariacare.utils.AUtils
import org.eyeseetea.malariacare.utils.Constants

open class SurveyPresenter(
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

    fun preLoadTabItems(tabID: Long, module: String) {
        val tab = TabDB.findById(tabID)
        if (tab != null) {
            AUtils.preloadTabItems(tab, module)
        }
    }

    private fun load() = executor.asyncExecute {
        //TODO: First phase to bring code from old SurveyService
        // second phase uncouple from dbflow
        try {
            Log.d("SurveyPresenter", "prepareSurveyInfo (Thread:" + Thread.currentThread().id + ")")

            //register composite scores for current survey and module
            val compositeScores = CompositeScoreDB.list()
            val survey = Session.getSurveyByModule(moduleName)

            ScoreRegister.registerCompositeScores(
                compositeScores,
                survey.id_survey.toFloat(),
                moduleName
            )

            //Get tabs for current program & register them (scores)
            val tabs = TabDB.getTabsBySession(moduleName)

            val allTabs = TabDB.getAllTabsByProgram(survey.program.id_program)

            //register tabs scores for current survey and module
            ScoreRegister.registerTabScores(tabs, survey.id_survey.toFloat(), moduleName)

            showData(compositeScores, tabs)
        } catch (e: Exception) {
            showNetworkError()

            println(
                "An error has occur retrieving the surveys: " + e.message
            )
        }
    }

    private fun showData(compositeScores: List<CompositeScoreDB>, tabs: List<TabDB>) =
        executor.uiExecute {
            view?.let { view ->
                view.showData(compositeScores,tabs )
            }
        }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    interface View {
        fun showData(compositeScores: List<@JvmSuppressWildcards  CompositeScoreDB>, tabs: List<@JvmSuppressWildcards  TabDB>)

        fun showNetworkError()
    }

    companion object {
        var defaultModuleName = Constants.FRAGMENT_SURVEY_KEY
    }
}
