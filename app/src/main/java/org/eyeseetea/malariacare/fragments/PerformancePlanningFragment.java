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

import android.app.ListFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.layout.adapters.dashboard.FutureAssessmentPlanningAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.PerformancePlanningAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class PerformancePlanningFragment extends ListFragment {

    PerformancePlanningAdapter performancePlanningAdapter;
    ViewGroup headerView;

    public PerformancePlanningFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        headerView = (ViewGroup) inflater.inflate(R.layout.performance_planning_header, null);
        return inflater.inflate(R.layout.fragment_performance_planning, container, false);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (headerView != null)  this.getListView().addHeaderView(headerView);

        performancePlanningAdapter = new PerformancePlanningAdapter(Survey.listAll(Survey.class), getActivity());
        setListAdapter(performancePlanningAdapter);

        //getListView().setOnItemClickListener(this);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}