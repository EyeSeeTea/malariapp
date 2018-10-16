/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import android.view.View;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.repositories.SettingsRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.Settings;
import org.eyeseetea.malariacare.domain.usecase.GetSettingsUseCase;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.FeedbackFragment;
import org.eyeseetea.malariacare.fragments.ObservationsFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.strategies.ActionBarStrategy;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

public class ImproveModuleController extends ModuleController {

    FeedbackFragment feedbackFragment;
    ObservationsFragment mObservationsFragment;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public ImproveModuleController(ModuleSettings moduleSettings){
        super(moduleSettings);
        this.tabLayout=R.id.tab_improve_layout;
        this.idVerticalTitle = R.id.titleCompleted;
    }


    public static String getSimpleName(){
        return ImproveModuleController.class.getSimpleName();
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment = new DashboardSentFragment();
        try {
            LinearLayout filters = (LinearLayout) dashboardActivity.findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.VISIBLE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onExitTab(){
        if(!isFragmentActive(FeedbackFragment.class) && !isFragmentActive(ObservationsFragment.class)){
            return;
        }

        closeFeedbackFragment();
    }

    public void onTabChanged(){
        if (fragment == null || !fragment.isAdded()) {
            reloadFragment();
        }
        if(isFragmentActive(FeedbackFragment.class) || isFragmentActive(ObservationsFragment.class)){
           return;
        }

        List<SurveyDB> surveys;

        if(PreferencesState.getInstance().isLastForOrgUnit()) {
            surveys = SurveyDB.getLastSentSurveysByProgramAndOrgUnit(
                    PreferencesState.getInstance().getProgramUidFilter(),
                    PreferencesState.getInstance().getOrgUnitUidFilter());

            if (surveys.size() == 1)
                onFeedbackSelected(surveys.get(0), true);
        }

        super.onTabChanged();
    }

    public void onBackPressed() {
        //List Sent surveys -> ask before leaving
        if (isFragmentActive(DashboardSentFragment.class)) {
            super.onBackPressed();
            return;
        }

        closeFeedbackFragment();
    }

    public void onFeedbackSelected(final SurveyDB survey, final boolean modifyFilter){
        Session.setSurveyByModule(survey, getSimpleName());
        try {
            LinearLayout filters = (LinearLayout) dashboardActivity.findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
        feedbackFragment = new FeedbackFragment();
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        feedbackFragment.setModuleName(getSimpleName());
        replaceFragment(R.id.dashboard_completed_container, feedbackFragment);


        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        final ISettingsRepository settingsRepository = new SettingsRepository(dashboardActivity);
        GetSettingsUseCase getSettingsUseCase = new GetSettingsUseCase(settingsRepository, mainExecutor, asyncExecutor);
        getSettingsUseCase.execute(new ISettingsRepository.ISettingsRepositoryCallback() {
            @Override
            public void onComplete(Settings settings) {
                ActionBarStrategy actionBarStrategy = new ActionBarStrategy(settings);
                actionBarStrategy.setActionBarForSurveyFeedback(dashboardActivity, survey);

                if(modifyFilter) {
                    UpdateFiltersBySurvey(survey);
                }

            }
        });
    }

    public void onPlanActionSelected(SurveyDB survey){
        Session.setSurveyByModule(survey, getSimpleName());
        try {
            LinearLayout filters = (LinearLayout) dashboardActivity.findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }

        mObservationsFragment = ObservationsFragment.newInstance(survey.getEventUid());

        replaceFragment(R.id.dashboard_completed_container, mObservationsFragment);

        UpdateFiltersBySurvey(survey);
    }

    private void UpdateFiltersBySurvey(SurveyDB survey) {
        PreferencesState.getInstance().setProgramUidFilter(
                survey.getProgram().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                survey.getOrgUnit().getUid());
    }

    private void closeFeedbackFragment() {
        android.app.Fragment fragment = dashboardActivity.getFragmentManager ().findFragmentById(R.id.dashboard_completed_container);
        if(fragment instanceof  FeedbackFragment) {
            feedbackFragment.unregisterReceiver();
            if(feedbackFragment.getView()!=null){
                feedbackFragment.getView().setVisibility(View.GONE);
            }
        }else if(fragment instanceof ObservationsFragment){
            if (feedbackFragment != null)
                replaceFragment(R.id.dashboard_completed_container, feedbackFragment);
            else
                replaceFragment(R.id.dashboard_completed_container, super.fragment);
        }

        //Reload improve fragment
        if (DashboardOrientation.VERTICAL.equals(dashboardController.getOrientation())) {
            dashboardController.reloadVertical();
        } else if (fragment instanceof FeedbackFragment) {
            reloadFragment();
        }

        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        final ISettingsRepository settingsRepository = new SettingsRepository(dashboardActivity);
        GetSettingsUseCase getSettingsUseCase = new GetSettingsUseCase(settingsRepository, mainExecutor, asyncExecutor);
        getSettingsUseCase.execute(new ISettingsRepository.ISettingsRepositoryCallback() {
            @Override
            public void onComplete(Settings settings) {
                //Update action bar title
                setActionBarDashboard(settings);
            }
        });
    }
}
