package org.eyeseetea.malariacare.presentation.presenters.surveys

import org.eyeseetea.malariacare.domain.entity.*
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitLevelsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase
import org.eyeseetea.malariacare.presentation.boundary.Executor

class CreateSurveyPresenter(
    private val executor: Executor,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val getOrgUnitsUseCase: GetOrgUnitsUseCase,
    private val getOrgUnitLevelsUseCase: GetOrgUnitLevelsUseCase
) {
    private var view: View? = null


    fun attachView(
        view: View,
    ) {
        this.view = view

        load()
    }

    fun detachView() {
        this.view = null
    }

    private fun load() = executor.asyncExecute {
        try {
            val programs = getProgramsUseCase.execute()
            val orgUnits = getOrgUnitsUseCase.execute()
            val orgUnitLevels = getOrgUnitLevelsUseCase.execute()

            showData(programs, orgUnits, orgUnitLevels)
        } catch (e: java.lang.Exception) {
            showNetworkError()
        }
    }

    private fun showData(programs: List<Program>,
                         orgUnits: List< OrgUnit>,
                         orgUnitLevels:List<OrgUnitLevel>,) = executor.uiExecute {
        view?.let { view ->
            view.showData(programs,orgUnits, orgUnitLevels)
        }
    }

    private fun showNetworkError() = executor.uiExecute {
        view?.let { view ->
            view.showNetworkError()
        }
    }

    interface View {
        fun showData(
            programs: List<@JvmSuppressWildcards Program>,
            orgUnits: List<@JvmSuppressWildcards OrgUnit>,
            orgUnitLevels:List<@JvmSuppressWildcards OrgUnitLevel>,
        )

        fun showNetworkError()
    }
}
