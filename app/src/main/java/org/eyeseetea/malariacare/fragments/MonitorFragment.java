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

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.monitor.AllAssessment.SentSurveysBuilderByOrgUnit;
import org.eyeseetea.malariacare.database.utils.monitor.AllAssessment.SentSurveysBuilderByProgram;
import org.eyeseetea.malariacare.database.utils.monitor.Facility.FacilityTableBuilderBase;
import org.eyeseetea.malariacare.database.utils.monitor.Facility.FacilityTableBuilderByOrgUnit;
import org.eyeseetea.malariacare.database.utils.monitor.Facility.FacilityTableBuilderByProgram;
import org.eyeseetea.malariacare.database.utils.monitor.MonitorMessagesBuilder;
import org.eyeseetea.malariacare.database.utils.monitor.Pie.PieTabGroupBuilderBase;
import org.eyeseetea.malariacare.database.utils.monitor.Pie.PieTabGroupBuilderByOrgUnit;
import org.eyeseetea.malariacare.database.utils.monitor.Pie.PieTabGroupBuilderByProgram;
import org.eyeseetea.malariacare.database.utils.monitor.AllAssessment.SentSurveysBuilderBase;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ignac on 10/12/2015.
 */
public class MonitorFragment extends Fragment implements IModuleFragment{
    List<Survey> surveysForGraphic;
    public static final String TAG = ".MonitorFragment";
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;
    private List<Program> programs;
    private List<OrgUnit> orgUnits;
    protected IDashboardAdapter adapter;
    private WebView webView;

    public MonitorFragment() {
        this.adapter = Session.getAdapterSent();
        this.surveys = new ArrayList();
        this.programs = new ArrayList<>();
        this.orgUnits = new ArrayList<>();
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
    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }
    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_MONITOR_DATA_ACTION));
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
    @Override
    public void reloadData() {
        HashMap<String,List> data= (HashMap<String,List>) Session.popServiceValue(SurveyService.ALL_MONITOR_DATA_ACTION);
        if(data!=null) {
            surveysForGraphic = data.get(SurveyService.PREPARE_SURVEYS);
            //Remove the bad surveys.
            Iterator<Survey> iter = surveysForGraphic.iterator();
            while(iter.hasNext()){
                Survey survey = iter.next();
                if(!survey.hasMainScore())
                {
                    iter.remove();
                }
            }

            programs = data.get(SurveyService.PREPARE_PROGRAMS);
            reloadSurveys(surveysForGraphic,programs);
        }
        reloadSurveys(surveysForGraphic,data.get(SurveyService.PREPARE_PROGRAMS), data.get(SurveyService.PREPARE_ORG_UNIT));
    }

    public void reloadSurveys(List<Survey> newListSurveys,List<Program> newListPrograms, List<OrgUnit> newListOrgUnit) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        boolean hasSurveys = newListSurveys != null && newListSurveys.size() > 0;
        boolean hasPrograms = newListPrograms != null && newListPrograms.size() > 0;
        boolean hasOrgUnits = newListOrgUnit != null && newListOrgUnit.size() > 0;
        this.surveys.clear();
        this.surveys.addAll(newListSurveys);
        this.programs = newListPrograms;
        this.orgUnits = newListOrgUnit;
        if (hasPrograms && hasSurveys && hasOrgUnits) {
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
                new SentSurveysBuilderByOrgUnit(surveysForGraphic, getActivity(),orgUnits).addDataInChart(view);
                new SentSurveysBuilderByProgram(surveysForGraphic, getActivity(),programs).addDataInChart(view);

                //Show stats by program
                SentSurveysBuilderBase.showData(view);

                //Add table x facility
                new FacilityTableBuilderByOrgUnit(surveysForGraphic, getActivity()).addDataInChart(view);
                new FacilityTableBuilderByProgram(surveysForGraphic, getActivity()).addDataInChart(view);

                //Add pie charts
                new PieTabGroupBuilderByOrgUnit(surveysForGraphic, getActivity()).addDataInChart(view);
                new PieTabGroupBuilderByProgram(surveysForGraphic, getActivity()).addDataInChart(view);

                //Render the table and pie.
                PieTabGroupBuilderBase.showPieTab(view);
                FacilityTableBuilderBase.showFacilities(view);

                //Set the colors of red/green/yellow pie and table

                FacilityTableBuilderBase.setColor(webView);
            }
        });
        //Load html
        webView.loadUrl("file:///android_asset/dashboard/dashboard.html");
    }

    private WebView initMonitor() {
        Activity activity=getActivity();
        WebView webView = (WebView) activity.findViewById(R.id.dashboard_monitor);
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
            if (SurveyService.ALL_MONITOR_DATA_ACTION.equals(intent.getAction())) {
                reloadData();
            }
        }
    }
}
