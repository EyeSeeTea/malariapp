package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

public abstract class FiltersFragment extends Fragment implements IModuleFragment {

    private OrgUnitProgramFilterView orgUnitProgramFilterView;

    protected abstract void onFiltersChanged();

    protected String selectedProgramUidFilter;
    protected String selectedOrgUnitUidFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        initializeFilters();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void reloadData() {
        initializeFilters();
        updateSelectedFilters();
    }

    private void updateSelectedFilters() {
        if (orgUnitProgramFilterView == null) {
            initializeFilters();
        }
        selectedProgramUidFilter = PreferencesState.getInstance().getProgramUidFilter();
        selectedOrgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();

        orgUnitProgramFilterView.changeSelectedFilters(
                selectedProgramUidFilter,
                selectedOrgUnitUidFilter);
    }

    private void initializeFilters() {
        if (orgUnitProgramFilterView == null){
            orgUnitProgramFilterView = DashboardActivity.dashboardActivity
                    .findViewById(R.id.monitor_org_unit_program_filter_view);

            orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE);
        }


        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(String selectedProgramFilter) {
                        if ((selectedProgramUidFilter == null && selectedProgramFilter!= null) ||
                            !selectedProgramUidFilter.equals(selectedProgramFilter)){

                            selectedProgramUidFilter = selectedProgramFilter;

                            saveCurrentFilters();
                            onFiltersChanged();
                        }
                    }

                    @Override
                    public void onOrgUnitFilterChanged(String selectedOrgUnitFilter) {
                        if ((selectedOrgUnitUidFilter == null && selectedOrgUnitFilter!= null) ||
                                !selectedOrgUnitUidFilter.equals(selectedOrgUnitFilter)) {
                            selectedOrgUnitUidFilter = selectedOrgUnitFilter;
                            saveCurrentFilters();
                            onFiltersChanged();
                        }
                    }
                });
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(selectedProgramUidFilter);
        PreferencesState.getInstance().setOrgUnitUidFilter(selectedOrgUnitUidFilter);
    }
}
