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
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.monitor.MonitorMessagesBuilder;
import org.eyeseetea.malariacare.data.database.utils.monitor.allassessments.SentSurveysBuilderBase;
import org.eyeseetea.malariacare.data.database.utils.monitor.allassessments
        .SentSurveysBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.allassessments
        .SentSurveysBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.monitor.facility.FacilityTableBuilderBase;
import org.eyeseetea.malariacare.data.database.utils.monitor.facility.FacilityTableBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.facility.FacilityTableBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.monitor.pie.PieBuilderBase;
import org.eyeseetea.malariacare.data.database.utils.monitor.pie.PieBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.pie.PieBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.layout.dashboard.config.MonitorFilter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ignac on 10/12/2015.
 */
public class MonitorFragment extends Fragment implements IModuleFragment {
    List<SurveyDB> surveysForGraphic;
    public static final String TAG = ".MonitorFragment";
    private SurveyReceiver surveyReceiver;
    private List<SurveyDB> surveys;
    private List<ProgramDB> programs;
    private List<OrgUnitDB> orgUnits;
    private WebView webView;
    public MonitorFilter filterType;

    public MonitorFragment() {
        this.surveys = new ArrayList();
        this.programs = new ArrayList<>();
        this.orgUnits = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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
    public void onResume() {
        Log.d(TAG, "onResume");
        //Loading...
        //setListShown(false);
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();
        stopMonitor();
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }


    public void setFilterType(MonitorFilter monitorFilter) {
        this.filterType = monitorFilter;
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.ALL_MONITOR_DATA_ACTION));
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
        BaseServiceBundle data = (BaseServiceBundle) Session.popServiceValue(
                SurveyService.ALL_MONITOR_DATA_ACTION);
        if (data != null) {
            surveysForGraphic = (List<SurveyDB>) data.getModelList(SurveyDB.class.getName());
            //Remove the bad surveys.
            Iterator<SurveyDB> iter = surveysForGraphic.iterator();
            while (iter.hasNext()) {
                SurveyDB survey = iter.next();
                if (!survey.hasMainScore()) {
                    iter.remove();
                }
            }
            programs = (List<ProgramDB>) data.getModelList(ProgramDB.class.getName());
            orgUnits = (List<OrgUnitDB>) data.getModelList(OrgUnitDB.class.getName());

            reloadSurveys(surveysForGraphic, programs, orgUnits);
        }
    }

    public void reloadSurveys(List<SurveyDB> newListSurveys, List<ProgramDB> newListPrograms,
            List<OrgUnitDB> newListOrgUnit) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): "
                + newListSurveys.size());
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
                //Update hardcoded messages
                new MonitorMessagesBuilder(getActivity()).addDataInChart(view);

                //Update hardcoded messages
                new MonitorMessagesBuilder(getActivity()).addDataInChart(view);

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    new SentSurveysBuilderByOrgUnit(surveysForGraphic, getActivity(),
                            orgUnits).addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    new SentSurveysBuilderByProgram(surveysForGraphic, getActivity(),
                            programs).addDataInChart(view);
                }

                //Show stats by program
                SentSurveysBuilderBase.showData(view);

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    new PieBuilderByOrgUnit(surveysForGraphic, getActivity()).addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    new PieBuilderByProgram(surveysForGraphic, getActivity()).addDataInChart(view);
                }
                //Render the table and pie.
                PieBuilderBase.showPieTab(view);

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    //facility by progam-> is a orgunit facility
                    new FacilityTableBuilderByProgram(surveysForGraphic,
                            getActivity()).addDataInChart(view);
                    FacilityTableBuilderByOrgUnit.showFacilities(view);
                }
                if (isProgramFilterActive()) {
                    //facility by orgunit-> is a program facility
                    new FacilityTableBuilderByOrgUnit(surveysForGraphic,
                            getActivity()).addDataInChart(view);
                    FacilityTableBuilderByProgram.showFacilities(view);
                }

                //Draw facility main table
                //Set the colors of red/green/yellow pie and table
                FacilityTableBuilderBase.setColor(view);
            }
        });


        //Load html
        webView.loadUrl("file:///android_asset/dashboard/dashboard.html");
    }

    private boolean isOrgUnitFilterActive() {
        return filterType.equals(MonitorFilter.ALL) || filterType.equals(MonitorFilter.ORG_UNIT);
    }

    private boolean isProgramFilterActive() {
        return filterType.equals(MonitorFilter.ALL) || filterType.equals(MonitorFilter.PROGRAM);
    }

    private WebView initMonitor() {
        Activity activity = getActivity();
        WebView webView = (WebView) activity.findViewById(R.id.dashboard_monitor);
        webView.setWebChromeClient(new MyWebChromeClient());
        //Init webView settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(
                new WebViewInterceptor(DashboardActivity.dashboardActivity, new UIThreadExecutor()),
                "Android");
        return webView;
    }

    /**
     * Stops webView gracefully
     */
    private void stopMonitor() {
        try {
            if (webView != null) {
                webView.stopLoading();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load and reload sent surveys
     */
    @Override
    public void reloadData() {
        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_MONITOR_DATA_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
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
                reloadSentSurveys();
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.d("Webview log", String.format("%s @ %d: %s",
                    cm.message(), cm.lineNumber(), cm.sourceId()));
            return true;
        }
    }
}
