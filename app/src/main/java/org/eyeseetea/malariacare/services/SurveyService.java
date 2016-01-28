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

package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.database.utils.feedback.FeedbackBuilder;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItemBuilder;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * A service that looks for current Surveys to show on Dashboard(Details) in an asyn manner.
 * Created by arrizabalaga on 16/06/15.
 */
public class SurveyService extends IntentService {

    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD="serviceMethod";

    /**
     * Name of the parameter that holds every lists that goes into the planned tab
     */
    public static final String PLANNED_ACTION="org.eyeseetea.malariacare.services.SurveyService.PLANNED_ACTION";

    /**
     * Name of 'All monitor data' action
     */
    public static final String ALL_MONITOR_DATA_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_MONITOR_DATA_ACTION";

    /**
     *
     * Name of the parameter that holds every survey that goes into the DashboardUnsentFragment
     */
    public static final String UNSENT_DASHBOARD_ACTION="org.eyeseetea.malariacare.services.SurveyService.UNSENT_DASHBOARD_ACTION";
    /**
     *
     * Name of the parameter that holds every survey that goes into the DashboardSentFragment
     */
    public static final String SENT_DASHBOARD_ACTION="org.eyeseetea.malariacare.services.SurveyService.SENT_DASHBOARD_ACTION";

    /**
     * Name of 'All create survey data' action
     */
    public static final String ALL_CREATE_SURVEY_DATA_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_CREATE_SURVEY_DATA_ACTION";

    /**
     * Name of the parameter that holds every survey that goes into the planned tab
     */
    public static final String PLANNED_SURVEYS="org.eyeseetea.malariacare.services.SurveyService.PLANNED_SURVEYS";

    /**
     * Name of 'list unsent or uncompleted' action
     */
    public static final String ALL_IN_PROGRESS_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION";
    /**
     * Name of 'list completed (and unsent)' action
     */
    public static final String ALL_COMPLETED_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_COMPLETED_SURVEYS_ACTION";

    /**
     * Name of 'list completed' action (Used in DashboardSentFragment)
     */
    public static final String ALL_SENT_OR_COMPLETED_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_SENT_OR_COMPLETED_SURVEYS_ACTION";

    /**
     * Name of 'reload' action which returns both lists (unsent, sent)
     */
    public static final String RELOAD_DASHBOARD_ACTION ="org.eyeseetea.malariacare.services.SurveyService.RELOAD_DASHBOARD_ACTION";

    /**
     * Name of 'show' action
     */
    public static final String PREPARE_SURVEY_ACTION ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION";

    /**
     * Name of 'feedback' action
     */
    public static final String PREPARE_FEEDBACK_ACTION="org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION";

    /**
     * Name of 'All filter sentfragment' action
     */
    public static final String ALL_ORG_UNITS_AND_PROGRAMS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_ORG_UNITS_AND_PROGRAMS_ACTION";
    /**
     * Name of 'All programs' action
     */
    public static final String ALL_PROGRAMS="org.eyeseetea.malariacare.services.SurveyService.ALL_PROGRAMS";

    /**
     * Key of composite scores entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_COMPOSITE_SCORES ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES";

    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_TABS";
    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_ALL_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_ALL_TABS";
    /**
     * Key of programs entry in shared session
     */
    public static final String PREPARE_PROGRAMS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_PROGRAMS";
    /**
     * Key of surveys entry in shared session
     */
    public static final String PREPARE_SURVEYS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEYS";
    /**
     * Key of org unit entry in shared session
     */
    public static final String PREPARE_ORG_UNIT ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_ORG_UNIT";
    /**
     * Key of org unit level entry in shared session
     */
    public static final String PREPARE_ORG_UNIT_LEVEL ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_ORG_UNIT_LEVEL";

    /**
     * Key of
     */
    public static final String PRELOAD_TAB_ITEMS ="org.eyeseetea.malariacare.services.SurveyService.PRELOAD_TAB_ITEMS";

    /**
     * Key of 'feedback' items in shared session
     */
    public static final String PREPARE_FEEDBACK_ACTION_ITEMS="org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS";

    /**
     * Tag for logging
     */
    public static final String TAG = ".SurveyService";

    /**
     * Constructor required due to a error message in AndroidManifest.xml if it is not present
     */
    public SurveyService(){
        super(SurveyService.class.getSimpleName());
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SurveyService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Take action to be done
        switch (intent.getStringExtra(SERVICE_METHOD)){
            case PREPARE_SURVEY_ACTION:
                prepareSurveyInfo();
                break;
            case ALL_IN_PROGRESS_SURVEYS_ACTION:
                getAllInProgressSurveys();
                break;
            case ALL_SENT_OR_COMPLETED_SURVEYS_ACTION:
                getAllSentOrCompletedSurveys(ALL_SENT_OR_COMPLETED_SURVEYS_ACTION);
                break;
            case ALL_COMPLETED_SURVEYS_ACTION:
                getAllCompletedSurveys();
                break;
            case RELOAD_DASHBOARD_ACTION:
                reloadDashboard();
                break;
            case PRELOAD_TAB_ITEMS:
                Log.e(".SurveyService", "Pre-loading tab: " + intent.getLongExtra("tab", 0));
                preLoadTabItems(intent.getLongExtra("tab", 0));
                break;
            case PREPARE_FEEDBACK_ACTION:
                getFeedbackItems();
                break;
            case ALL_MONITOR_DATA_ACTION:
                getAllMonitorData();
                break;
            case ALL_ORG_UNITS_AND_PROGRAMS_ACTION:
                getAllOrgUnitsAndPrograms();
                break;
            case ALL_CREATE_SURVEY_DATA_ACTION:
                getAllCreateSurveyData();
                break;
            case ALL_PROGRAMS:
                getAllPrograms();
                break;
            case PLANNED_ACTION:
                getPlannedSurveys();
                break;
            case UNSENT_DASHBOARD_ACTION:
                getUnsentDashboard();
                break;
            case SENT_DASHBOARD_ACTION:
                getSentDashboard();
                break;
        }
    }

    private void getAllCreateSurveyData() {
        Log.d(TAG,"getAllCreateSurveyData (Thread:"+Thread.currentThread().getId()+")");
        List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ID_PARENT).isNull()).queryList();
        List<OrgUnitLevel> orgUnitLevelList = new Select().all().from(OrgUnitLevel.class).queryList();
        List<Program> programList = Program.list();

        HashMap<String,List> orgCreateSurveyData=new HashMap<>();
        orgCreateSurveyData.put(PREPARE_ORG_UNIT, orgUnitList);
        orgCreateSurveyData.put(PREPARE_ORG_UNIT_LEVEL, orgUnitLevelList);
        orgCreateSurveyData.put(PREPARE_PROGRAMS, programList);

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_CREATE_SURVEY_DATA_ACTION, orgCreateSurveyData);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_CREATE_SURVEY_DATA_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllOrgUnitsAndPrograms() {
        Log.d(TAG,"getAllOrgUnitAndPrograms (Thread:"+Thread.currentThread().getId()+")");
        List<OrgUnit> orgUnitList=OrgUnit.getAllOrgUnit();
        List<Program> programList=Program.getAllPrograms();

        HashMap<String,List> orgUnitsAndPrograms=new HashMap<>();
        orgUnitsAndPrograms.put(PREPARE_ORG_UNIT, orgUnitList);
        orgUnitsAndPrograms.put(PREPARE_PROGRAMS, programList);
        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_ORG_UNITS_AND_PROGRAMS_ACTION, orgUnitsAndPrograms);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_ORG_UNITS_AND_PROGRAMS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);

    }

    private void getAllMonitorData() {
        Log.d(TAG,"getAllMonitorData (Thread:"+Thread.currentThread().getId()+")");
        List<Program> programList=Program.getAllPrograms();
        List<Survey> sentSurveys=Survey.getAllSentSurveys();

        HashMap<String,List> monitorMap=new HashMap<>();
        monitorMap.put(PREPARE_SURVEYS, sentSurveys);
        monitorMap.put(PREPARE_PROGRAMS, programList);
        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_MONITOR_DATA_ACTION, monitorMap);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_MONITOR_DATA_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllPrograms() {
        Log.d(TAG, "getAllPrograms (Thread:" + Thread.currentThread().getId() + ")");
        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_PROGRAMS, Program.getAllPrograms());

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_PROGRAMS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllInProgressSurveys() {
        Log.d(TAG,"getAllUncompletedUnsentUnplanedSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllInProgressSurveys();

        //Load %completion in every survey (it takes a while so it can NOT be done in UI Thread)
        for(Survey survey:surveys){
            survey.getAnsweredQuestionRatio();
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_IN_PROGRESS_SURVEYS_ACTION, surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_IN_PROGRESS_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void preLoadTabItems(Long tabID){
        Tab tab = Tab.findById(tabID);
        if (tab !=null) {
            Utils.preloadTabItems(tab);
        }
    }

    private void getSentDashboard() {
        Log.d(TAG, "SentDashboard");

        getAllSentOrCompletedSurveys(ALL_SENT_OR_COMPLETED_SURVEYS_ACTION);
        getAllOrgUnitsAndPrograms();
    }

    private void getUnsentDashboard() {
        Log.d(TAG, "UnsentDashboard");

        getAllInProgressSurveys();
        getAllCompletedSurveys();
    }

    private void getPlannedSurveys() {
        Log.d(TAG, "PlannedSurveys");
        getAllPlannedSurveys();
        getAllPrograms();
    }

    private void getAllPlannedSurveys(){
        Session.putServiceValue(PLANNED_SURVEYS, PlannedItemBuilder.getInstance().buildPlannedItems());
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PLANNED_SURVEYS));
    }

    private void reloadDashboard(){
        Log.d(TAG, "reloadDashboard");
        //Returning result to anyone listening
        getAllPrograms();
        getAllCreateSurveyData();
        getAllMonitorData();
        getAllInProgressSurveys();
        getAllCompletedSurveys();
        getAllSentOrCompletedSurveys(ALL_SENT_OR_COMPLETED_SURVEYS_ACTION);
        getAllOrgUnitsAndPrograms();
        getAllPlannedSurveys();

    }

    /**
     * Action that calculates the 'feedback' items corresponding to the current survey in session
     */
    private void getFeedbackItems(){
        //Mock some items
        List<Feedback> feedbackList= FeedbackBuilder.build(Session.getSurvey());

        //Return result to anyone listening
        Log.d(TAG, String.format("getFeedbackItems: %d", feedbackList.size()));

        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PREPARE_FEEDBACK_ACTION));
    }

    /**
     * Selects all sent surveys from database
     */
    private void getAllSentOrCompletedSurveys(String service){
        Log.d(TAG,"getAllSentOrCompletedSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllSentOrCompletedSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(service,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(service);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllCompletedSurveys(){
        Log.d(TAG,"getCompletedSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllCompletedSurveys();

        //Load %completion in every survey (it takes a while so it can NOT be done in UI Thread)
        for(Survey survey:surveys){
            survey.getAnsweredQuestionRatio();
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_COMPLETED_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_COMPLETED_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    /**
     * Prepares required data to show a survey completely (tabs and composite scores).
     */
    private void prepareSurveyInfo(){
        Log.d(TAG, "prepareSurveyInfo (Thread:" + Thread.currentThread().getId() + ")");

        List<CompositeScore> compositeScores = CompositeScore.list();
        ScoreRegister.registerCompositeScores(compositeScores);

        //Get tabs for current program & register them (scores)
        List<Tab> tabs = Tab.getTabsBySession();
        List<Tab> allTabs = new Select().all().from(Tab.class).queryList();

        ScoreRegister.registerTabScores(tabs);

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(PREPARE_SURVEY_ACTION_COMPOSITE_SCORES, compositeScores);
        Session.putServiceValue(PREPARE_SURVEY_ACTION_TABS, tabs);
        Session.putServiceValue(PREPARE_ALL_TABS, allTabs);
        //Returning result to anyone listening
        Intent resultIntent = new Intent(PREPARE_SURVEY_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
