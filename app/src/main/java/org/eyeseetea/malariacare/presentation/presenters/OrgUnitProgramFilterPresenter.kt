package org.eyeseetea.malariacare.presentation.presenters

import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB
import org.eyeseetea.malariacare.data.database.model.ProgramDB
import org.eyeseetea.malariacare.data.database.utils.PreferencesState

class OrgUnitProgramFilterPresenter {
    internal var view: View? = null

    private lateinit var programs: MutableList<ProgramDB>
    private lateinit var orgUnits: MutableList<OrgUnitDB>
    private lateinit var programDefaultOption: ProgramDB
    private lateinit var orgUnitDefaultOption: OrgUnitDB

    private var exclusiveFilter: Boolean = false

    lateinit var selectedProgramFilter: ProgramDB
        private set

    lateinit var selectedOrgUnitFilter: OrgUnitDB
        private set

    fun attachView(view: View) {
        this.view = view
        loadOrgUnits()
        loadPrograms()
    }

    private fun loadOrgUnits() {
        orgUnits = OrgUnitDB.list()

        orgUnitDefaultOption = OrgUnitDB(
            PreferencesState.getInstance().context.resources.getString(
                R.string.filter_all_org_units
            )
        )

        selectedOrgUnitFilter = orgUnitDefaultOption

        orgUnits.add(0, orgUnitDefaultOption)

        view?.renderOrgUnits(orgUnits)
    }

    private fun loadPrograms() {
        programs = ProgramDB.list()
        programDefaultOption = ProgramDB(
            PreferencesState.getInstance().context.resources.getString(
                R.string.filter_all_org_assessments
            )
        )

        selectedProgramFilter = programDefaultOption

        programs.add(0, programDefaultOption)

        view?.renderPrograms(programs)
    }

    fun onProgramSelected(program: ProgramDB) {
        if (selectedProgramFilter != program) {
            selectedProgramFilter = program

            if (exclusiveFilter) {
                unSelectOrgUnit()
            }

            view?.notifyProgramFilterChange(selectedProgramFilter)
        }
    }

    private fun unSelectOrgUnit() {
        selectedOrgUnitFilter = orgUnitDefaultOption

        view?.unSelectOrgUnitFilter()
    }

    private fun unSelectProgram() {
        selectedProgramFilter = programDefaultOption

        view?.unSelectProgramFilter()
    }

    private fun notifySelectOrgUnit() {
        val indexToSelect = orgUnits.indexOf(selectedOrgUnitFilter)

        view?.selectOrgUnitFilter(indexToSelect)
    }

    private fun notifySelectProgram() {
        val indexToSelect = programs.indexOf(selectedProgramFilter)

        view?.selectProgramFilter(indexToSelect)
    }

    fun onOrgUnitSelected(orgUnit: OrgUnitDB) {
        if (selectedOrgUnitFilter != orgUnit) {
            selectedOrgUnitFilter = orgUnit

            if (exclusiveFilter) {
                unSelectProgram()
            }

            view?.notifyOrgUnitFilterChange(selectedOrgUnitFilter)
        }
    }

    fun setExclusiveFilter(exclusiveFilter: Boolean) {
        this.exclusiveFilter = exclusiveFilter
    }

    fun changeSelectedFilters(programUidFilter: String, orgUnitUidFilter: String) {
        var programToSelect: ProgramDB? = ProgramDB.getProgram(programUidFilter)
        var orgUnitToSelect: OrgUnitDB? = OrgUnitDB.getOrgUnit(orgUnitUidFilter)

        if (programToSelect == null)
            programToSelect = programDefaultOption

        if (orgUnitToSelect == null)
            orgUnitToSelect = orgUnitDefaultOption

        onOrgUnitSelected(orgUnitToSelect)
        onProgramSelected(programToSelect)

        notifySelectOrgUnit()
        notifySelectProgram()
    }

    interface View {
        fun renderPrograms(programs: List<@JvmSuppressWildcards ProgramDB>)
        fun renderOrgUnits(orgUnits: List<@JvmSuppressWildcards OrgUnitDB>)
        fun notifyProgramFilterChange(programFilter: ProgramDB?)
        fun notifyOrgUnitFilterChange(orgUnitFilter: OrgUnitDB?)
        fun unSelectOrgUnitFilter()
        fun unSelectProgramFilter()

        fun selectOrgUnitFilter(indexToSelect: Int)
        fun selectProgramFilter(indexToSelect: Int)
    }
}
