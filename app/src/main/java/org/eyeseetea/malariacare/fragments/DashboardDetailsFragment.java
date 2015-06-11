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

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.listeners.SwipeDismissListViewTouchListener;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardDetailsFragment extends ListFragment {

    private List<Survey> surveys;
    protected IDashboardAdapter adapter;
    private static int index = 0;

    public DashboardDetailsFragment(){
        this.adapter = Session.getAdapter();
    }

    public static DashboardDetailsFragment newInstance(int index) {
        DashboardDetailsFragment f = new DashboardDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }


    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.surveys = Survey.getAllUnsentSurveys();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IDashboardAdapter adapterE = Session.getAdapter().newInstance(this.surveys, getActivity());
        this.adapter = adapterE;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View header = inflater.inflate(this.adapter.getHeaderLayout(), null, false);
        View footer = inflater.inflate(this.adapter.getFooterLayout(), null, false);
        ListView listView = getListView();
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        setListAdapter((BaseAdapter) adapter);

        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return position>0 && position<=((AssessmentAdapter)adapter).getCount();
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (final int position : reverseSortedPositions) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getActivity().getString(R.string.dialog_title_delete_survey))
                                            .setMessage(getActivity().getString(R.string.dialog_info_delete_survey))
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    ((Survey)adapter.getItem(position-1)).delete();
                                                    adapter.remove(adapter.getItem(position - 1));
                                                    adapter.notifyDataSetChanged();
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).create().show();
                                }

                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());


        setListShown(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l, v, position, id);

        //Discard clicks on header|footer
        if(position<=0 || position > surveys.size()){
            return;
        }

        Session.setSurvey(surveys.get(position-1));

        //Call Survey Activity
        Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
        v.getContext().startActivity(surveyIntent);
    }

    @Override
    public void onResume(){
        super.onResume();
    }
}