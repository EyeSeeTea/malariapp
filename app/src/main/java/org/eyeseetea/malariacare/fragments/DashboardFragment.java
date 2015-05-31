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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.orm.query.Select;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.DashboardDetailsActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AnalyticsAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.DashboardAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.FutureAssessmentPlanningAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.PerformancePlanningAdapter;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends ListFragment {
    boolean mDualPane;
    int mCurCheckPosition = 0;

    private List<Survey> surveys;
    private List<IDashboardAdapter> adapters;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // show the progress bar
        setListShown(false);

        // Get the not-sent surveys ordered by date
        this.surveys = Survey.getUnsentSurveys(5);

        // make a list with the adapters
        this.adapters = new ArrayList<>();
        this.adapters.add(new AssessmentAdapter(this.surveys, getActivity()));

        // create a list of listeners to capture the "see all" event
        List<View.OnClickListener> listeners = new ArrayList<>();
        listeners.add(new DashboardListener(getActivity(), getString(R.string.dashboard_button_see_all), 0));

        setListAdapter(new DashboardAdapter(this.surveys, this.adapters, listeners, getActivity()));

        // Check to see if we have a frame in which to embed the details fragment directly in the containing UI
        View detailsFrame = getActivity().findViewById(R.id.details);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null){
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        // FIXME: This piece of code is not yet properly working and thought to be used for landscape display of dashboard
        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state
            showDetails(mCurCheckPosition);
        }

        // hide the progress bar
        setListShown(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // show the progress bar
        setListShown(false);
        // show details in fragment or activity
        showDetails(position);
        // hide the progress bar
        setListShown(true);
    }*/

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    void showDetails(int index) {
        mCurCheckPosition = index;

        // FIXME: This piece of code is not yet properly working and thought to be used for landscape display of dashboard
        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            IDashboardAdapter adapter = (IDashboardAdapter) getListView().getSelectedItem();
            DashboardDetailsFragment details = (DashboardDetailsFragment)
                    getFragmentManager().findFragmentById(R.id.details);
            if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = DashboardDetailsFragment.newInstance(index);

                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (index == 0) {
                    //ft.replace(R.id.details, details);
                    ft.replace(R.id.dashboard_details_fragment, details);
                } else {
                    Log.i(".DashboardFragment", "reaching this");
                    //ft.replace(R.id.a_item, details);
                }
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            Intent intent = new Intent();
            intent.setClass(getActivity(), DashboardDetailsActivity.class);
            intent.putExtra("index", index);
            Session.setAdapter(adapters.get(index));
            startActivity(intent);
        }
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    private class DashboardListener implements View.OnClickListener {

        private String listenerOption; //One of edit, delete
        private Activity context;
        private int index;

        public DashboardListener(Activity context, String listenerOption, int index) {
            this.context = context;
            this.listenerOption = listenerOption;
            this.index = index;
        }

        public void onClick(View view) {
            if (listenerOption.equals(context.getString(R.string.dashboard_button_see_all))) {
                showDetails(index);
            }
        }
    }
}