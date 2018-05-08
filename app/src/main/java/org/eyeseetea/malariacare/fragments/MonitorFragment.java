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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderBase;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.layout.dashboard.config.MonitorFilter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MonitorFragment extends Fragment implements IModuleFragment {
    List<SurveyDB> surveysForGraphic;
    public static final String TAG = ".MonitorFragment";
    private SurveyReceiver surveyReceiver;
    private List<SurveyDB> surveys;
    private List<ProgramDB> programs;
    private List<OrgUnitDB> orgUnits;
    private WebView webView;
    public MonitorFilter filterType;
    private WebViewInterceptor mWebViewInterceptor;

    private OrgUnitProgramFilterView orgUnitProgramFilterView;

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

        loadFilter();

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(ProgramDB selectedProgramFilter) {
                        pushProgramFilterToJavascript(selectedProgramFilter.getUid());
                        saveCurrentFilters();
                    }

                    @Override
                    public void onOrgUnitFilterChanged(OrgUnitDB selectedOrgUnitFilter) {
                        pushOrgUnitFilterToJavascript(selectedOrgUnitFilter.getUid());
                        saveCurrentFilters();
                    }
                });


        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void pushOrgUnitFilterToJavascript(String selectedOrgUnitFilter) {
        String JAVASCRIPT_UPDATE_FILTER = "javascript:updateOrgUnitFilter('%s')";
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_FILTER, selectedOrgUnitFilter);
        Log.d(TAG, updateChartJS);
        if(webView!=null) {
            webView.loadUrl(updateChartJS);
        }
    }

    private void pushProgramFilterToJavascript(String selectedProgramFilter) {
        String JAVASCRIPT_UPDATE_FILTER = "javascript:updateProgramFilter('%s')";
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_FILTER, selectedProgramFilter);
        Log.d(TAG, updateChartJS);
        if(webView!=null) {
            webView.loadUrl(updateChartJS);
        }
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(
                orgUnitProgramFilterView.getSelectedProgramFilter().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter().getUid());
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

    private void updateSelectedFilters() {
        if (orgUnitProgramFilterView == null) {
            loadFilter();
        }
        String programUidFilter = PreferencesState.getInstance().getProgramUidFilter();
        String orgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();
        orgUnitProgramFilterView.changeSelectedFilters(programUidFilter, orgUnitUidFilter);
    }

    private void loadFilter() {
        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.monitor_org_unit_program_filter_view);
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
        showMessageNoAddedSurveys((surveysForGraphic == null || surveysForGraphic.isEmpty()));
    }

    private void showMessageNoAddedSurveys(boolean show) {
        getActivity().findViewById(R.id.monitor_no_surveys_message).setVisibility(
                show ? View.VISIBLE : View.GONE);
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
                if (!isAdded()) {
                    return;
                }
                //Update hardcoded messages
                new MonitorMessagesBuilder().addDataInChart(view);

                //Update hardcoded messages
                new MonitorMessagesBuilder().addDataInChart(view);

                //Set the colors of red/green/yellow pie and table
                FacilityTableBuilderBase.setColor(view);

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    new SentSurveysBuilderByOrgUnit(surveysForGraphic, getActivity(),
                            orgUnits).addDataInChart(view);
                }

                if (isProgramFilterActive()) {
                    new SentSurveysBuilderByProgram(surveysForGraphic, getActivity(),
                            programs).addDataInChart(view);
                }
                //Set chart title
                SentSurveysBuilderBase.injectChartTitle(webView);

                //Show stats by program
                SentSurveysBuilderBase.showData(view);

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    new PieBuilderByOrgUnit(surveysForGraphic).addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    new PieBuilderByProgram(surveysForGraphic).addDataInChart(view);
                }

                //Add line chart
                if (isOrgUnitFilterActive()) {
                    //facility by progam-> is a orgunit facility
                    new FacilityTableBuilderByProgram(surveysForGraphic).addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    //facility by orgunit-> is a program facility
                    new FacilityTableBuilderByOrgUnit(surveysForGraphic).addDataInChart(view);
                }

                //Draw facility main table

                String programUidFilter = PreferencesState.getInstance().getProgramUidFilter();
                String orgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();
                if(!programUidFilter.equals("")){
                    pushProgramFilterToJavascript(programUidFilter);
                }else if(!orgUnitUidFilter.equals("")){
                    pushOrgUnitFilterToJavascript(orgUnitUidFilter);
                }else{
                    FacilityTableBuilderByProgram.showFacilities(view);
                }
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
        //Init webView settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        mWebViewInterceptor = new WebViewInterceptor();
        mWebViewInterceptor.setBubbleClickListener(new WebViewInterceptor.BubbleClickListener() {
            @Override
            public void onClickMultipleSurveys(String uidList) {
                ArrayList<SurveyDB> surveys = new ArrayList<>();
                if (uidList.length() > 0) {
                    String uids[] = uidList.split(";");
                    for (String uid : uids) {
                        surveys.add(SurveyDB.findById(Long.parseLong(uid)));
                    }
                }

                showListOfSurveys(surveys);
            }

            @Override
            public void onClickSingleSurvey(final String uid) {
                new UIThreadExecutor().run(new Runnable() {
                    @Override
                    public void run() {
                        DashboardActivity.dashboardActivity.openFeedback(SurveyDB.findById(Long.parseLong(uid)), false);
                    }
                });
            }
        });
        webView.addJavascriptInterface(mWebViewInterceptor,
                "Android");
        return webView;
    }

    public void showListOfSurveys(final ArrayList<SurveyDB> surveys) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.dashboardActivity);
        LayoutInflater inflater = DashboardActivity.dashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.historical_log_dialog, null);
        builder.setView(v);
        TextView orgUnit = (TextView) v.findViewById(R.id.org_unitName);
        TextView program = (TextView) v.findViewById(R.id.programName);
        program.setText(surveys.get(0).getProgram().getName());
        orgUnit.setText(surveys.get(0).getOrgUnit().getName());
        Button cancel = (Button) v.findViewById(R.id.cancel);
        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.log_content);
        View row = inflater.inflate(R.layout.item_list_dialog_header, null);
        ((TextView)row.findViewById(R.id.first_column)).setText(R.string.assessment_sent_date);
        ((TextView)row.findViewById(R.id.second_column)).setText(R.string.score);
        linearLayout.addView(row);
        final AlertDialog alertDialog = builder.create();
        for(final SurveyDB survey: surveys){
            row = inflater.inflate(R.layout.item_list_row_row, null);
            TextView completionDate = (TextView) row.findViewById(R.id.first_column);
            TextView score = (TextView) row.findViewById(R.id.second_column);
            completionDate.setText(AUtils.getEuropeanFormatedDate(survey.getCompletionDate()));
            score.setText(Math.round(survey.getMainScore())+"");
            Resources resources = PreferencesState.getInstance().getContext().getResources();

            ScoreType scoreType = new ScoreType(survey.getMainScore());
            if (scoreType.isTypeA()) {
                score.setBackgroundColor(resources.getColor(R.color.lightGreen));
            }else if (scoreType.isTypeB()){
                score.setBackgroundColor(resources.getColor(R.color.assess_yellow));
            }else if (scoreType.isTypeC()){
                score.setBackgroundColor(resources.getColor(R.color.darkRed));
            }
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    new UIThreadExecutor().run(new Runnable() {
                        @Override
                        public void run() {
                            DashboardActivity.dashboardActivity.openFeedback(survey, false);
                        }
                    });
                }
            });
            linearLayout.addView(row );
        }
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }
        );

        alertDialog.show();
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
        updateSelectedFilters();

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
}
