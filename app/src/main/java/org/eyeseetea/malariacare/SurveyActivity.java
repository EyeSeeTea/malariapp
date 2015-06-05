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

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SurveyActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private List<Tab> tabsList;
    private Map<Tab, ITabAdapter> adaptersMap = new HashMap<Tab, ITabAdapter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(".SurveyActivity", "Starting");
        setContentView(R.layout.survey);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        Program program = Session.getSurvey().getProgram();
        List<CompositeScore> compositeScores = new ArrayList<CompositeScore>();

        Log.i(".SurveyActivity", "Registering Composite Score");
        //Intializing composite score register
        for (CompositeScore compositeScore : CompositeScore.listAll(CompositeScore.class)) {

            //If the questions of the compositescore belongs to the program, the comp. score is addded
            Question compositeScoreQuestion = compositeScore.getSingleQuestionIncludingChildren();
            if (compositeScoreQuestion.belongsToProgram(program)) {
                Log.i(".SurveyActivity", "Include "+ compositeScore.getCode());
                compositeScores.add(compositeScore);
                ScoreRegister.registerScore(compositeScore);
            }

        }

        Log.i(".SurveyActivity", "Creating Adapter");
        tabsList = Tab.getTabsBySession();

        for (Tab tab : tabsList) {
            if (tab.getName().equals(Constants.COMPOSITE_SCORE_TAB_NAME))
                adaptersMap.put(tab, new CompositeScoreAdapter(compositeScores, this, R.layout.composite_score_tab, tab.getName()));
            else if (tab.getType() != Constants.TAB_SCORE_SUMMARY) {
                ScoreRegister.registerScore(tab);
                adaptersMap.put(tab, AutoTabAdapter.build(tab,this));
            }
        }

        Log.i(".SurveyActivity", "Creating Menu");
        createMenu();

        // Show survey info as a footer below the form
        LayoutUtils.setActionBarText(actionBar, Session.getSurvey().getOrgUnit().getName(), Session.getSurvey().getProgram().getName());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void createMenu() {

        final Spinner menu = (Spinner) this.findViewById(R.id.tabSpinner);

        menu.setAdapter(new TabArrayAdapter(this, tabsList));
        menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tab selectedTab = (Tab) menu.getSelectedItem();
                showTab(selectedTab);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showTab(Tab selectedTab) {

        if (selectedTab.isGeneralScore())
            showGeneralScores();
        else {
            LayoutInflater inflater = LayoutInflater.from(this);
            ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
            parent.removeAllViews();

            ITabAdapter tabAdapter = adaptersMap.get(selectedTab);

            View view = inflater.inflate(tabAdapter.getLayout(), parent, false);
            parent.addView(view);

            if (selectedTab.getType() == Constants.TAB_AUTOMATIC_SCORED || selectedTab.getType() == Constants.TAB_CUSTOM_SCORED
                    || selectedTab.getType() == Constants.TAB_SCORE_SUMMARY) {
                tabAdapter.initializeSubscore();
            }

            ListView mQuestions = (ListView) this.findViewById(R.id.listView);
            mQuestions.setAdapter((BaseAdapter) tabAdapter);

        }
    }

    private void showGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, parent, false);
        parent.addView(view);

        Float tab1 = 0F, tab2 = 0F, tab3 = 0F, tab4 = 0F, tab5 = 0F, tab6 = 0F, tab7 = 0F, tab8 = 0F, tab9 = 0F;

        List<ITabAdapter> adaptersList = new ArrayList<ITabAdapter>(adaptersMap.values());

        // FIXME: This is a very ugly way of doing it, change it soon
        if (adaptersList.get(4) != null) {
            tab1 = adaptersList.get(6).getScore();
            ((TextView) this.findViewById(R.id.profileScore)).setText(Utils.round(tab1));
            LayoutUtils.trafficLight(this.findViewById(R.id.profileScore), tab1, null);
        }
        if (adaptersList.get(2) != null) {
            tab9 = adaptersList.get(2).getScore();
            ((TextView) this.findViewById(R.id.envAndMatScore)).setText(Utils.round(tab9));
            LayoutUtils.trafficLight(this.findViewById(R.id.envAndMatScore), tab9, null);
        }
        if (adaptersList.get(8) != null) {
            tab8 = adaptersList.get(8).getScore();
            ((TextView) this.findViewById(R.id.feedbackScore)).setText(Utils.round(tab8));
            LayoutUtils.trafficLight(this.findViewById(R.id.feedbackScore), tab8, null);
        }
        if (adaptersList.get(5) != null) {
            tab2 = adaptersList.get(6).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase1)).setText(Utils.round(tab2));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase1), tab2, null);
        }
        if (adaptersList.get(1) != null) {
            tab4 = adaptersList.get(1).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase2)).setText(Utils.round(tab4));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase2), tab4, null);
        }
        if (adaptersList.get(0) != null) {
            tab6 = adaptersList.get(0).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase3)).setText(Utils.round(tab6));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase3), tab6, null);
        }
        if (adaptersList.get(8) != null) {
            tab3 = adaptersList.get(0).getScore();
            ((TextView) this.findViewById(R.id.rdtCase1)).setText(Utils.round(tab3));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase1), tab3, null);
        }
        if (adaptersList.get(7) != null) {
            tab5 = adaptersList.get(9).getScore();
            ((TextView) this.findViewById(R.id.rdtCase2)).setText(Utils.round(tab5));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase2), tab5, null);
        }
        if (adaptersList.get(2) != null) {
            tab7 = adaptersList.get(5).getScore();
            ((TextView) this.findViewById(R.id.rdtCase3)).setText(Utils.round(tab7));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase3), tab7, null);
        }

        Float avgClinical = (tab2 + tab4 + tab6) / 3;
        Float avgRdt = (tab3 + tab5 + tab7) / 3;
        Float overall = (avgClinical + avgRdt + tab1 + tab8 + tab9) / 5;

        ((TextView) this.findViewById(R.id.clinicalAvg)).setText(Utils.round(avgClinical));
        LayoutUtils.trafficLight(this.findViewById(R.id.clinicalAvg), avgClinical, null);
        ((TextView) this.findViewById(R.id.rdtAvg)).setText(Utils.round(avgRdt));
        LayoutUtils.trafficLight(this.findViewById(R.id.rdtAvg), avgRdt, null);
        ((TextView) this.findViewById(R.id.totalScore)).setText(Utils.round(overall));
        LayoutUtils.trafficLight(this.findViewById(R.id.totalScore), overall, null);

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SurveyActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
