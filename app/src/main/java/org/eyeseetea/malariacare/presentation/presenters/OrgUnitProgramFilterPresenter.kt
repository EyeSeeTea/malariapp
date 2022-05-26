package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.domain.entity.OrgUnit
import org.eyeseetea.malariacare.domain.entity.Program
import org.eyeseetea.malariacare.domain.usecase.GetOrgUnitsUseCase
import org.eyeseetea.malariacare.domain.usecase.GetProgramsUseCase
import org.eyeseetea.malariacare.presentation.boundary.Executor

class OrgUnitProgramFilterPresenter(
    private val executor: Executor,
    private val getOrgUnitsUseCase: GetOrgUnitsUseCase,
    private val getProgramsUseCase: GetProgramsUseCase
) {
    internal var view: View? = null

    private var programs: List<Program> = listOf()
    private var orgUnits: List<OrgUnit> = listOf()

    private var programsNames: MutableList<String> = mutableListOf()
    private var orgUnitsNames: MutableList<String> = mutableListOf()

    private var exclusiveFilter: Boolean = false

    private lateinit var allOrgUnitText: String
    private lateinit var allProgramsText: String

    private lateinit var selectedProgramName: String
    private lateinit var selectedOrgUnitName: String

    lateinit var selectedUidProgram: String
    lateinit var selectedUidOrgUnit: String

    fun attachView(view: View, allOrgUnitText: String, allProgramsText: String) {
        this.view = view

        this.allOrgUnitText = allOrgUnitText
        this.allProgramsText = allProgramsText

        loadMetadata()
    }

    fun onProgramSelected(programName: String) {
        if (selectedProgramName != programName) {
            changeSelectedProgram(programName)

            if (exclusiveFilter && programName != allProgramsText) {
                unSelectOrgUnit()
            }

            notifyProgramFilterChange()
        }
    }

    fun onOrgUnitSelected(orgUnitName: String) {
        if (selectedOrgUnitName != orgUnitName) {
            changeSelectedOrgUnit(orgUnitName)

            if (exclusiveFilter && orgUnitName != allOrgUnitText) {
                unSelectProgram()
            }

            notifyOrgUnitFilterChange()
        }
    }

    fun setExclusiveFilter(exclusiveFilter: Boolean) {
        this.exclusiveFilter = exclusiveFilter
    }

    fun changeSelectedFilters(programUidFilter: String, orgUnitUidFilter: String) {
        val programToSelect =
            programs.firstOrNull { program -> program.uid == programUidFilter }

        val orgUnitToSelect =
            orgUnits.firstOrNull { orgUnit -> orgUnit.uid == orgUnitUidFilter }

        val programNameToSelect = programToSelect?.name ?: allProgramsText
        val orgUnitNameToSelect = orgUnitToSelect?.name ?: allOrgUnitText

        onProgramSelected(programNameToSelect)

        if (exclusiveFilter && programNameToSelect != allProgramsText) {
            unSelectOrgUnit()
            notifyOrgUnitFilterChange()
        } else {
            onOrgUnitSelected(orgUnitNameToSelect)
        }

        notifySelectOrgUnit()
        notifySelectProgram()
    }

    private fun loadMetadata() = executor.asyncExecute {
        loadOrgUnits()
        loadPrograms()
    }

    private fun loadOrgUnits() {
        orgUnits = getOrgUnitsUseCase.execute()
        orgUnitsNames = orgUnits.map { orgUnit -> orgUnit.name } as MutableList<String>

        changeSelectedOrgUnit(allOrgUnitText)

        orgUnitsNames.add(0, selectedOrgUnitName)

        showOrgUnits()
    }

    private fun loadPrograms() {
        programs = getProgramsUseCase.execute()
        programsNames = programs.map { program -> program.name } as MutableList<String>

        changeSelectedProgram(allProgramsText)

        programsNames.add(0, selectedProgramName)

        showPrograms()
    }

    private fun changeSelectedProgram(programName: String) {
        selectedProgramName = programName

        if (selectedProgramName == allProgramsText) {
            selectedUidProgram = ""
        } else {
            selectedUidProgram =
                programs.first { program -> program.name == selectedProgramName }.uid
        }
    }

    private fun changeSelectedOrgUnit(orgUnitName: String) {
        selectedOrgUnitName = orgUnitName

        if (selectedOrgUnitName == allOrgUnitText) {
            selectedUidOrgUnit = ""
        } else {
            selectedUidOrgUnit =
                orgUnits.first { orgUnit -> orgUnit.name == selectedOrgUnitName }.uid
        }
    }

    private fun showOrgUnits() = executor.uiExecute {
        view?.showOrgUnits(orgUnitsNames as List<String>)
    }

    private fun showPrograms() = executor.uiExecute {
        view?.showPrograms(programsNames as List<String>)
    }

    private fun notifyProgramFilterChange() = executor.uiExecute {
        view?.notifyProgramFilterChange(selectedUidProgram)
    }

    private fun notifyOrgUnitFilterChange() = executor.uiExecute {
        view?.notifyOrgUnitFilterChange(selectedUidOrgUnit)
    }

    private fun unSelectOrgUnit() = executor.uiExecute {
        changeSelectedOrgUnit(allOrgUnitText)

        view?.unSelectOrgUnitFilter()
    }

    private fun unSelectProgram() = executor.uiExecute {
        changeSelectedProgram(allProgramsText)

        view?.unSelectProgramFilter()
    }

    private fun notifySelectOrgUnit() = executor.uiExecute {
        val indexToSelect = orgUnitsNames.indexOf(selectedOrgUnitName)

        view?.selectOrgUnitFilter(indexToSelect)
    }

    private fun notifySelectProgram() = executor.uiExecute {
        val indexToSelect = programsNames.indexOf(selectedProgramName)

        view?.selectProgramFilter(indexToSelect)
    }

    interface View {
        fun showPrograms(programs: List<@JvmSuppressWildcards String>)
        fun showOrgUnits(orgUnits: List<@JvmSuppressWildcards String>)
        fun notifyProgramFilterChange(programFilter: String)
        fun notifyOrgUnitFilterChange(orgUnitFilter: String)
        fun unSelectOrgUnitFilter()
        fun unSelectProgramFilter()

        fun selectOrgUnitFilter(indexToSelect: Int)
        fun selectProgramFilter(indexToSelect: Int)
    }
}
