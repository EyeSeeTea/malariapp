package org.eyeseetea.malariacare.views.filters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter;
import org.eyeseetea.malariacare.presentation.presenters.OrgUnitProgramFilterPresenter;
import org.eyeseetea.malariacare.views.CustomSpinner;

import java.util.List;

public class OrgUnitProgramFilterView
        extends FrameLayout implements OrgUnitProgramFilterPresenter.View {

    public enum FilterType {EXCLUSIVE,NON_EXCLUSIVE}

    public interface FilterChangedListener {
        void onProgramFilterChanged(ProgramDB programFilter);
        void onOrgUnitFilterChanged(OrgUnitDB orgUnitFilter);
    }

    private FilterChangedListener mFilterChangedListener;

    private CustomSpinner orgUnitFilterSpinner;
    private CustomSpinner programFilterSpinner;

    private OrgUnitProgramFilterPresenter presenter;


    public OrgUnitProgramFilterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    public void setFilterChangedListener(FilterChangedListener filterChangedListener) {
        mFilterChangedListener = filterChangedListener;
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(context, R.layout.view_orgunit_program_filter, this);

        initializeOrgUnitViews();
        initializeProgramViews();
        initializePresenter();
    }

    private void initializePresenter() {
        presenter = new OrgUnitProgramFilterPresenter();
        presenter.attachView(this);

    }

    private void initializeOrgUnitViews() {
        orgUnitFilterSpinner = (CustomSpinner) findViewById(R.id.spinner_orgUnit_filter);

        orgUnitFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                OrgUnitDB orgUnit = (OrgUnitDB) parent.getItemAtPosition(position);

                presenter.onOrgUnitSelected(orgUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void initializeProgramViews() {
        programFilterSpinner = (CustomSpinner) findViewById(R.id.spinner_program_filter);

        programFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                ProgramDB program = (ProgramDB) parent.getItemAtPosition(position);

                presenter.onProgramSelected(program);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void renderPrograms(List<ProgramDB> programs) {
        programFilterSpinner.setAdapter(new FilterProgramArrayAdapter(getContext(), programs));
    }

    @Override
    public void renderOrgUnits(List<OrgUnitDB> orgUnits) {
        orgUnitFilterSpinner.setAdapter(new FilterOrgUnitArrayAdapter(getContext(), orgUnits));
    }

    @Override
    public void notifyProgramFilterChange(ProgramDB programFilter) {
        if (mFilterChangedListener != null) {
            mFilterChangedListener.onProgramFilterChanged(programFilter);
        }
    }

    @Override
    public void notifyOrgUnitFilterChange(OrgUnitDB orgUnitFilter) {
        if (mFilterChangedListener != null) {
            mFilterChangedListener.onOrgUnitFilterChanged(orgUnitFilter);
        }
    }

    @Override
    public void unSelectOrgUnitFilter() {
        orgUnitFilterSpinner.setSelection(0,true,true);
    }

    @Override
    public void unSelectProgramFilter() {
        programFilterSpinner.setSelection(0,true,true);
    }

    @Override
    public void selectOrgUnitFilter(int indexToSelect) {
        orgUnitFilterSpinner.setSelection(indexToSelect,true,true);
    }

    @Override
    public void selectProgramFilter(int indexToSelect) {
        programFilterSpinner.setSelection(indexToSelect,true,true);
    }

    public ProgramDB getSelectedProgramFilter() {
        return presenter.getSelectedProgramFilter();
    }

    public OrgUnitDB getSelectedOrgUnitFilter() {
        return presenter.getSelectedOrgUnitFilter();
    }


    public void setFilterType(FilterType filterType){
        if (filterType == FilterType.EXCLUSIVE)
            presenter.setExclusiveFilter(true);
        else
            presenter.setExclusiveFilter(false);
    }

    public void changeSelectedFilters(String programUidFilter, String orgUnitUidFilter) {
        presenter.changeSelectedFilters(programUidFilter,orgUnitUidFilter);
    }
}
