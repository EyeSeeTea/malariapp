/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.fragments;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.monitor.FacilityTableBuilder;
import org.eyeseetea.malariacare.database.utils.monitor.PieTabGroupBuilder;
import org.eyeseetea.malariacare.database.utils.monitor.SentSurveysBuilder;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.services.SurveyService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ignac on 10/12/2015.
 */
public class MonitorFragment extends Fragment {
    List<Survey> surveysForGraphic;
    public static final String TAG = ".CompletedFragment";
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;
    protected IDashboardAdapter adapter;
    private static int index = 0;
    private WebView webView;
    Spinner filterSpinnerProgram;
    Spinner filterSpinnerOrgUnit;
    String selectedOrgUnit;
    String selectedProgram;
    public MonitorFragment() {
        this.adapter = Session.getAdapterSent();
        this.surveys = new ArrayList();
    }

    public static MonitorFragment newInstance(int index) {
        MonitorFragment f = new MonitorFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        //Loading...
        //setListShown(false);
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();
        stopMonitor();
        super.onStop();
    }
    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_SENT_SURVEYS_ACTION));
        }
    }
    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver() {
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }
    /**
     * load and reload sent surveys
     */
    public void reloadSentSurveys() {
        surveysForGraphic = (List<Survey>) Session.popServiceValue(SurveyService.ALL_SENT_SURVEYS_ACTION);
        reloadSurveys(surveysForGraphic);
    }


    public void reloadSurveys(List<Survey> newListSurveys) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        boolean hasSurveys = newListSurveys != null && newListSurveys.size() > 0;
        this.surveys.clear();
        this.surveys.addAll(newListSurveys);
        if (hasSurveys) {
            reloadMonitor();
        }

        //setListShownNoAnimation(false);
    }
    public void reloadMonitor() {
        webView = initMonitor();
        //onPageFinish load data
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //Add line chart
                HashMap<Program,ArrayList<Survey>> mapSurveysByProgram= new HashMap<Program,ArrayList<Survey>> ();


                final List<Program> programs=new ArrayList<Program>();
                Collections.sort(surveysForGraphic, new Comparator<Survey>() {
                    public int compare(Survey surveyA, Survey surveyB) {
                        if (!programs.contains(surveyA.getTabGroup().getProgram())) {
                            programs.add(surveyA.getTabGroup().getProgram());
                        }
                        return surveyA.getTabGroup().getProgram().getUid().compareTo(surveyB.getTabGroup().getProgram().getUid());
                    }
                });
                new SentSurveysBuilder(surveysForGraphic, getActivity(),programs).addDataInChart(view);

                SentSurveysBuilder.showData(view);
                //List<Survey> surveysByProgramAndOrgUnit = filterSurveysByProgramAndOrgUnit(surveysForGraphic);


                //Add table x facility
                new FacilityTableBuilder(surveysForGraphic, getActivity()).addDataInChart(view);

                //Add pie charts
                new PieTabGroupBuilder(surveysForGraphic, getActivity()).addDataInChart(view);

                //Render the table and pie.
                PieTabGroupBuilder.showPieTab(view);
                FacilityTableBuilder.showFacilities(view);


            }
        });
        //Load html
        webView.loadUrl("file:///android_asset/dashboard/dashboard.html");
    }

    private WebView initMonitor() {
        WebView webView = (WebView) getActivity().findViewById(R.id.dashboard_monitor);
        //Init webView settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.getSettings().setJavaScriptEnabled(true);

        return webView;
    }

    /**
     * Stops webView gracefully
     */
    private void stopMonitor(){
        try{
            if(webView!=null){
                webView.stopLoading();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if (SurveyService.ALL_SENT_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadSentSurveys();
            }
        }
    }

    private List<Survey> filterSurveysByProgram(List<Survey> surveys) {
        HashMap<String, Survey> filteredSurveys;
        filteredSurveys = new HashMap<>();
        surveysForGraphic = new ArrayList<>();
        for (Survey survey : surveys) {
                if (!filteredSurveys.containsKey(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid()+survey.getId_survey())) {
                    if(filterSurveyByProgram(survey))
                        filteredSurveys.put(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid()+survey.getId_survey(), survey);
                }
        }
        for (Survey survey : filteredSurveys.values()) {
            surveysForGraphic.add(survey);
        }
        Log.d(TAG, "size" + surveysForGraphic.size());
        return surveysForGraphic;
    }
    private List<Survey> filterSurveysByProgramAndOrgUnit(List<Survey> surveys) {
        HashMap<String, Survey> filteredSurveys;
        filteredSurveys = new HashMap<>();
        surveysForGraphic = new ArrayList<>();
        for (Survey survey : surveys) {
            if (!filteredSurveys.containsKey(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid()+survey.getId_survey())) {
                if(filterSurveyByProgramAndOrgUnit(survey))
                    filteredSurveys.put(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid()+survey.getId_survey(), survey);
            }
        }
        for (Survey survey : filteredSurveys.values()) {
            surveysForGraphic.add(survey);
        }
        Log.d(TAG, "size" + surveysForGraphic.size());
        return surveysForGraphic;
    }
    private boolean filterSurveyByProgram(Survey survey) {
        if(selectedProgram.equals(survey.getTabGroup().getProgram().getUid()))
                return true;
        return false;
    }

    private boolean filterSurveyByProgramAndOrgUnit(Survey survey) {
        if(filterSurveyByProgram(survey))
            if(selectedOrgUnit.equals(survey.getOrgUnit().getUid()))
                return true;
        return false;
    }
}
