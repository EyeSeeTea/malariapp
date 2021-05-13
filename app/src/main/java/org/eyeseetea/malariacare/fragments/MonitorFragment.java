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

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetServerClassification;
import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeUpdateOrgUnitFilter;
import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeUpdateProgramFilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.monitor.InitMessagesInvoker;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.SetClassificationContextInvoker;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.layout.dashboard.config.MonitorFilter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.MonitorSurveysDialogFragment;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MonitorFragment extends FiltersFragment  implements IModuleFragment {
    List<SurveyDB> surveysForGraphic;
    public static final String TAG = ".MonitorFragment";
    private SurveyReceiver surveyReceiver;
    private List<SurveyDB> surveys;
    private List<ProgramDB> programs;
    private List<OrgUnitDB> orgUnits;
    private WebView webView;
    public MonitorFilter filterType;
    private WebViewInterceptor mWebViewInterceptor;

    private View rootView;

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    public static MonitorFragment newInstance(ServerClassification serverClassification) {
        MonitorFragment fragment = new MonitorFragment();

        Bundle args = new Bundle();
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
        fragment.setArguments(args);

        return fragment;
    }

    public MonitorFragment() {
        this.surveys = new ArrayList();
        this.programs = new ArrayList<>();
        this.orgUnits = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onFiltersChanged() {
        if (getSelectedProgramUidFilter().isEmpty() && getSelectedOrgUnitUidFilter().isEmpty()) {
            DashboardActivity.dashboardActivity.openMonitorByActions();
        } else {
            if (webView == null) return;

            if (!getSelectedProgramUidFilter().isEmpty()) {
                invokeUpdateProgramFilter(webView,getSelectedProgramUidFilter());
            } else {
                invokeUpdateOrgUnitFilter(webView,getSelectedOrgUnitUidFilter());
            }
        }
    }

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.monitor_org_unit_program_filter_view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

        rootView = inflater.inflate(R.layout.fragment_monitor_by_calendar, container, false);

        webView =  rootView.findViewById(R.id.dashboard_monitor);

        return rootView;
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
        unregisterSurveysReceiver();
        stopMonitor();
        super.onStop();
    }

    @Override
    public void onPause() {
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

                invokeSetServerClassification(webView, serverClassification);

                //Update hardcoded messages
                new InitMessagesInvoker().invoke(view);

                if (serverClassification == ServerClassification.COMPETENCIES) {
                    SetClassificationContextInvoker.invokeByCompetencies(view);
                } else {
                    SetClassificationContextInvoker.invokeByScoring(view);
                }

                if (isOrgUnitFilterActive()) {
                    new PieBuilderByOrgUnit(surveysForGraphic, serverClassification)
                            .addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    new PieBuilderByProgram(surveysForGraphic, serverClassification)
                            .addDataInChart(view);
                }

                if (isOrgUnitFilterActive()) {
                    new FacilityTableBuilderByProgram(surveysForGraphic).addDataInChart(
                            view,
                            serverClassification);
                }
                if (isProgramFilterActive()) {
                    new FacilityTableBuilderByOrgUnit(surveysForGraphic).addDataInChart(
                            view,
                            serverClassification);
                }

                String programUidFilter =
                        PreferencesState.getInstance().getProgramUidFilter();
                String orgUnitUidFilter =
                        PreferencesState.getInstance().getOrgUnitUidFilter();

                if (!programUidFilter.equals("")) {
                    invokeUpdateProgramFilter(webView, programUidFilter);
                } else if (!orgUnitUidFilter.equals("")) {
                    invokeUpdateOrgUnitFilter(webView, orgUnitUidFilter);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                    WebResourceError error) {
                //Your code to do
                Toast.makeText(getActivity(),
                        "Your Internet Connection May not be active Or " + error,
                        Toast.LENGTH_LONG).show();
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
                showListOfSurveys(uidList);
            }

            @Override
            public void onClickSingleSurvey(final String uid) {
                new UIThreadExecutor().run(new Runnable() {
                    @Override
                    public void run() {
                        DashboardActivity.dashboardActivity.openFeedback(
                                SurveyDB.findById(Long.parseLong(uid)), false);
                    }
                });
            }
        });
        webView.addJavascriptInterface(mWebViewInterceptor,
                "Android");
        return webView;
    }

    public void showListOfSurveys(String surveyIds) {

        FragmentManager fm = getActivity().getSupportFragmentManager();

        MonitorSurveysDialogFragment monitorSurveysDialogFragment =
                MonitorSurveysDialogFragment.newInstance(surveyIds, serverClassification);

        monitorSurveysDialogFragment.show(fm, "");
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
        super.reloadData();

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
