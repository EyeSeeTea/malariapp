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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositiveScoreAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SurveyActivity extends ActionBarActivity {

    private List<Tab> tabsList;
    private Map<Tab, ITabAdapter> adaptersMap = new HashMap<Tab, ITabAdapter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(".SurveyActivity", "Starting");
        initializeApp();

        Log.i(".SurveyActivity", "Registering Compositive Score");
        //Intializing compositive score register
        for (CompositiveScore compositiveScore : CompositiveScore.listAll(CompositiveScore.class)) {
            ScoreRegister.registerScore(compositiveScore);
        }

        Log.i(".SurveyActivity", "Creating Adapter");
        tabsList = Tab.getTabsBySession();
        for (Tab tab : tabsList){
            if (tab.getName().equals("Compositive Scores"))
                adaptersMap.put(tab, new CompositiveScoreAdapter(CompositiveScore.listAll(CompositiveScore.class), this, R.layout.compositivescoretab, tab.getName()));
            else if (!tab.getName().equals("Score")) {
                ScoreRegister.registerScore(tab);
                adaptersMap.put(tab, new AutoTabAdapter(Utils.convertTabToArray(tab), this, R.layout.form, tab.getName()));
                //adapters.put(tab, new AutoTabAdapter(tab, this));
            }
        }

        Log.i(".SurveyActivity", "Creating Menu");
        createMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;// TODO: implement the settings menu
            case R.id.action_pull:
                return true;// TODO: implement the DHIS pull
            case R.id.action_license:
                Log.d(".MainActivity", "User asked for license");
                DialogDispatcher mf = DialogDispatcher.newInstance(item.getActionView());
                mf.showDialog(getFragmentManager(), DialogDispatcher.LICENSE_DIALOG);
                break;
            case R.id.action_about:
                DialogDispatcher aboutD = DialogDispatcher.newInstance(this.getCurrentFocus());
                aboutD.showDialog(getFragmentManager(), DialogDispatcher.ABOUT_DIALOG);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeApp() {
        setContentView(R.layout.survey);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.qualityapp_logo);
    }

    private void showTab(Tab selectedTab) {

        ListView mQuestions;
        LayoutInflater inflater = LayoutInflater.from(this);

        ITabAdapter mytab = adaptersMap.get(selectedTab);

        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();
        View view = inflater.inflate(mytab.getLayout(), parent, false);
        parent.addView(view);
        mQuestions = (ListView) this.findViewById(R.id.listView);
        mytab.initialize();
        mQuestions.setAdapter((BaseAdapter) mytab);
    }


    private void createMenu() {

        final Spinner menu = (Spinner) this.findViewById(R.id.tabSpinner);

        menu.setAdapter(new TabArrayAdapter(this, tabsList));
        menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tab selectedTab = (Tab) menu.getSelectedItem();
                if (selectedTab.getName().equals("Score"))
                    runGeneralScores();
                else
                    showTab(selectedTab);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void runGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, parent, false);
        parent.addView(view);

        Float tab1 = 0F, tab2 = 0F, tab3 = 0F, tab4 = 0F, tab5 = 0F, tab6 = 0F, tab7 = 0F, tab8 = 0F, tab9 = 0F;

        List<ITabAdapter> adaptersList = new ArrayList<ITabAdapter>(adaptersMap.values());

        if (adaptersList.get(1) != null) {
            tab1 = adaptersList.get(1).getScore();
            ((TextView) this.findViewById(R.id.profileScore)).setText(Utils.round(tab1));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.profileScore), tab1, null);
        }
        if (adaptersList.get(9) != null) {
            tab9 = adaptersList.get(9).getScore();
            ((TextView) this.findViewById(R.id.envAndMatScore)).setText(Utils.round(tab9));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.envAndMatScore), tab9, null);
        }
        if (adaptersList.get(8) != null) {
            tab8 = adaptersList.get(8).getScore();
            ((TextView) this.findViewById(R.id.feedbackScore)).setText(Utils.round(tab8));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.feedbackScore), tab8, null);
        }
        if (adaptersList.get(2) != null) {
            tab2 = adaptersList.get(2).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase1)).setText(Utils.round(tab2));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.clinicalCase1), tab2, null);
        }
        if (adaptersList.get(4) != null) {
            tab4 = adaptersList.get(4).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase2)).setText(Utils.round(tab4));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.clinicalCase2), tab4, null);
        }
        if (adaptersList.get(6) != null) {
            tab6 = adaptersList.get(6).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase3)).setText(Utils.round(tab6));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.clinicalCase3), tab6, null);
        }
        if (adaptersList.get(3) != null) {
            tab3 = adaptersList.get(3).getScore();
            ((TextView) this.findViewById(R.id.rdtCase1)).setText(Utils.round(tab3));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.rdtCase1), tab3, null);
        }
        if (adaptersList.get(5) != null) {
            tab5 = adaptersList.get(5).getScore();
            ((TextView) this.findViewById(R.id.rdtCase2)).setText(Utils.round(tab5));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.rdtCase2), tab5, null);
        }
        if (adaptersList.get(7) != null) {
            tab7 = adaptersList.get(7).getScore();
            ((TextView) this.findViewById(R.id.rdtCase3)).setText(Utils.round(tab7));
            LayoutUtils.trafficLight((TextView) this.findViewById(R.id.rdtCase3), tab7, null);
        }

        Float Avg_Clinical = (tab2 + tab4 + tab6) / 3;
        Float Avg_Rdt = (tab3 + tab5 + tab7) / 3;
        Float Overall = (Avg_Clinical + Avg_Rdt + tab1 + tab8 + tab9) / 5;

        ((TextView) this.findViewById(R.id.clinicalAvg)).setText(Utils.round(Avg_Clinical));
        LayoutUtils.trafficLight((TextView) this.findViewById(R.id.clinicalAvg), Avg_Clinical, null);
        ((TextView) this.findViewById(R.id.rdtAvg)).setText(Utils.round(Avg_Rdt));
        LayoutUtils.trafficLight((TextView) this.findViewById(R.id.rdtAvg), Avg_Rdt, null);
        ((TextView) this.findViewById(R.id.totalScore)).setText(Utils.round(Overall));
        LayoutUtils.trafficLight((TextView) this.findViewById(R.id.totalScore), Overall, null);

    }
}
