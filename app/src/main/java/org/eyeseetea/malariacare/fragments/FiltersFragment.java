package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
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
    protected abstract OrgUnitProgramFilterView.FilterType getFilterType();

    @IdRes
    protected abstract int getOrgUnitProgramFilterViewId();

    protected String getSelectedProgramUidFilter(){
        return orgUnitProgramFilterView.getSelectedProgramFilter();
    }

    protected String getSelectedOrgUnitUidFilter(){
        return orgUnitProgramFilterView.getSelectedOrgUnitFilter();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFilters();
    }

    @Override
    public void reloadData() {
        updateSelectedFilters();
    }

    private void updateSelectedFilters() {
        if (orgUnitProgramFilterView == null) {
            initializeFilters();
        }

        String selectedProgramUidFilter = PreferencesState.getInstance().getProgramUidFilter();
        String selectedOrgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();

        orgUnitProgramFilterView.changeSelectedFilters(
                selectedProgramUidFilter,
                selectedOrgUnitUidFilter);
    }

    private void initializeFilters() {
        if (orgUnitProgramFilterView == null) {
            orgUnitProgramFilterView = DashboardActivity.dashboardActivity
                    .findViewById(getOrgUnitProgramFilterViewId());

            orgUnitProgramFilterView.setFilterType(getFilterType());
        }

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(String selectedProgramFilter) {
                        saveCurrentFilters();
                        onFiltersChanged();

                    }

                    @Override
                    public void onOrgUnitFilterChanged(String selectedOrgUnitFilter) {
                        saveCurrentFilters();
                        onFiltersChanged();
                    }
                });
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(getSelectedProgramUidFilter());
        PreferencesState.getInstance().setOrgUnitUidFilter(getSelectedOrgUnitUidFilter());
    }
}
