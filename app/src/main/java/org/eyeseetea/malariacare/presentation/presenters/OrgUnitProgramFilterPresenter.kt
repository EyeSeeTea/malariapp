package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB
import org.eyeseetea.malariacare.data.database.model.ProgramDB

class OrgUnitProgramFilterPresenter {
    internal var view: View? = null

    private lateinit var programs: List<ProgramDB>
    private lateinit var orgUnits: List<OrgUnitDB>

    private lateinit var programsNames: MutableList<String>
    private lateinit var orgUnitsNames: MutableList<String>

    private var exclusiveFilter: Boolean = false

    private lateinit var allOrgUnitText: String
    private lateinit var allProgramsText: String

    private lateinit var selectedProgramName: String
    private lateinit var selectedOrgUnitName: String

    lateinit var selectedProgram: String
    lateinit var selectedOrgUnit: String

    fun attachView(view: View, allOrgUnitText: String, allProgramsText: String) {
        this.view = view

        this.allOrgUnitText = allOrgUnitText
        this.allProgramsText = allProgramsText

        loadOrgUnits()
        loadPrograms()
    }

    private fun loadOrgUnits() {
        orgUnits = OrgUnitDB.list()
        orgUnitsNames = orgUnits.map { orgUnit -> orgUnit.name } as MutableList<String>

        changeSelectedOrgUnit(allOrgUnitText)

        orgUnitsNames.add(0, selectedOrgUnitName)

        view?.renderOrgUnits(orgUnitsNames as List<String>)
    }

    private fun loadPrograms() {
        programs = ProgramDB.list()
        programsNames = programs.map { program -> program.name } as MutableList<String>

        changeSelectedProgram(allProgramsText)

        programsNames.add(0, selectedProgramName)

        view?.renderPrograms(programsNames as List<String>)
    }

    fun onProgramSelected(programName: String) {
        if (selectedProgramName != programName) {
            changeSelectedProgram(programName)

            if (exclusiveFilter) {
                unSelectOrgUnit()
            }

            view?.notifyProgramFilterChange(selectedProgram)
        }
    }

    private fun changeSelectedProgram(programName: String) {
        selectedProgramName = programName

        if (selectedProgramName == allProgramsText) {
            selectedProgram = ""
        } else {
            selectedProgram = programs.first { program -> program.name == selectedProgramName }.uid
        }
    }

    private fun changeSelectedOrgUnit(orgUnitName: String) {
        selectedOrgUnitName = orgUnitName

        if (selectedOrgUnitName == allOrgUnitText) {
            selectedOrgUnit = ""
        } else {
            selectedOrgUnit = orgUnits.first { orgUnit -> orgUnit.name == selectedOrgUnitName }.uid
        }
    }

    private fun unSelectOrgUnit() {
        changeSelectedOrgUnit(allOrgUnitText)

        view?.unSelectOrgUnitFilter()
    }

    private fun unSelectProgram() {
        changeSelectedProgram(allProgramsText)

        view?.unSelectProgramFilter()
    }

    private fun notifySelectOrgUnit() {
        val indexToSelect = orgUnitsNames.indexOf(selectedOrgUnitName)

        view?.selectOrgUnitFilter(indexToSelect)
    }

    private fun notifySelectProgram() {
        val indexToSelect = programsNames.indexOf(selectedProgramName)

        view?.selectProgramFilter(indexToSelect)
    }

    fun onOrgUnitSelected(orgUnitName: String) {
        if (selectedOrgUnitName != orgUnitName) {
            changeSelectedOrgUnit(orgUnitName)

            if (exclusiveFilter) {
                unSelectProgram()
            }

            view?.notifyOrgUnitFilterChange(selectedOrgUnit)
        }
    }

    fun setExclusiveFilter(exclusiveFilter: Boolean) {
        this.exclusiveFilter = exclusiveFilter
    }

    fun changeSelectedFilters(programUidFilter: String, orgUnitUidFilter: String) {
        var programNameToSelect = allProgramsText
        var orgUnitNameToSelect = allOrgUnitText

        if (programUidFilter.isNotBlank()) {
            programNameToSelect =
                programs.first { program -> program.uid == programUidFilter }.name
        }

        if (orgUnitUidFilter.isNotBlank()) {
            orgUnitNameToSelect =
                orgUnits.first { orgUnit -> orgUnit.uid == orgUnitUidFilter }.name
        }

        onOrgUnitSelected(orgUnitNameToSelect)
        onProgramSelected(programNameToSelect)

        notifySelectOrgUnit()
        notifySelectProgram()
    }

    interface View {
        fun renderPrograms(programs: List<@JvmSuppressWildcards String>)
        fun renderOrgUnits(orgUnits: List<@JvmSuppressWildcards String>)
        fun notifyProgramFilterChange(programFilter: String)
        fun notifyOrgUnitFilterChange(orgUnitFilter: String)
        fun unSelectOrgUnitFilter()
        fun unSelectProgramFilter()

        fun selectOrgUnitFilter(indexToSelect: Int)
        fun selectProgramFilter(indexToSelect: Int)
    }
}
