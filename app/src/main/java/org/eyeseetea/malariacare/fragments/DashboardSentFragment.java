/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramOUSurveyDict;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentSentAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DashboardSentFragment extends Fragment implements IModuleFragment {


    public static final String TAG = ".SentFragment";
    private final static int WITHOUT_ORDER = 0;
    private final static int FACILITY_ORDER = 1;
    private final static int DATE_ORDER = 2;
    private final static int SCORE_ORDER = 3;
    private static int LAST_ORDER = WITHOUT_ORDER;

    private SurveyReceiver surveyReceiver;
    protected AssessmentSentAdapter adapter;
    //surveys contains all the surveys without filter
    private List<SurveyDB> surveys;
    //oneSurveyForOrgUnit contains the filtered orgunit list
    List<SurveyDB> oneSurveyForOrgUnit;


    //orderBy contains the selected order
    int orderBy = WITHOUT_ORDER;
    //reverse contains the selected order asc or desc
    static boolean reverse = false;

    boolean forceAllSurveys;

    CustomRadioButton customRadioButton;
    TextView noSurveysText;

    private RecyclerView recyclerView;
    private View rootView;

    /**
     * Toggles the state of the flag that determines if only shown one or all the surveys
     */

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public void toggleForceAllSurveys() {
        this.forceAllSurveys = !this.forceAllSurveys;
    }

    public boolean isForceAllSurveys() {
        return forceAllSurveys;
    }

    public DashboardSentFragment() {
        this.surveys = new ArrayList();
        oneSurveyForOrgUnit = new ArrayList<>();
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

        loadFilter();

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(ProgramDB selectedProgramFilter) {
                        reloadSentSurveys(surveys);
                        saveCurrentFilters();
                    }

                    @Override
                    public void onOrgUnitFilterChanged(OrgUnitDB selectedOrgUnitFilter) {
                        reloadSentSurveys(surveys);
                        saveCurrentFilters();
                    }
                });

        rootView = inflater.inflate(R.layout.improve_listview, null);
        noSurveysText = rootView.findViewById(R.id.no_surveys_improve);

        initComponents();
        initRecyclerView();
        refreshSurveys();

        return rootView;
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(
                orgUnitProgramFilterView.getSelectedProgramFilter().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter().getUid());
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
                        .findViewById(R.id.improve_org_unit_program_filter_view);
    }

    private void initComponents() {
        customRadioButton = (CustomRadioButton) rootView.findViewById(
                R.id.check_show_all_surveys);
        forceAllSurveys = false;
        PreferencesState.getInstance().setForceAllSentSurveys(forceAllSurveys);
        customRadioButton.setChecked(true);
        customRadioButton.setOnClickListener(v -> {
                    toggleForceAllSurveys();
                    PreferencesState.getInstance().setForceAllSentSurveys(isForceAllSurveys());
                    ((CustomRadioButton) v).setChecked(!isForceAllSurveys());
                    reloadData();
                }
        );
    }

    public void refreshSurveys() {
        adapter.setSurveys(oneSurveyForOrgUnit);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.sentSurveyList);

        adapter = new AssessmentSentAdapter();
        recyclerView.setAdapter(adapter);
    }

    public void setScoreOrder() {
        orderBy = SCORE_ORDER;
        reloadSentSurveys(surveys);
    }

    public void setFacilityOrder() {
        orderBy = FACILITY_ORDER;
        reloadSentSurveys(surveys);
    }

    public void setDateOrder() {
        orderBy = DATE_ORDER;
        reloadSentSurveys(surveys);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }

    //Adds the clicklistener to the header CustomTextView.
    private View initFilterOrder(View header) {

        CustomTextView statusctv = (CustomTextView) header.findViewById(R.id.statusHeader);

        statusctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateOrder();
            }
        });
        CustomTextView scorectv = (CustomTextView) header.findViewById(R.id.scoreHeader);

        scorectv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScoreOrder();
            }
        });

        CustomTextView facilityctv = (CustomTextView) header.findViewById(R.id.idHeader);

        facilityctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFacilityOrder();
            }
        });
        return header;
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(
                    PreferencesState.getInstance().getContext()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.RELOAD_SENT_FRAGMENT_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver() {
        if (surveyReceiver != null) {
            Log.d(TAG, "UnregisterSurveysReceiver");
            LocalBroadcastManager.getInstance(
                    PreferencesState.getInstance().getContext()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    public void refreshScreen(List<SurveyDB> newListSurveys) {
        Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): "
                + newListSurveys.size());
        adapter.setSurveys(newListSurveys);
        if (!this.isAdded()) {
            return;
        }
        if (newListSurveys.isEmpty()) {
            noSurveysText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noSurveysText.setVisibility(View.GONE);
        }
    }

    @Override
    public void reloadData() {
        updateSelectedFilters();

        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD,
                SurveyService.RELOAD_SENT_FRAGMENT_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    /**
     * filter the surveys for last survey in org unit, and set surveysForGraphic for the statistics
     */
    public void reloadSentSurveys(List<SurveyDB> surveys) {
        // To prevent from reloading too fast, before service has finished its job
        if (surveys == null) return;

        HashMap<String, SurveyDB> orgUnits;
        orgUnits = new HashMap<>();
        ProgramOUSurveyDict programOUSurveyDict = new ProgramOUSurveyDict();
        oneSurveyForOrgUnit = new ArrayList<>();
        if (PreferencesState.getInstance().isNoneFilter()) {
            for (SurveyDB survey : surveys) {
                oneSurveyForOrgUnit.add(survey);
            }
        } else if (isForceAllSurveys()) {
            for (SurveyDB survey : surveys) {
                if (isNotFilteredByOU(survey) && isNotFilteredByProgram(survey)) {
                    oneSurveyForOrgUnit.add(survey);
                }
            }
        } else if (PreferencesState.getInstance().isLastForOrgUnit()) {
            for (SurveyDB survey : surveys) {
                if (survey.getOrgUnit() != null && survey.getProgram() != null) {
                    if (!programOUSurveyDict.containsKey(survey.getProgram().getUid(),
                            survey.getOrgUnit().getUid())) {
                        addSurveyIfNotFiltered(programOUSurveyDict, survey);
                    } else {
                        SurveyDB surveyMapped = programOUSurveyDict.get(
                                survey.getProgram().getUid(),
                                survey.getOrgUnit().getUid());
                        //Log.d(TAG, "reloadSentSurveys check NPE \tsurveyMapped:" + surveyMapped
                        // + "\tsurvey:" + survey);
                        if ((surveyMapped.getCompletionDate() != null
                                && survey.getCompletionDate() != null)
                                && surveyMapped.getCompletionDate().before(
                                survey.getCompletionDate())) {
                            programOUSurveyDict = addSurveyIfNotFiltered(programOUSurveyDict,
                                    survey);
                        }
                    }
                }
            }
            oneSurveyForOrgUnit = programOUSurveyDict.values();
        }

        //Order the surveys, and reverse if is needed, taking the last order from LAST_ORDER
        if (orderBy != WITHOUT_ORDER) {
            reverse = false;
            if (orderBy == LAST_ORDER) {
                reverse = true;
            }
            Collections.sort(oneSurveyForOrgUnit, new Comparator<SurveyDB>() {
                public int compare(SurveyDB survey1, SurveyDB survey2) {
                    int compare;
                    Float noScore = 0f;
                    switch (orderBy) {
                        case FACILITY_ORDER:
                            String surveyA = survey1.getOrgUnit().getName();
                            String surveyB = survey2.getOrgUnit().getName();
                            compare = surveyA.compareTo(surveyB);
                            break;
                        case DATE_ORDER:
                            compare = survey1.getCompletionDate().compareTo(
                                    survey2.getCompletionDate());
                            break;
                        case SCORE_ORDER:
                            compare = (survey1.hasMainScore() ? survey1.getMainScore()
                                    : noScore).compareTo(
                                    (survey2.hasMainScore() ? survey2.getMainScore() : noScore));
                            break;
                        default:
                            compare = (survey1.hasMainScore() ? survey1.getMainScore()
                                    : noScore).compareTo(
                                    (survey2.hasMainScore() ? survey2.getMainScore() : noScore));
                            break;
                    }

                    if (reverse) {
                        return (compare * -1);
                    }
                    return compare;
                }
            });
        }
        if (reverse) {
            LAST_ORDER = WITHOUT_ORDER;
        } else {
            LAST_ORDER = orderBy;
        }
        refreshScreen(oneSurveyForOrgUnit);
    }

    /**
     * This method add a survey to the program/OU/survey map only in case it's not filtered by the
     * selected filters and the survey is older than any previous existing in the map
     */
    private ProgramOUSurveyDict addSurveyIfNotFiltered(ProgramOUSurveyDict programOUSurveyDict,
            SurveyDB survey) {
        if (isNotFilteredByOU(survey) && isNotFilteredByProgram(survey)) {
            SurveyDB previousSurvey = programOUSurveyDict.get(survey.getProgram().getUid(),
                    survey.getOrgUnit().getUid());
            if (previousSurvey == null || previousSurvey.getCompletionDate().compareTo(
                    survey.getCompletionDate()) < 0) {
                programOUSurveyDict.put(survey.getProgram().getUid(), survey.getOrgUnit().getUid(),
                        survey);
            }
        }
        return programOUSurveyDict;
    }

    private boolean isNotFilteredByOU(SurveyDB survey) {
        OrgUnitDB orgUnitDB = orgUnitProgramFilterView.getSelectedOrgUnitFilter();

        if (orgUnitDB.getName().equals(PreferencesState.getInstance().getContext().getString(
                R.string.filter_all_org_units)) ||
                orgUnitDB.getUid().equals(survey.getOrgUnit().getUid())) {
            return true;
        }
        return false;
    }

    private boolean isNotFilteredByProgram(SurveyDB survey) {
        ProgramDB programDB = orgUnitProgramFilterView.getSelectedProgramFilter();

        if (programDB.getName().equals(PreferencesState.getInstance().getContext().getString(
                R.string.filter_all_org_assessments)) ||
                programDB.getUid().equals(survey.getProgram().getUid())) {
            return true;
        }
        return false;
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
            if (SurveyService.RELOAD_SENT_FRAGMENT_ACTION.equals(intent.getAction())) {
                BaseServiceBundle sentDashboardBundle = (BaseServiceBundle) Session.popServiceValue(
                        SurveyService.RELOAD_SENT_FRAGMENT_ACTION);
                surveys = (List<SurveyDB>) sentDashboardBundle.getModelList(
                        SurveyDB.class.getName());
                reloadSentSurveys(surveys);
            }
        }
    }
}