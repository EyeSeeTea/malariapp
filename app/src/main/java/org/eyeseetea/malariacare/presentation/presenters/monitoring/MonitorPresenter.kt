package org.eyeseetea.malariacare.presentation.presenters.monitoring

import org.eyeseetea.malariacare.domain.entity.OrgUnit
import org.eyeseetea.malariacare.domain.entity.Program
import org.eyeseetea.malariacare.domain.entity.Survey
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSurveysUseCase
import org.eyeseetea.malariacare.domain.usecase.SurveysFilter
import org.eyeseetea.malariacare.observables.ObservablePush
import org.eyeseetea.malariacare.presentation.boundary.Executor
import java.util.*

class MonitorPresenter(
    private val executor: Executor,
    private val getSurveysByStatus: GetSurveysUseCase,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val getOrgUnitsUseCase: GetOrgUnitsUseCase,
) {
    private var view: View? = null
    private lateinit var surveyStatusFilter: SurveyStatusFilter

    private lateinit var programUid: String
    private lateinit var orgUnitUid: String

    init {
        ObservablePush.getInstance().addObserver { observable, o ->
            if (view != null) {
                load()
            }
        }
    }

    fun attachView(
        view: View,
        surveyStatusFilter: SurveyStatusFilter,
        programUid: String,
        orgUnitUid: String
    ) {
        this.view = view
        this.surveyStatusFilter = surveyStatusFilter

        this.programUid = programUid
        this.orgUnitUid = orgUnitUid

        load()
    }

    fun detachView() {
        this.view = null
    }

    fun refresh(programUid: String, orgUnitUid: String) {
        this.programUid = programUid
        this.orgUnitUid = orgUnitUid

        load()
    }

    private fun load() = executor.asyncExecute {
        try {
            val date = Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -5)
            cal[Calendar.DAY_OF_MONTH] = 1

            val surveysFilter = SurveysFilter(surveyStatusFilter, programUid, orgUnitUid, cal.time)

            val surveys = getSurveysByStatus.execute(surveysFilter)

            val programs = getProgramsUseCase.execute().associateBy { it.uid }

            val orgUnits = getOrgUnitsUseCase.execute().associateBy { it.uid }

            showSurveys(surveys,programs,orgUnits)
        } catch (e: Exception) {
            showNetworkError()

            println(
                "An error has occur retrieving the surveys: " + e.message
            )
        }
    }

    private fun showSurveys(
        surveys: List<Survey>,
        programs: Map<String, Program>,
        orgUnits: Map<String, OrgUnit>
    ) = executor.uiExecute {
        view?.let { view ->
            view.showData(surveys, programs, orgUnits)
        }
    }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    interface View {
        fun showData(
            surveys: List<@JvmSuppressWildcards Survey>,
            programs: Map<String, @JvmSuppressWildcards Program>,
            orgUnits: Map<String, @JvmSuppressWildcards OrgUnit>

        )

        fun showNetworkError()
    }
}
