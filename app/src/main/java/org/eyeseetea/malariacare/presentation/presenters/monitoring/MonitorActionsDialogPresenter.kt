package org.eyeseetea.malariacare.presentation.presenters.monitoring

import org.eyeseetea.malariacare.domain.entity.OrgUnit
import org.eyeseetea.malariacare.domain.entity.Program
import org.eyeseetea.malariacare.domain.entity.ServerMetadata
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramByUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase
import org.eyeseetea.malariacare.domain.usecase.GetSurveyByUidUseCase
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitByUidUseCase
import org.eyeseetea.malariacare.presentation.boundary.Executor
import org.eyeseetea.malariacare.presentation.mapper.observations.ObservationMapper
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel
import java.lang.Exception

class MonitorActionsDialogPresenter(
    private val executor: Executor,
    private val getProgramByUidUseCase: GetProgramByUidUseCase,
    private val getOrgUnitByUidUseCase: GetOrgUnitByUidUseCase,
    private val getServerMetadataUseCase: GetServerMetadataUseCase,
    private val getSurveyByUidUseCase: GetSurveyByUidUseCase,
    private val getObservationBySurveyUidUseCase: GetObservationBySurveyUidUseCase
) {
    private var view: View? = null
    private lateinit var surveyUid: String

    private lateinit var program: Program
    private lateinit var orgUnit: OrgUnit
    private lateinit var serverMetadata: ServerMetadata

    fun attachView(view: View, surveyUid: String) {

        this.view = view
        this.surveyUid = surveyUid

        loadAll()
    }

    fun detachView() {
        view = null
    }

    private fun loadAll() = executor.asyncExecute {
        showLoading()
        loadMetadata()
        loadData()
        hideLoading()
    }

    private fun loadMetadata() {
        try {
            val survey = getSurveyByUidUseCase.execute(surveyUid)

            program = getProgramByUidUseCase.execute(survey.programUId)
            orgUnit = getOrgUnitByUidUseCase.execute(survey.orgUnitUId)
            serverMetadata = getServerMetadataUseCase.execute()
        } catch (e: Exception) {
            showLoadingErrorMessage()
        }
    }

    private fun loadData() {
        try {
            val observation = getObservationBySurveyUidUseCase.execute(surveyUid)

            val observationViewModel = ObservationMapper.mapToViewModel(observation, serverMetadata)

            showOrgUnitAndProgram()
            showActions(
                observationViewModel.action1,
                observationViewModel.action2,
                observationViewModel.action3
            )
        } catch (e: Exception) {
            view?.showLoadErrorMessage()
        }
    }

    private fun showLoading() = executor.uiExecute {
        view?.showLoading()
    }

    private fun hideLoading() = executor.uiExecute {
        view?.hideLoading()
    }

    private fun showOrgUnitAndProgram() = executor.uiExecute {
        view?.showOrgUnitAndProgram(orgUnit.name, program.name)
    }

    private fun showActions(
        action1: ActionViewModel,
        action2: ActionViewModel,
        action3: ActionViewModel
    ) = executor.uiExecute {
        view?.showActions(action1, action2, action3)
    }

    private fun showLoadingErrorMessage() = executor.uiExecute {
        view?.showLoadErrorMessage()
    }

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showLoadErrorMessage()

        fun showOrgUnitAndProgram(orgUnit: String, program: String)

        fun showActions(
            action1: ActionViewModel,
            action2: ActionViewModel,
            action3: ActionViewModel
        )
    }
}