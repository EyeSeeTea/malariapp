package org.eyeseetea.malariacare.views.filters

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_orgunit_program_filter.view.*
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB
import org.eyeseetea.malariacare.data.database.model.ProgramDB
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter
import org.eyeseetea.malariacare.presentation.presenters.OrgUnitProgramFilterPresenter

class OrgUnitProgramFilterView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet), OrgUnitProgramFilterPresenter.View {

    // TODO: When all callers be converted to kotlin change this interface by functions
    interface FilterChangedListener {
        fun onProgramFilterChanged(programFilter: ProgramDB?)
        fun onOrgUnitFilterChanged(orgUnitFilter: OrgUnitDB?)
    }

    enum class FilterType {
        EXCLUSIVE, NON_EXCLUSIVE
    }

    private var mFilterChangedListener: FilterChangedListener? = null

    private lateinit var presenter: OrgUnitProgramFilterPresenter

    val selectedProgramFilter: ProgramDB
        get() = presenter.selectedProgramFilter

    val selectedOrgUnitFilter: OrgUnitDB
        get() = presenter.selectedOrgUnitFilter

    init {
        init(context)
    }

    fun setFilterChangedListener(filterChangedListener: FilterChangedListener) {
        mFilterChangedListener = filterChangedListener
    }

    fun setFilterType(filterType: FilterType) {
        if (filterType == FilterType.EXCLUSIVE)
            presenter.setExclusiveFilter(true)
        else
            presenter.setExclusiveFilter(false)
    }

    fun changeSelectedFilters(programUidFilter: String, orgUnitUidFilter: String) {
        presenter.changeSelectedFilters(programUidFilter, orgUnitUidFilter)
    }

    override fun renderPrograms(programs: List<ProgramDB>) {
        spinner_program_filter.adapter = FilterProgramArrayAdapter(context, programs)
    }

    override fun renderOrgUnits(orgUnits: List<OrgUnitDB>) {
        spinner_orgUnit_filter.adapter = FilterOrgUnitArrayAdapter(context, orgUnits)
    }

    override fun notifyProgramFilterChange(programFilter: ProgramDB?) {
        mFilterChangedListener?.onProgramFilterChanged(programFilter)
    }

    override fun notifyOrgUnitFilterChange(orgUnitFilter: OrgUnitDB?) {
        mFilterChangedListener!!.onOrgUnitFilterChanged(orgUnitFilter)
    }

    override fun unSelectOrgUnitFilter() {
        spinner_orgUnit_filter.setSelection(0, true, true)
    }

    override fun unSelectProgramFilter() {
        spinner_program_filter.setSelection(0, true, true)
    }

    override fun selectOrgUnitFilter(indexToSelect: Int) {
        spinner_orgUnit_filter.setSelection(indexToSelect, true, true)
    }

    override fun selectProgramFilter(indexToSelect: Int) {
        spinner_program_filter!!.setSelection(indexToSelect, true, true)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_orgunit_program_filter, this)

        initializeOrgUnitViews()
        initializeProgramViews()
        initializePresenter()
    }

    private fun initializePresenter() {
        presenter = OrgUnitProgramFilterPresenter()

        presenter.attachView(
            this,
            context.getString(R.string.filter_all_org_units),
            context.getString(R.string.filter_all_org_assessments)
        )
    }

    private fun initializeOrgUnitViews() {
        this.spinner_orgUnit_filter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val orgUnit = parent.getItemAtPosition(position) as OrgUnitDB

                    presenter.onOrgUnitSelected(orgUnit)
                }

                override fun onNothingSelected(arg0: AdapterView<*>) {
                }
            }
    }

    private fun initializeProgramViews() {
        this.spinner_program_filter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val program = parent.getItemAtPosition(position) as ProgramDB

                    presenter.onProgramSelected(program)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {
                }
            }
    }
}
