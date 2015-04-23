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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssessmentFragment extends ListFragment  {

    AssessmentAdapter assessmentAdapter;
    ViewGroup headerView;
    List<Survey> surveyList;

    public AssessmentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        headerView = (ViewGroup) inflater.inflate(R.layout.assessment_header, null);
        return inflater.inflate(R.layout.fragment_assessment, container, false);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (headerView != null)  this.getListView().addHeaderView(headerView);

        surveyList = Survey.listAll(Survey.class);
        assessmentAdapter = new AssessmentAdapter(surveyList, getActivity());
        setListAdapter(assessmentAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        Session.setSurvey(surveyList.get(position-1));

        //Call Survey Activity
        getActivity().finish();
        Intent surveyIntent = new Intent(v.getContext(), MainActivity.class);
        v.getContext().startActivity(surveyIntent);
    }
}