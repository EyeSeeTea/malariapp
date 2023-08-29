/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.presentation.presenters.surveys.SurveysPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

public class DashboardUnsentFragment extends FiltersFragment implements SurveysPresenter.View {

    public static final String TAG = ".UnsentFragment";
    //private SurveyReceiver surveyReceiver;
    private SurveysPresenter surveysPresenter;

    private AssessmentUnsentAdapter adapter;

    private FloatingActionButton createSurveyButton;
    private TextView noSurveysText;

    private RecyclerView recyclerView;
    private View rootView;

    @Override
    protected void onFiltersChanged() {
        reloadData();
    }

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.assess_org_unit_program_filter_view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.assess_listview, null);

        noSurveysText = rootView.findViewById(R.id.no_surveys);
        createSurveyButton = rootView.findViewById(R.id.start_button);

        initRecyclerView();
        initPresenter();

        return rootView;
    }

    private void initPresenter() {
        surveysPresenter = DataFactory.INSTANCE.provideSurveysPresenter();

        surveysPresenter.attachView(this, SurveyStatusFilter.IN_PROGRESS, getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
    }

    @Override
    public void onDestroy() {
        surveysPresenter.detachView();

        super.onDestroy();
    }

    private void showOrHiddenButton(SurveyViewModel survey) {
        String orgUnitFilter = getSelectedOrgUnitUidFilter();
        String programFilter = getSelectedProgramUidFilter();

        if (orgUnitFilter.equals("") || programFilter.equals("")) {
            createSurveyButton.show();
            noSurveysText.setText(R.string.assess_no_surveys);
        } else if (survey != null ||
                !OrgUnitProgramRelationDB.existProgramAndOrgUnitRelation(programFilter, orgUnitFilter)) {
            ((View)createSurveyButton).setVisibility(View.INVISIBLE);
            noSurveysText.setText(R.string.survey_not_assigned_facility);
        } else {
            createSurveyButton.show();
            noSurveysText.setText(R.string.assess_no_surveys);
        }
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.unsentSurveyList);

        adapter = new AssessmentUnsentAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void reloadData() {
        super.reloadData();

        if (surveysPresenter != null){
            surveysPresenter.refresh( getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
        }
    }

    private void showOrHiddenList(boolean hasSurveys) {
        if (hasSurveys) {
            noSurveysText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noSurveysText.setVisibility(View.GONE);
        }
    }

    @Override
    public void showSurveys(@NonNull List<SurveyViewModel> surveys) {
        if (this.adapter != null) {
            this.adapter.setSurveys(surveys);
            SurveyViewModel survey = null;
            if (surveys.size() > 0) {
                survey = surveys.get(0);
            }
            showOrHiddenButton(survey);
            showOrHiddenList(surveys.isEmpty());
        }
    }

    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }
}