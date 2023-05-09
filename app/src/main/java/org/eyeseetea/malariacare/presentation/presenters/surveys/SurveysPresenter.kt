package org.eyeseetea.malariacare.presentation.presenters.surveys

import org.eyeseetea.malariacare.domain.entity.*
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSurveysUseCase
import org.eyeseetea.malariacare.domain.usecase.SurveysFilter
import org.eyeseetea.malariacare.observables.ObservablePush
import org.eyeseetea.malariacare.presentation.boundary.Executor
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel

open class SurveysPresenter(
    private val executor: Executor,
    private val getSurveysByStatus: GetSurveysUseCase,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val getOrgUnitsUseCase: GetOrgUnitsUseCase,
) {
    private var view: View? = null
    private lateinit var surveyStatusFilter: SurveyStatusFilter
    private lateinit var programsMap: Map<String, Program>
    private lateinit var orgUnitsMap: Map<String, OrgUnit>

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

        loadMetadata()
        load()
    }

    fun detachView() {
        this.view = null
    }

    private fun loadMetadata() = executor.asyncExecute {
        try {
            loadPrograms()
            loadOrgUnits()
        } catch (e: java.lang.Exception) {
            showNetworkError()
        }
    }

    fun refresh(programUid: String, orgUnitUid: String) {
        this.programUid = programUid
        this.orgUnitUid = orgUnitUid

        load()
    }

    private fun load() = executor.asyncExecute {
        try {
            val surveysFilter = SurveysFilter(surveyStatusFilter, programUid, orgUnitUid)

            val surveys = getSurveysByStatus.execute(surveysFilter)

            val surveyViewModels = surveys.map { mapToViewModel(it) }

            showSurveys(surveyViewModels)
        } catch (e: Exception) {
            showNetworkError()

            println(
                "An error has occur retrieving the surveys: " + e.message
            )
        }
    }

    private fun showSurveys(surveys: List<SurveyViewModel>) = executor.uiExecute {
        view?.let { view ->
            view.showSurveys(surveys)
        }
    }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    private fun loadPrograms() {
        val programs = getProgramsUseCase.execute()

        programsMap = programs.associateBy { it.uid }
    }

    private fun loadOrgUnits() {
        val orgUnits = getOrgUnitsUseCase.execute()

        orgUnitsMap = orgUnits.associateBy { it.uid }
    }

    private fun mapToViewModel(survey: Survey): SurveyViewModel {
        val program = programsMap[survey.programUId]
        val orgUnit = orgUnitsMap[survey.orgUnitUId]

        return SurveyViewModel(
            survey.surveyUid,
            program?.name,
            orgUnit?.name,
            survey.completionDate,
            survey.competency,
            survey.score,
            survey.status == SurveyStatus.COMPLETED,
            survey.hasConflict()
        )
    }

    interface View {
        fun showSurveys(
            surveys: List<@JvmSuppressWildcards SurveyViewModel>
        )

        fun showNetworkError()
    }
}
