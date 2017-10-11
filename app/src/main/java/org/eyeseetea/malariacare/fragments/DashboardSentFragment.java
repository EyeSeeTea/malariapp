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

import static org.eyeseetea.malariacare.R.id.program;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

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
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.listeners.SwipeDismissListViewTouchListener;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardSentFragment extends ListFragment implements IModuleFragment {


    public static final String TAG = ".CompletedFragment";
    private final static int WITHOUT_ORDER =0;
    private final static int FACILITY_ORDER =1;
    private final static int DATE_ORDER =2;
    private final static int SCORE_ORDER =3;
    private static int LAST_ORDER =WITHOUT_ORDER;

    private SurveyReceiver surveyReceiver;
    protected AssessmentSentAdapter adapter;
    //surveys contains all the surveys without filter
    private List<SurveyDB> surveys;
    //oneSurveyForOrgUnit contains the filtered orgunit list
    List<SurveyDB> oneSurveyForOrgUnit;


    //orderBy contains the selected order
    int orderBy=WITHOUT_ORDER;
    //reverse contains the selected order asc or desc
    static boolean reverse=false;
    DashboardActivity dashboardActivity;

    boolean forceAllSurveys;

    CustomRadioButton customRadioButton;
    /**
     * Toggles the state of the flag that determines if only shown one or all the surveys
     */

    OrgUnitProgramFilterView orgUnitProgramFilterView =
            (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                    .findViewById(R.id.improve_org_unit_program_filter_view);

    public void toggleForceAllSurveys(){
        this.forceAllSurveys=!this.forceAllSurveys;
    }
    public boolean isForceAllSurveys() {
        return forceAllSurveys;
    }

    public DashboardSentFragment() {
        this.surveys = new ArrayList();
        oneSurveyForOrgUnit = new ArrayList<>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dashboardActivity = (DashboardActivity) activity;
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

        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.improve_org_unit_program_filter_view);

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(ProgramDB selectedProgramFilter) {
                        reloadSentSurveys(surveys);
                    }

                    @Override
                    public void onOrgUnitFilterChanged(OrgUnitDB selectedOrgUnitFilter) {
                        reloadSentSurveys(surveys);
                    }
                });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        initCheckBox(getView().getRootView());
        initAdapter();
        initListView();
        resetList();
    }

    private void initCheckBox(View view) {
        customRadioButton = (CustomRadioButton) view.findViewById(
                R.id.check_show_all_surveys);
        forceAllSurveys = false;
        PreferencesState.getInstance().setForceAllSentSurveys(forceAllSurveys);
        customRadioButton.setChecked(true);
        customRadioButton.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                             toggleForceAllSurveys();
                                                             PreferencesState.getInstance().setForceAllSentSurveys(isForceAllSurveys());
                                                             ((CustomRadioButton) v).setChecked(!isForceAllSurveys());
                                                             reloadData();
                                                 }
                                             }
        );


    }

    public void resetList() {
        adapter.setItems(oneSurveyForOrgUnit);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the one in session is created.
     */
    private void initAdapter(){
        this.adapter = new AssessmentSentAdapter(this.surveys, getActivity());
    }

    public void setScoreOrder()
    {
        orderBy=SCORE_ORDER;
        reloadSentSurveys(surveys);
    }

    public void setFacilityOrder()
    {
        orderBy=FACILITY_ORDER;
        reloadSentSurveys(surveys);
    }

    public void setDateOrder()
    {
        orderBy=DATE_ORDER;
        reloadSentSurveys(surveys);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        Log.d(TAG, "onListItemClick");
        super.onListItemClick(l, v, position, id);

        //Discard clicks on header|footer (which is attended on onNewSurvey via super)
        if(!isPositionASurvey(position)){
            return;
        }

        // call feedbackselected function(and it call surveyfragment)
        dashboardActivity.openFeedback(oneSurveyForOrgUnit.get(position - 1));
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();
        super.onStop();
    }
    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }
    /**
     * Checks if the given position points to a real survey instead of a footer or header of the listview.
     * @param position
     * @return true|false
     */
    private boolean isPositionASurvey(int position){
        return !isPositionFooter(position) && !isPositionHeader(position);
    }

    /**
     * Checks if the given position is the header of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionHeader(int position){
        return position<=0;
    }

    /**
     * Checks if the given position is the footer of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionFooter(int position){
        return position==(this.surveys.size()+1);
    }

    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View header = inflater.inflate(this.adapter.getHeaderLayout(),null,false);
        View footer = inflater.inflate(this.adapter.getFooterLayout(), null, false);
        if(!PreferencesState.getInstance().isVerticalDashboard())
            header=initFilterOrder(header);
        else
        {
            CustomTextView title = (CustomTextView) getActivity().findViewById(R.id.titleCompleted);
            title.setText(adapter.getTitle());
        }
        ListView listView = getListView();
        if(!PreferencesState.getInstance().isVerticalDashboard())
            listView.setBackgroundColor(getResources().getColor(R.color.feedbackDarkBlue));
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        setListAdapter(adapter);
        if(PreferencesState.getInstance().isVerticalDashboard()){

            // Create a ListView-specific touch listener. ListViews are given special treatment because
            // by default they handle touches for their list items... i.e. they're in charge of drawing
            // the pressed state (the list selector), handling list item clicks, etc.
            SwipeDismissListViewTouchListener touchListener =
                    new SwipeDismissListViewTouchListener(
                            listView,
                            new SwipeDismissListViewTouchListener.DismissCallbacks() {
                                @Override
                                public boolean canDismiss(int position) {
                                    return position>0 && position<=surveys.size();
                                }

                                @SuppressLint("StringFormatInvalid")
                                @Override
                                public void onDismiss(ListView listView, int[] reverseSortedPositions) {

                                    for (final int position : reverseSortedPositions) {
                                        final SurveyDB selectedSurvey=((SurveyDB)adapter.getItem(position-1));

                                        String confirmMessage =getActivity().getString(R.string.dialog_info_delete_survey);
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getActivity().getString(R.string.dialog_title_delete_survey))
                                                .setMessage(String.format(confirmMessage,selectedSurvey.getFullName()))
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        selectedSurvey.delete();
                                                        Intent surveysIntent=new Intent(getActivity(), SurveyService.class);
                                                        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
                                                        getActivity().startService(surveysIntent);
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
        }
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
            LocalBroadcastManager.getInstance(PreferencesState.getInstance().getContext()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.RELOAD_SENT_FRAGMENT_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver() {
        if (surveyReceiver != null) {
            Log.d(TAG,"UnregisterSurveysReceiver");
            LocalBroadcastManager.getInstance(PreferencesState.getInstance().getContext()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    public void refreshScreen(List<SurveyDB> newListSurveys) {
        Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        adapter.setItems(newListSurveys);
        this.adapter.notifyDataSetChanged();
    }

    public void reloadSurveys(List<SurveyDB> newListSurveys) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        adapter.setItems(newListSurveys);
        this.adapter.notifyDataSetChanged();
        if(isAdded())
            setListShown(true);
        else{
            reloadData();
        }
    }

    @Override
    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_SENT_FRAGMENT_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
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
        }else if (isForceAllSurveys()) {
            for (SurveyDB survey : surveys) {
                if(isNotFilteredByOU(survey) && isNotFilteredByProgram(survey)) {
                    oneSurveyForOrgUnit.add(survey);
                }
            }
        }else if(PreferencesState.getInstance().isLastForOrgUnit()){
            for (SurveyDB survey : surveys) {
                if (survey.getOrgUnit() != null && survey.getProgram() != null) {
                    if (!programOUSurveyDict.containsKey(survey.getProgram().getUid(),
                            survey.getOrgUnit().getUid())) {
                        addSurveyIfNotFiltered(programOUSurveyDict, survey);
                    } else {
                        SurveyDB surveyMapped = programOUSurveyDict.get(survey.getProgram().getUid(),
                                survey.getOrgUnit().getUid());
                        //Log.d(TAG, "reloadSentSurveys check NPE \tsurveyMapped:" + surveyMapped + "\tsurvey:" + survey);
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
                    Float noScore=0f;
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
                            compare = (survey1.hasMainScore()?survey1.getMainScore():noScore).compareTo((survey2.hasMainScore()?survey2.getMainScore():noScore));
                            break;
                        default:
                            compare = (survey1.hasMainScore()?survey1.getMainScore():noScore).compareTo((survey2.hasMainScore()?survey2.getMainScore():noScore));
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
     * @param programOUSurveyDict
     * @param survey
     * @return
     */
    private ProgramOUSurveyDict addSurveyIfNotFiltered(ProgramOUSurveyDict programOUSurveyDict, SurveyDB survey) {
        if(isNotFilteredByOU(survey) && isNotFilteredByProgram(survey)) {
            SurveyDB previousSurvey = programOUSurveyDict.get(survey.getProgram().getUid(), survey.getOrgUnit().getUid());
            if (previousSurvey==null || previousSurvey.getCompletionDate().compareTo(survey.getCompletionDate()) < 0)
                programOUSurveyDict.put(survey.getProgram().getUid(), survey.getOrgUnit().getUid(), survey);
        }
        return programOUSurveyDict;
    }

    private boolean isNotFilteredByOU(SurveyDB survey){
        OrgUnitDB orgUnitDB = orgUnitProgramFilterView.getSelectedOrgUnitFilter();

        if(orgUnitDB.getName().equals(PreferencesState.getInstance().getContext().getString(
                        R.string.filter_all_org_units)) ||
                orgUnitDB.getUid().equals(survey.getOrgUnit().getUid()))
            return true;
        return false;
    }

    private boolean isNotFilteredByProgram(SurveyDB survey){
        ProgramDB programDB = orgUnitProgramFilterView.getSelectedProgramFilter();

        if(programDB.getName().equals(PreferencesState.getInstance().getContext().getString(
                R.string.filter_all_org_assessments)) ||
                programDB.getUid().equals(survey.getProgram().getUid()))
            return true;
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
                BaseServiceBundle sentDashboardBundle = (BaseServiceBundle) Session.popServiceValue(SurveyService.RELOAD_SENT_FRAGMENT_ACTION);
                surveys = (List<SurveyDB>) sentDashboardBundle.getModelList(SurveyDB.class.getName());
                reloadSentSurveys(surveys);
            }
        }
    }
}