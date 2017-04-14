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
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.FeedbackFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

/**
 * Created by idelcano on 25/02/2016.
 */
public class ImproveModuleController extends ModuleController {

    FeedbackFragment feedbackFragment;

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
        if(!isFragmentActive(FeedbackFragment.class)){
            return;
        }

        closeFeedbackFragment();
    }

    public void onTabChanged(){
        if(isFragmentActive(FeedbackFragment.class)){
           return;
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

    public void onFeedbackSelected(Survey survey){
        Session.setSurveyByModule(survey, getSimpleName());
        try {
            LinearLayout filters = (LinearLayout) dashboardActivity.findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
        feedbackFragment = FeedbackFragment.newInstance(1);
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        feedbackFragment.setModuleName(getSimpleName());
        replaceFragment(R.id.dashboard_completed_container, feedbackFragment);
        LayoutUtils.setActionBarTitleForSurvey(dashboardActivity, survey);
    }

    private void closeFeedbackFragment() {

        //Clear feedback fragment
        //ScoreRegister.clear(Session.getSurveyByModule().getId_survey());

        FeedbackFragment feedbackFragment = (FeedbackFragment) dashboardActivity.getFragmentManager ().findFragmentById(R.id.dashboard_completed_container);
        feedbackFragment.unregisterReceiver();
        feedbackFragment.getView().setVisibility(View.GONE);

        //Reload improve fragment
        if (DashboardOrientation.VERTICAL.equals(dashboardController.getOrientation())) {
            dashboardController.reloadVertical();
        }else{
            reloadFragment();
        }

        //Update action bar title
        super.setActionBarDashboard();
    }
}
