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

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentSentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardSentFragment extends ListFragment {


    public static final String TAG = ".CompletedFragment";
    private final static int WITHOUT_ORDER =0;
    private final static int FACILITY_ORDER =1;
    private final static int DATE_ORDER =2;
    private final static int SCORE_ORDER =3;
    private static int LAST_ORDER =WITHOUT_ORDER;
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;
    protected IDashboardAdapter adapter;
    private static int index = 0;
    List<Survey> oneSurveyForOrgUnit;
    List<OrgUnit> orgUnitList;
    List <Program> programList;
    Spinner filterSpinnerOrgUnit;
    Spinner filterSpinnerProgram;
    String orgUnitFilter;
    String programFilter;
    int orderBy=WITHOUT_ORDER;
    static boolean reverse=false;
    OnFeedbackSelectedListener mCallback;

    public DashboardSentFragment() {
        this.adapter = Session.getAdapterSent();
        this.surveys = new ArrayList();
        oneSurveyForOrgUnit = new ArrayList<>();
    }

    public static DashboardSentFragment newInstance(int index) {
        DashboardSentFragment f = new DashboardSentFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }


    // Container Activity must implement this interface
    public interface OnFeedbackSelectedListener {
        public void onFeedbackSelected(Survey survey);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFeedbackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFeedbackSelectedListener");
        }
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        orgUnitFilter= getActivity().getString(R.string.filter_all_org_units_upper);
        programFilter= getActivity().getString(R.string.filter_all_org_assessments_upper);
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

        initAdapter();
        initListView();
    }
    private void initFilters() {
        filterSpinnerProgram = (Spinner) getActivity().findViewById(R.id.filter_program);
        List<Program> filterProgramList=programList;
        filterProgramList.add(0, new Program(getActivity().getString(R.string.filter_all_org_assessments_upper)));

        filterSpinnerProgram.setAdapter(new FilterProgramArrayAdapter(this.getActivity().getApplicationContext(), filterProgramList));
        filterSpinnerProgram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Program program = (Program) parent.getItemAtPosition(position);
                boolean reload = false;
                if (program.getName().equals(getActivity().getString(R.string.filter_all_org_assessments_upper))) {
                    if (programFilter != getActivity().getString(R.string.filter_all_org_assessments_upper)) {
                        programFilter = getActivity().getString(R.string.filter_all_org_assessments_upper);
                        reload=true;
                    }
                } else {
                    if (programFilter != program.getUid()) {
                        programFilter = program.getUid();
                        reload=true;
                    }
                }
                if(reload)
                    reloadSentSurveys();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        filterSpinnerOrgUnit = (Spinner) getActivity().findViewById(R.id.filter_orgunit);

        orgUnitList.add(0, new OrgUnit(getActivity().getString(R.string.filter_all_org_units_upper)));
        filterSpinnerOrgUnit.setAdapter(new FilterOrgUnitArrayAdapter(getActivity().getApplicationContext(), orgUnitList));
        filterSpinnerOrgUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                OrgUnit orgUnit = (OrgUnit) parent.getItemAtPosition(position);
                boolean reload = false;
                if (orgUnit.getName().equals(getActivity().getString(R.string.filter_all_org_units_upper))) {
                    if (orgUnitFilter != getActivity().getString(R.string.filter_all_org_units_upper)) {
                        orgUnitFilter = getActivity().getString(R.string.filter_all_org_units_upper);
                        reload = true;
                    }
                } else {
                    if (orgUnitFilter != orgUnit.getUid()) {
                        orgUnitFilter = orgUnit.getUid();
                        reload = true;
                    }
                }
                if (reload)
                    reloadSentSurveys();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }
    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        //Loading...
        setListShown(false);
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
        IDashboardAdapter adapterInSession = Session.getAdapterSent();
        if(adapterInSession == null){
            adapterInSession = new AssessmentSentAdapter(this.surveys, getActivity());
        }else{
            adapterInSession = adapterInSession.newInstance(this.surveys, getActivity());
        }
        this.adapter = adapterInSession;
        Session.setAdapterSent(this.adapter);
    }

    public void setScoreOrder()
    {
        orderBy=SCORE_ORDER;
        reloadSentSurveys();
    }

    public void setFacilityOrder()
    {
        orderBy=FACILITY_ORDER;
        reloadSentSurveys();
    }

    public void setDateOrder()
    {
        orderBy=DATE_ORDER;
        reloadSentSurveys();
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        Log.d(TAG, "onListItemClick");
        super.onListItemClick(l, v, position, id);

        //Discard clicks on header|footer (which is attended on newSurvey via super)
        if(!isPositionASurvey(position)){
            return;
        }

        // call feedbackselected function(and it call surveyfragment)

        mCallback.onFeedbackSelected(surveys.get(position - 1));
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
        View header = inflater.inflate(this.adapter.getHeaderLayout(), null, false);
        View footer = inflater.inflate(this.adapter.getFooterLayout(), null, false);
        header=initFilterOrder(header);
        ListView listView = getListView();
        listView.setBackgroundColor(getResources().getColor(R.color.feedbackDarkBlue));
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        setListAdapter((BaseAdapter) adapter);
        Session.listViewSent = listView;
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
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_SENT_OR_COMPLETED_SURVEYS_ACTION));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_ORG_UNITS_AND_PROGRAMS_ACTION));
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

    public void reloadSurveys(List<Survey> newListSurveys) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        boolean hasSurveys = newListSurveys != null && newListSurveys.size() > 0;
        this.surveys.clear();
        this.surveys.addAll(newListSurveys);
        adapter.setItems(newListSurveys);
        this.adapter.notifyDataSetChanged();
        setListShown(true);
    }

    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    /**
     * filter the surveys for last survey in org unit, and set surveysForGraphic for the statistics
     */
    public void reloadSentSurveys() {
        List<Survey> surveys = (List<Survey>) Session.popServiceValue(SurveyService.ALL_SENT_OR_COMPLETED_SURVEYS_ACTION);
        HashMap<String, Survey> orgUnits;
        orgUnits = new HashMap<>();
        oneSurveyForOrgUnit = new ArrayList<>();

        for (Survey survey : surveys) {
            if (survey.isSent() || survey.isCompleted()) {
                if (survey.getOrgUnit() != null) {
                    if (!orgUnits.containsKey(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid())) {
                        filterSurvey(orgUnits, survey);
                    } else {
                        Survey surveyMapped = orgUnits.get(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid());
                        Log.d(TAG,"reloadSentSurveys check NPE \tsurveyMapped:"+surveyMapped+"\tsurvey:"+survey);
                        Log.d(TAG,"reloadSentSurveys check completionDate\tsurveyMapped:"+surveyMapped.getCompletionDate()+"\tsurvey:"+survey.getCompletionDate());
                        if (surveyMapped.getCompletionDate().before(survey.getCompletionDate())) {
                            orgUnits=filterSurvey(orgUnits, survey);
                        }
                    }
                }
            }
        }
        for (Survey survey : orgUnits.values()) {
            oneSurveyForOrgUnit.add(survey);
        }
        //Order the surveys, and reverse if is needed, taking the last order from LAST_ORDER
        if (orderBy != WITHOUT_ORDER) {
            reverse=false;
            if(orderBy==LAST_ORDER){
                reverse=true;
            }
            Collections.sort(oneSurveyForOrgUnit, new Comparator<Survey>() {
                public int compare(Survey survey1, Survey survey2) {
                    int compare;
                    switch (orderBy) {
                        case FACILITY_ORDER:
                            String surveyA = survey1.getOrgUnit().getName();
                            String surveyB = survey2.getOrgUnit().getName();
                            compare = surveyA.compareTo(surveyB);
                            break;
                        case DATE_ORDER:
                            compare = survey1.getCompletionDate().compareTo(survey2.getCompletionDate());
                            break;
                        case SCORE_ORDER:
                            compare = survey1.getMainScore().compareTo(survey2.getMainScore());
                            break;
                        default:
                            compare = survey1.getMainScore().compareTo(survey2.getMainScore());
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
            LAST_ORDER=WITHOUT_ORDER;
        }
        else{
            LAST_ORDER=orderBy;
        }
        reloadSurveys(oneSurveyForOrgUnit);
    }

    public void getOrgUnitAndProgram(){
        HashMap<String,List> data=(HashMap) Session.popServiceValue(SurveyService.ALL_ORG_UNITS_AND_PROGRAMS_ACTION);
        orgUnitList=data.get(SurveyService.PREPARE_ORG_UNIT);
        programList=data.get(SurveyService.PREPARE_PROGRAMS);
    }

    private HashMap<String, Survey> filterSurvey(HashMap<String, Survey> orgUnits, Survey survey) {
        if(orgUnitFilter.equals(getActivity().getString(R.string.filter_all_org_units_upper)) || orgUnitFilter.equals(survey.getOrgUnit().getUid()))
            if(programFilter.equals(getActivity().getString(R.string.filter_all_org_assessments_upper)) || programFilter.equals(survey.getTabGroup().getProgram().getUid()))
              orgUnits.put(survey.getTabGroup().getProgram().getUid()+survey.getOrgUnit().getUid(), survey);
        return orgUnits;
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
            if (SurveyService.ALL_SENT_OR_COMPLETED_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadSentSurveys();
            }
            if(SurveyService.ALL_ORG_UNITS_AND_PROGRAMS_ACTION.equals(intent.getAction())){
                getOrgUnitAndProgram();
                initFilters();
            }
        }
    }
}