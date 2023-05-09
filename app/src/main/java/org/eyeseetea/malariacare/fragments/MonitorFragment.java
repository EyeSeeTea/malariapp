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

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.monitor.InitMessagesInvoker;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.FacilityTableBuilderByProgram;
import org.eyeseetea.malariacare.data.database.utils.monitor.facilities.SetClassificationContextInvoker;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.monitor.pies.PieBuilderByProgram;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.dashboard.config.MonitorFilter;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.presentation.presenters.monitoring.MonitorPresenter;
import org.eyeseetea.malariacare.views.MonitorSurveysDialogFragment;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MonitorFragment extends FiltersFragment implements IModuleFragment, MonitorPresenter.View {

    public static final String TAG = ".MonitorFragment";

    private MonitorPresenter monitorPresenter;

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
                invokeUpdateProgramFilter(webView, getSelectedProgramUidFilter());
            } else {
                invokeUpdateOrgUnitFilter(webView, getSelectedOrgUnitUidFilter());
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

        webView = rootView.findViewById(R.id.dashboard_monitor);

        initPresenter();

        return rootView;
    }

    private void initPresenter() {
        monitorPresenter = DataFactory.INSTANCE.provideMonitorPresenter();

        monitorPresenter.attachView(this, SurveyStatusFilter.SENT, getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        stopMonitor();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        monitorPresenter.detachView();

        super.onDestroy();
    }

    public void setFilterType(MonitorFilter monitorFilter) {
        this.filterType = monitorFilter;
    }

    private void showMessageNoAddedSurveys(boolean show) {
        getActivity().findViewById(R.id.monitor_no_surveys_message).setVisibility(
                show ? View.VISIBLE : View.GONE);
    }

    public void reloadMonitor(List<Survey> surveys, Map<String, Program> programs, Map<String, OrgUnit> orgUnits) {
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
                    new PieBuilderByOrgUnit(surveys, serverClassification, orgUnits)
                            .addDataInChart(view);
                }
                if (isProgramFilterActive()) {
                    new PieBuilderByProgram(surveys, serverClassification, programs)
                            .addDataInChart(view);
                }

                if (isOrgUnitFilterActive()) {
                    new FacilityTableBuilderByProgram(surveys,orgUnits, programs).addDataInChart(
                            view,
                            serverClassification);
                }
                if (isProgramFilterActive()) {
                    new FacilityTableBuilderByOrgUnit(surveys,orgUnits, programs).addDataInChart(
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
                                SurveyDB.getSurveyByUId(uid), false);
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

        if (monitorPresenter != null) {
            monitorPresenter.refresh(getSelectedProgramUidFilter(), getSelectedOrgUnitUidFilter());
        }
    }

    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }

    @Override
    public void showData(@NotNull List<Survey> surveys,
                         @NotNull Map<String, Program> programs,
                         @NotNull Map<String, OrgUnit> orgUnits) {
        if (surveys.size() == 0) {
            showMessageNoAddedSurveys(true);
        } else {
            showMessageNoAddedSurveys(false);

            reloadMonitor(surveys, programs, orgUnits);
        }
    }
}
