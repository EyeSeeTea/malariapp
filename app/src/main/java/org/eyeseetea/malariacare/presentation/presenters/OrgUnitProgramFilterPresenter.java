package org.eyeseetea.malariacare.presentation.presenters;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import java.util.List;

public class OrgUnitProgramFilterPresenter {
    View view;

    private List<ProgramDB> programs;
    private List<OrgUnitDB> orgUnits;
    private ProgramDB mProgramDefaultOption;
    private OrgUnitDB mOrgUnitDefaultOption;
    private ProgramDB mSelectedProgramFilter;
    private OrgUnitDB mSelectedOrgUnitFilter;
    private boolean mExclusiveFilter;

    public void attachView(View view){
        this.view = view;
        loadOrgUnits();
        loadPrograms();
    }

    private void loadOrgUnits(){
        orgUnits = OrgUnitDB.list();

        mOrgUnitDefaultOption = new OrgUnitDB(PreferencesState.getInstance().getContext().getResources().getString(
                R.string.filter_all_org_units));

        mSelectedOrgUnitFilter = mOrgUnitDefaultOption;

        orgUnits.add(0, mOrgUnitDefaultOption);

        if (view != null){
            view.renderOrgUnits(orgUnits);
        }
    }

    private void loadPrograms(){
        programs = ProgramDB.list();
        mProgramDefaultOption = new ProgramDB(PreferencesState.getInstance().getContext().getResources().getString(
                R.string.filter_all_org_assessments));

        mSelectedProgramFilter = mProgramDefaultOption;

        programs.add(0, mProgramDefaultOption);

        if (view != null){
            view.renderPrograms(programs);
        }
    }

    public void onProgramSelected(ProgramDB program) {
        if (!mSelectedProgramFilter.equals(program)) {
            mSelectedProgramFilter = program;

            if (mExclusiveFilter)
                unSelectOrgUnit();

            if (view != null) {
                view.notifyProgramFilterChange(mSelectedProgramFilter);
            }
        }
    }

    private void unSelectOrgUnit() {
        mSelectedOrgUnitFilter = mOrgUnitDefaultOption;

        if (view != null) {
            view.unSelectOrgUnitFilter();
        }
    }

    private void unSelectProgram() {
        mSelectedProgramFilter = mProgramDefaultOption;

        if (view != null) {
            view.unSelectProgramFilter();
        }
    }

    public void onOrgUnitSelected(OrgUnitDB orgUnit) {
        if (!mSelectedOrgUnitFilter.equals(orgUnit)) {
            mSelectedOrgUnitFilter = orgUnit;

            if (mExclusiveFilter)
                unSelectProgram();

            if (view != null) {
                view.notifyOrgUnitFilterChange(mSelectedOrgUnitFilter);

            }
        }
    }

    public ProgramDB getSelectedProgramFilter() {
        return mSelectedProgramFilter;
    }

    public OrgUnitDB getSelectedOrgUnitFilter() {
        return mSelectedOrgUnitFilter;
    }

    public void setExclusiveFilter(boolean exclusiveFilter) {
        mExclusiveFilter = exclusiveFilter;
    }

    public interface View{
        void renderPrograms(List<ProgramDB> programs);
        void renderOrgUnits(List<OrgUnitDB> orgUnits);
        void notifyProgramFilterChange(ProgramDB programFilter);
        void notifyOrgUnitFilterChange(OrgUnitDB orgUnitFilter);
        void unSelectOrgUnitFilter();
        void unSelectProgramFilter();
    }
}
