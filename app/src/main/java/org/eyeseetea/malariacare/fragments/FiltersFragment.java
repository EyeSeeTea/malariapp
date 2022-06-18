package org.eyeseetea.malariacare.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

public abstract class FiltersFragment extends Fragment implements IModuleFragment {

    private OrgUnitProgramFilterView orgUnitProgramFilterView;

    protected abstract void onFiltersChanged();
    protected abstract OrgUnitProgramFilterView.FilterType getFilterType();

    @IdRes
    protected abstract int getOrgUnitProgramFilterViewId();

    protected String getSelectedProgramUidFilter(){
        if (orgUnitProgramFilterView == null) return "";

        return orgUnitProgramFilterView.getSelectedProgramFilter();
    }

    protected String getSelectedOrgUnitUidFilter(){
        if (orgUnitProgramFilterView == null) return "";

        return orgUnitProgramFilterView.getSelectedOrgUnitFilter();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

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
        if (orgUnitProgramFilterView == null && DashboardActivity.dashboardActivity != null) {
            orgUnitProgramFilterView = DashboardActivity.dashboardActivity
                    .findViewById(getOrgUnitProgramFilterViewId());

            orgUnitProgramFilterView.setFilterType(getFilterType());
        }

        if (orgUnitProgramFilterView != null){
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
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(getSelectedProgramUidFilter());
        PreferencesState.getInstance().setOrgUnitUidFilter(getSelectedOrgUnitUidFilter());
    }
}
