package org.eyeseetea.malariacare.views.filters

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_orgunit_program_filter.view.*
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.factories.MetadataFactory
import org.eyeseetea.malariacare.presentation.executors.WrapperExecutor
import org.eyeseetea.malariacare.presentation.presenters.OrgUnitProgramFilterPresenter

class OrgUnitProgramFilterView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet), OrgUnitProgramFilterPresenter.View {

    // TODO: When all callers be converted to kotlin change this interface by functions
    interface FilterChangedListener {
        fun onProgramFilterChanged(programFilter: String)
        fun onOrgUnitFilterChanged(orgUnitFilter: String)
    }

    enum class FilterType {
        EXCLUSIVE, NON_EXCLUSIVE
    }

    private var mFilterChangedListener: FilterChangedListener? = null

    private lateinit var presenter: OrgUnitProgramFilterPresenter

    val selectedProgramFilter: String
        get() = presenter.selectedUidProgram

    val selectedOrgUnitFilter: String
        get() = presenter.selectedUidOrgUnit

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

    override fun showPrograms(programNames: List<String>) {
        spinner_program_filter.adapter =
            ArrayAdapter(context, R.layout.simple_spinner_item, programNames)
    }

    override fun showOrgUnits(orgUnitNames: List<String>) {
        spinner_orgUnit_filter.adapter =
            ArrayAdapter(context, R.layout.simple_spinner_item, orgUnitNames)
    }

    override fun notifyProgramFilterChange(programFilter: String) {
        mFilterChangedListener?.onProgramFilterChanged(programFilter)
    }

    override fun notifyOrgUnitFilterChange(orgUnitFilter: String) {
        mFilterChangedListener?.onOrgUnitFilterChanged(orgUnitFilter)
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
        presenter = OrgUnitProgramFilterPresenter(
            WrapperExecutor(),
            MetadataFactory.provideGetOrgUnitsUseCase(),
            MetadataFactory.provideGetProgramsUseCase()
        )

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
                    val orgUnitName = parent.getItemAtPosition(position) as String

                    presenter.onOrgUnitSelected(orgUnitName)
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
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val programName = parent.getItemAtPosition(position) as String

                    presenter.onProgramSelected(programName)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {
                }
            }
    }
}
